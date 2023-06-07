package com.mobileplus.dummytriluc.transceiver

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.google.gson.JsonObject
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.MachineInfo
import com.mobileplus.dummytriluc.socket.ISocketController
import com.mobileplus.dummytriluc.socket.SocketControllerImpl
import com.mobileplus.dummytriluc.transceiver.command.ConnectCommand
import com.mobileplus.dummytriluc.transceiver.command.DisconnectCommand
import com.mobileplus.dummytriluc.transceiver.command.ICommand
import com.mobileplus.dummytriluc.transceiver.command.IPracticeCommand
import com.mobileplus.dummytriluc.ui.utils.extensions.dataArray
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.LinkedList

/**
 * Created by KO Huyn on 16/05/2023.
 */
class TransceiverControllerImpl private constructor() : ITransceiverController, KoinComponent {

    private val dataManager by inject<DataManager>()

    private lateinit var socket: ISocketController
    private val currentState = MutableStateFlow(ConnectionState.NONE)
    private val cachedCommand = LinkedList<ICommand>()
    private val transceiverEventState = PublishSubject.create<TransceiverEvent>()
    private var onEventMachineSend: (JsonObject?) -> Unit = {}
    private val compositeDisposable = CompositeDisposable()

    override fun startup() {
        socket = SocketControllerImpl()
        socket.startup(BuildConfig.SOCKET_URL, dataManager.getToken() ?: "")
        socket.onListener { event, data ->
            val eventState = TransceiverEvent.getEvent(event)
            if (eventState != null) {
                transceiverEventState.onNext(eventState)
                handleConnectionState(eventState, data)
            }
        }
        if (dataManager.isConnectedMachine) {
            connect()
        }
    }

    override fun onTransceiverEventStateListener(listener: (TransceiverEvent) -> Unit) {
        transceiverEventState.subscribe(listener).addTo(compositeDisposable)
    }

    override fun onEventMachineSend(listener: (data: JsonObject?) -> Unit) {
        this.onEventMachineSend = listener
    }

    override fun onConnectionStateChange(
        lifecycle: Lifecycle,
        listener: (data: ConnectionState) -> Unit
    ) {
        lifecycle.coroutineScope.launch {
            currentState.collect { listener(it) }
        }
    }

    override fun connectToMachine(machineInfo: MachineInfo) {
        dataManager.machineCodeConnectLasted = machineInfo
        connect()
    }

    private fun connect() {
        registerAllListenerState()
        socket.connect()
        currentState.tryEmit(ConnectionState.CONNECTING)
    }

    override fun getMachineInfo(): MachineInfo? {
        return dataManager.machineCodeConnectLasted
    }

    override fun disconnect() {
        send(DisconnectCommand(getMachineInfo(), dataManager.getUserInfo()?.id, true))
        forceDisconnect()
    }

    private fun forceDisconnect() {
        socket.disconnect()
    }

    override fun isConnected(): Boolean {
        return socket.isConnected()
    }

    override fun send(cmd: ICommand) {
        if (isConnected()) {
            var params = cmd.params()
            if (cmd is IPracticeCommand){
                params = cmd.appendDataPractice()
            }
            val arg = params.toList()
                .mapNotNull { (key, value) -> if (value == null) null else key to value }
                .toTypedArray()
            socket.emit(cmd.getEventName(), *arg)
        } else {
            cachedCommand.add(cmd)
        }
    }

    private fun IPracticeCommand.appendDataPractice(): HashMap<String, Any?> {
        val params = params()
        params["machine_id"] = getMachineInfo()?.machineRoom
        params["user_id"] = dataManager.getUserInfo()?.id
        params["sendTo"] = "MACHINE"
        return params
    }

    private fun registerAllListenerState() {
        socket.registerListener(TransceiverEvent.CONNECT.eventName)
        socket.registerListener(TransceiverEvent.DISCONNECT.eventName)
        socket.registerListener(TransceiverEvent.CONNECT_ERROR.eventName)
        socket.registerListener(TransceiverEvent.NEW_MESSAGE.eventName)
        socket.registerListener(TransceiverEvent.PRACTICE.eventName)
        socket.registerListener(TransceiverEvent.CONNECT_MACHINE.eventName)
    }

    private fun unregisterAllListenerState() {
        socket.unregisterListener(TransceiverEvent.CONNECT.eventName)
        socket.unregisterListener(TransceiverEvent.DISCONNECT.eventName)
        socket.unregisterListener(TransceiverEvent.CONNECT_ERROR.eventName)
        socket.unregisterListener(TransceiverEvent.NEW_MESSAGE.eventName)
        socket.registerListener(TransceiverEvent.PRACTICE.eventName)
        socket.registerListener(TransceiverEvent.CONNECT_MACHINE.eventName)
    }

    private fun handleConnectionState(eventName: TransceiverEvent, data: String?) {
        when (eventName) {
            TransceiverEvent.CONNECT -> {
                getMachineInfo()?.machineRoom?.let { room ->
                    send(ConnectCommand(room))
                    dataManager.isConnectedMachine = true
                    currentState.tryEmit(ConnectionState.CONNECTED)
                }
            }

            TransceiverEvent.DISCONNECT -> {
                dataManager.isConnectedMachine = false
                compositeDisposable.clear()
                currentState.tryEmit(ConnectionState.DISCONNECTED)
            }

            TransceiverEvent.CONNECT_ERROR -> {
                getMachineInfo()?.let { connectToMachine(it) }
                currentState.tryEmit(ConnectionState.NONE)
            }

            TransceiverEvent.NEW_MESSAGE -> {

            }
            TransceiverEvent.PRACTICE -> {
                try {
                    if (data != null) {
                        val jsonPractice = JSONObject(data)
                        val sendTo = jsonPractice.getOrNull<String>("sendTo")
                        if (sendTo != null && sendTo == "APP") {
                            val mode = jsonPractice.getOrNull<Int>("mode")
                            if (mode == 12) {
                                forceDisconnect()
                            }
                            val sessionId = jsonPractice.getOrNull<String>("sessionId")
                            if (sessionId != null) {
                                dataManager.getDataPracticeResult(sessionId)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ response ->
                                        logErr(response.toString())
                                        val json = response.dataArray().firstOrNull()?.asJsonObject
                                        onEventMachineSend(json)
                                    }, {
                                        logErr(it.toString(), it)
                                    }).let { compositeDisposable.add(it) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logErr("", e)
                }
            }
            TransceiverEvent.CONNECT_MACHINE -> {

            }
        }
    }

    private inline fun <reified T> JSONObject.getOrNull(key: String): T? {
        return if (this.has(key)) {
            this.get(key) as? T
        } else null
    }

    companion object {
        private var _instance: TransceiverControllerImpl? = null
        fun getInstance(): ITransceiverController {
            if (_instance == null) {
                synchronized(this) {
                    if (_instance == null) {
                        TransceiverControllerImpl().also { _instance = it }
                    }
                }
            }
            return _instance!!
        }
    }
}