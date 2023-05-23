package com.mobileplus.dummytriluc.transceiver

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
import com.mobileplus.dummytriluc.transceiver.data.TransceiverChannel
import com.mobileplus.dummytriluc.ui.utils.extensions.dataArray
import com.mobileplus.dummytriluc.ui.utils.extensions.dataObject
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.LinkedList

/**
 * Created by KO Huyn on 16/05/2023.
 */
class TransceiverControllerImpl private constructor(): ITransceiverController,KoinComponent {

    private val dataManager by inject<DataManager>()

    private lateinit var socket: ISocketController
    val currentState: ConnectionState = ConnectionState.NONE
    private val cachedCommand = LinkedList<ICommand>()
    private var transceiverEventState: ((TransceiverEvent) -> Unit)? = null
    private var onEventMachineSend: ((JsonObject) -> Unit)? = null
    private var machineInfo: MachineInfo? = null
    private val compositeDisposable = CompositeDisposable()

    override fun startup() {
        socket = SocketControllerImpl()
        socket.startup(BuildConfig.SOCKET_URL, dataManager.getToken() ?: "")
        socket.onListener { event, data ->
            val eventState = TransceiverEvent.getEvent(event)
            if (eventState != null) {
                transceiverEventState?.invoke(eventState)
                handleConnectionState(eventState, data)
            }
        }
    }

    override fun onTransceiverEventStateListener(listener: (TransceiverEvent) -> Unit) {
        transceiverEventState = listener
    }

    override fun onEventMachineSend(listener: (data: JsonObject) -> Unit) {
        onEventMachineSend = listener
    }

    override fun registerChannel(channel: TransceiverChannel, vararg data: Pair<String, String>) {
        socket.emit(channel.channel, *data)
    }

    override fun connectToMachine(machineInfo: MachineInfo) {
        this.machineInfo = machineInfo
        registerAllListenerState()
        socket.connect()
    }

    override fun getMachineInfo(): MachineInfo? {
        return machineInfo
    }

    override fun disconnect() {
        send(DisconnectCommand(machineInfo, dataManager.getUserInfo()?.id, true))
        compositeDisposable.clear()
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
                .mapNotNull { (key, value) -> if (value == null) null else key to value.toString() }
                .toTypedArray()
            socket.emit(cmd.getEventName(), *arg)
        } else {
            cachedCommand.add(cmd)
        }
    }

    private fun IPracticeCommand.appendDataPractice(): HashMap<String, Any?> {
        val params = params()
        params["machine_id"] = machineInfo?.machineRoom
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
                machineInfo?.machineRoom?.let { room ->
                    send(ConnectCommand(room))
                }
            }

            TransceiverEvent.DISCONNECT -> {

            }

            TransceiverEvent.CONNECT_ERROR -> {
                machineInfo?.let { connectToMachine(it) }
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
                            val sessionId = jsonPractice.getOrNull<String>("sessionId")
                            if (sessionId != null) {
                                dataManager.getDataPracticeResult(sessionId)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ response ->
                                        logErr(response.toString())
                                        onEventMachineSend?.invoke(response.dataArray().get(0).asJsonObject)
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

    private fun <T> JSONObject.getOrNull(key: String): T? {
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