package com.mobileplus.dummytriluc.transceiver

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.google.gson.Gson
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.MachineInfo
import com.mobileplus.dummytriluc.socket.ISocketController
import com.mobileplus.dummytriluc.socket.SocketControllerImpl
import com.mobileplus.dummytriluc.transceiver.command.ConnectCommand
import com.mobileplus.dummytriluc.transceiver.command.DisconnectCommand
import com.mobileplus.dummytriluc.transceiver.command.ICommand
import com.mobileplus.dummytriluc.transceiver.command.IMachineCommand
import com.mobileplus.dummytriluc.transceiver.command.UpdateSoundCommand
import com.mobileplus.dummytriluc.transceiver.ext.getOrNull
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import com.mobileplus.dummytriluc.transceiver.mode.SendCommandFrom
import com.mobileplus.dummytriluc.transceiver.observer.IObserverMachine
import com.mobileplus.dummytriluc.ui.utils.extensions.dataArray
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.toList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
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
    private val gson by inject<Gson>()

    private lateinit var socket: ISocketController
    private val currentState = MutableStateFlow(ConnectionState.NONE)
    private val commandState = MutableSharedFlow<SendCommandFrom>()
    private val cachedCommand = LinkedList<ICommand>()
    private val transceiverEventState = PublishSubject.create<TransceiverEvent>()
    private val onEventMachineSend: (List<BluetoothResponse>) -> Unit = { notifyObserver(it) }
    private var onPingChange: (ping: Int, rssi: Int) -> Unit = { _, _ -> }
    private val compositeDisposable = CompositeDisposable()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val observers = mutableListOf<IObserverMachine>()

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

    override fun onConnectionStateChange(
        lifecycle: Lifecycle,
        listener: (data: ConnectionState) -> Unit
    ) {
        lifecycle.coroutineScope.launch {
            currentState.collect { listener(it) }
        }
    }

    override fun onSendCommandStateListener(
        lifecycle: Lifecycle,
        listener: (state: SendCommandFrom) -> Unit
    ) {
        lifecycle.coroutineScope.launch {
            commandState.collect {
                listener(it)
            }
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

    override fun onPingChange(listener: (ping: Int, rssi: Int) -> Unit) {
        this.onPingChange = listener
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
            coroutineScope.launch {
                commandState.emit(SendCommandFrom.FromApp(cmd.getCommandMode(), cmd))
            }
            if (cmd.getCommandMode() == CommandMode.UNDEFINE) {
                return
            }
            var params = cmd.params()
            if (cmd is IMachineCommand) {
                params = cmd.appendDataPractice()
            }
            params["mode"] = cmd.getCommandMode().mode
            val arg = params.toList()
                .mapNotNull { (key, value) -> if (value == null) null else key to value }
                .toTypedArray()
            socket.emit(cmd.getEventName().eventName, *arg)
        } else {
            cachedCommand.add(cmd)
        }
    }

    private fun IMachineCommand.appendDataPractice(): HashMap<String, Any?> {
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
                    if (dataManager.machineCodeConnectLasted?.updateSound == true) {
                        send(UpdateSoundCommand)
                    }
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

            TransceiverEvent.SUBSCRIBE -> {

            }

            TransceiverEvent.UNSUBSCRIBE -> {

            }

            TransceiverEvent.PRACTICE -> {
                try {
                    if (data != null) {
                        val jsonPractice = JSONObject(data)
                        val sendTo = jsonPractice.getOrNull<String>("sendTo")
                        if (sendTo != null && sendTo == "APP") {
                            val mode = jsonPractice.getOrNull<Int>("mode")
                                ?.let { CommandMode.getOrNull(it) }
                            if (mode != null) {
                                coroutineScope.launch {
                                    commandState.emit(SendCommandFrom.FromMachine(mode, jsonPractice))
                                }
                            }
                            if (mode == CommandMode.DISCONNECT) {
                                forceDisconnect()
                            }
                            val sessionId = jsonPractice.getOrNull<String>("sessionId")
                            val isEnd = jsonPractice.getOrNull<Boolean>("is_end") ?: true
                            if (sessionId != null && sessionId != getMachineInfo()?.machineRoom && isEnd) {
                                dataManager.getDataPracticeResult(sessionId)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ response ->
                                        logErr(response.toString())
                                        val listMachineResponse =
                                            gson.toList<BluetoothResponse>(response.dataArray())
                                        sessionId.toIntOrNull()?.let { idSession ->
                                            listMachineResponse.forEach { it.sessionId = idSession }
                                        }
                                        onEventMachineSend(listMachineResponse)
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
                if (!data.isNullOrEmpty()) {
                    val json = JSONObject(data)
                    val ping = json.getOrNull<Int>("PING")
                    val rssi = json.getOrNull<Int>("RSSI")
                    if (ping != null && rssi != null) {
                        onPingChange(ping, rssi)
                    }
                }
            }
        }
    }

    companion object {
        private var _instance: TransceiverControllerImpl? = null
        fun getInstance(): TransceiverControllerImpl {
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

    override fun registerObserver(observer: IObserverMachine) {
        observers.add(observer)
    }

    override fun removeObserver(observer: IObserverMachine) {
        val index = observers.indexOf(observer)
        if (index >= 0) {
            observers.removeAt(index)
        }
    }

    private fun notifyObserver(data: List<BluetoothResponse>) {
        observers.forEach {
            it.onEventMachineSendData(data)
        }
    }
}