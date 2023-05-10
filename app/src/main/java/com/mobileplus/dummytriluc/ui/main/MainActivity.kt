package com.mobileplus.dummytriluc.ui.main

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.KeyEvent
import com.core.BaseActivity
import com.google.gson.Gson
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.*
import com.mobileplus.dummytriluc.bluetooth.ble.BluetoothLeService
import com.mobileplus.dummytriluc.bluetooth.request.BleReceiveDataErrorRequest
import com.mobileplus.dummytriluc.bluetooth.request.SessionResponse
import com.mobileplus.dummytriluc.data.model.NotificationObjService
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.service.TriLucNotification
import com.mobileplus.dummytriluc.ui.dialog.ConnectBleWithDeviceDialog
import com.mobileplus.dummytriluc.ui.dialog.NoInternetDialog
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.challenge.detail.ChallengeDetailFragment
import com.mobileplus.dummytriluc.ui.main.chat.chatmessage.ChatMessageFragment
import com.mobileplus.dummytriluc.ui.main.coach.CoachMainFragment
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.CoachGroupDetailFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionFragment
import com.mobileplus.dummytriluc.ui.main.connect.ConnectFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.dialog.ConfirmPracticeTestDialog
import com.mobileplus.dummytriluc.ui.main.practice.test.PracticeTestFragment
import com.mobileplus.dummytriluc.ui.main.testconnect.TestConnectFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.eventbus.*
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.mobileplus.dummytriluc.ui.utils.language.LocalManageUtil
import com.mobileplus.dummytriluc.ui.video.record.VideoRecordFragment
import com.utils.ext.*
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.net.URISyntaxException
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


class MainActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.activity_main
    private val mainViewModel by viewModel<MainViewModel>()
    var userInfo: UserInfo? = null
    val hotline: String by lazy { mainViewModel.hotline }
    private var mSocket: Socket? = null
    private val gson by inject<Gson>()


    override fun updateUI(savedInstanceState: Bundle?) {
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
        saveConfig()
        listenerBleDataResponse()
        handleActionConnection()
        userInfo = mainViewModel.user
        if (mainViewModel.isFirstConnect) {
            mainViewModel.setFirstConnected(false)
            openFragment(R.id.mainContainer, TestConnectFragment::class.java, null, true)
        } else {
            openFragment(R.id.mainContainer, MainFragment::class.java, null, true)
        }
        navigateDeepLink(intent?.data)
        onHandleIntentNotification(intent)

    }

    //--------------------------------------------------------------------------|
    //                                                                          |
    //                                                                          |
    //                                 BLUETOOTH                                |
    //                                                                          |
    //                                                                          |
    //--------------------------------------------------------------------------|

    companion object {
        const val BLUETOOTH_TAG = "BLUETOOTH"
        const val MAX_LENGTH_BLUETOOTH_RECEIVE = 500
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_PERMISSION_LOCATION = 3
        const val MAX_TURN_ERROR = 30
    }

    /**
     * [rxResponseDataBle]
     * @return String JSON
     * @author KO Huyn
     * Theo dõi data bluetooth
     */
    val rxResponseDataBle: PublishSubject<String> = PublishSubject.create()

    /**
     * [rxCallbackDataBle]
     * @return String JSON
     * @author KO Huyn
     * Gửi object bluetooth hoàn chỉnh sau khi ghép nối xong
     */
    val rxCallbackDataBle: PublishSubject<Pair<Boolean, List<BluetoothResponse>>> =
        PublishSubject.create()

    /**
     * [bleDevice]
     * @author KO Huyn
     * thiết bị kết nối
     */
    var bleDevice: BluetoothDevice? = null

    /**
     * [bluetoothAdapter]
     * @author KO Huyn
     * @return đúng khi lần đầu kết nối để xử lý dữ liệu từ những lần trước chưa được gửi lên
     */
    private var isDataCacheBle: Boolean = false
        set(value) {
            field = value
            logErr("isDataCacheBle:$isDataCacheBle")
        }

    /**
     * [bluetoothAdapter]
     * @author KO Huyn
     * Represents the local device Bluetooth adapter
     */
    private var bluetoothAdapter: BluetoothAdapter? = null

    /**
     * [bluetoothService]
     * @author KO Huyn
     * Quản lý các sự kiện hành động của bluetooth(thường dùng cho BluetoothLeService)
     */
    private var bluetoothService: BluetoothLeService? = null

    /**
     * [rxStateConnectBle]
     * @return RxBleConnectionState
     * @author KO Huyn
     * Theo dõi trạng thái bluetooth
     */
    val rxStateConnectBle: PublishSubject<BluetoothStatus> =
        PublishSubject.create()

    /**
     * [isConnectedBle]
     * @author KO Huyn
     * @return kiểm tra trạng thái kết nối của bluetooth
     */
    var isConnectedBle: Boolean = false
        get() {
            return if (bluetoothService is BluetoothLeService) (bluetoothService as BluetoothLeService).isConnected()
            else field
        }

    /**
     * [response]
     * @return trả về dữ liệu bluetooth sau khi append từng mảng.
     */
    private val response = StringBuilder()

    /**
     * @param [isLoadingBleData] loading khi máy tập đang trả dữ liệu
     * @author KO Huyn
     */

    private var isLoadingBleData = false
        set(value) {
            field = value
            if (field) showDialogLoadDataBle(getString(R.string.syncing_data_machine))
            else {
                if (!isLoadingBleDeleteData) {
                    hideDialogLoadDataBle()
                }
            }
        }

    /**
     * @param [isLoadingBleDeleteData] loading khi máy tập đang xoá dữ liệu
     * @author KO Huyn
     */
    private var isLoadingBleDeleteData = false
        set(value) {
            field = value
            logErr("isLoadingBleDeleteData: $value")
            if (field) showDialogLoadDataBle(getString(R.string.optimizing_data_machine), true)
            else hideDialogLoadDataBle()
        }


    /**
     * @param [loadingConnectingBle] loading khi kết nối bluetooth
     * @author KO Huyn
     */
    private var loadingConnectingBle = false
        set(value) {
            field = value
            if (field) showDialogLoadDataBle(getString(R.string.connecting), true)
            else hideDialogLoadDataBle()
        }
    var rxLoadingBle: Disposable = rxResponseDataBle.debounce(200, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            isLoadingBleData = false
            if (isKeepConnectFirst) {
                rxActionConnection.onNext(actionConnection)
                isKeepConnectFirst = false
            }
            modeCheckSumWithTurn(response.toString())
            response.clear()
        }

    /**
     * [reConnectWriteBle]
     * @author KO Huyn
     * @return dữ liệu ghi xuống máy tập khi máy tập mất kết nối sẽ kết nối lại
     */
    private var reConnectWriteBle: ReconnectWriteBle? = null

    /**
     * @param [command] câu lệnh truyền xuống máy tập
     * @author KO Huyn
     */
    fun actionWriteBle(command: String): Boolean {
        logErr(BLUETOOTH_TAG, "write ble:$command")
        return if (isConnectedBle) {
            if (command == CommandBle.END) {
                isLoadingBleDeleteData = true
            }
            bluetoothService?.writeCharacteristic(command.toByteArray())
            true
        } else {
            reConnectWriteBle = ReconnectWriteBle(command)
            bluetoothService?.reconnectBle()
            return false
        }
    }

    /**
     * Xử lý sau khi quét được bluetooth
     */
    private fun connectBluetoothListener(device: BluetoothDevice) {
        bluetoothService?.setupAdvertise(bluetoothAdapter)
        bleDevice = device
        bluetoothService?.connect(device)
    }

    /**
     * @return ngắt kết nối bluetooth
     */
    fun triggerDisconnect() {
        logErr(BLUETOOTH_TAG, "triggerDisconnect")
        bluetoothService?.stopAdvertise(bluetoothAdapter)
        bluetoothService?.disconnect()
    }

    private var lastTurnReceive = AppConstants.INTEGER_DEFAULT
    private val mutableMapOfTurn = mutableMapOf<Int, String?>()

    private var countErrorBleSend = 0

    private fun List<BluetoothResponseMachine>?.transformToBleServer(): List<BluetoothResponse>? {
        return this?.map { bleMachine ->
            val bleDataServer = mutableListOf<DataBluetooth?>()
            bleMachine.data.forEach { dataBleMachine ->
                if (dataBleMachine != null) {
                    bleDataServer.add(
                        DataBluetooth(
                            force = dataBleMachine.force,
                            onTarget = dataBleMachine.onTarget,
                            position = dataBleMachine.position,
                            time = dataBleMachine.time
                        )
                    )
                }
            }
            BluetoothResponse(
                mode = bleMachine.mode,
                userId = bleMachine.userId,
                sessionId = bleMachine.sessionId,
                practiceId = bleMachine.practiceId,
                machineId = bleMachine.machineId,
                startTime1 = bleMachine.startTime1,
                endTime = bleMachine.endTime,
                startTime2 = bleMachine.startTime2,
                data = bleDataServer,
                error = bleMachine.error
            )
        }
    }


    private fun writeErrorTurn(turnsError: MutableList<Int>) {
        listTurnError = turnsError
        val subListOf: MutableList<Int> =
            if (listTurnError.size > MAX_TURN_ERROR)
                listTurnError.subList(0, MAX_TURN_ERROR)
            else listTurnError
        listTurnError = if (listTurnError.size > MAX_TURN_ERROR)
            listTurnError.subList(MAX_TURN_ERROR, listTurnError.size)
        else mutableListOf()
        val cmd = gson.toJson(
            BleReceiveDataErrorRequest(
                lastTurnReceive,
                subListOf,
                subListOf.size
            )
        )
        if (listTurnError.isEmpty()) {
            isLoadingBleDeleteData = true
        }
        this.actionWriteBle(cmd.plus("~"))
        logErr("listTurnError", listTurnError.joinToString(","))
    }

    data class CheckSumBle(private val turn: String? = null) {
        private val turnFormatter get() = turn?.replace("*", "")?.replace("~", "")
        val value get() = turnFormatter?.subValueChecksum('|')
        private val turnLength
            get() = turnFormatter?.substringAfterLast('|')?.replaceStringToIntOrNull()
        val turnCount get() = turnFormatter?.substringBefore('|')?.toIntOrNull()
        val isFail: Boolean
            get() {
                return turnLength != value?.length || value == null || turnLength == null || turnCount == null
            }

        private fun String.subValueChecksum(char: Char): String? {
            var indexFrom = indexOf(char) + 1
            var indexTo = lastIndexOf(char)
            if (indexFrom == -1) indexFrom = 0
            if (indexTo == -1) indexTo = this.length
            return try {
                if (indexFrom >= indexTo) null else
                    substring(indexFrom, indexTo)
            } catch (e: Exception) {
                e.logErr()
                null
            }
        }
    }

    var listTurnError = mutableListOf<Int>()
    private fun modeCheckSumWithTurn(response: String) {
        logErr("response: $response")
        if (response.contains(AppConstants.SUCCESS_BLE)) {
            isLoadingBleDeleteData = false
        }

        val listTurn: List<CheckSumBle> =
            response.split("*").map { CheckSumBle(it) }.filter { !it.isFail }
        logErr(
            "write ble",
            listTurn.map { "${it.turnCount}-${it.value}" }.joinToString("\n")
        )
        val mapTemp = mutableMapOf<Int?, String?>()
        listTurn.forEach { mapTemp[it.turnCount] = it.value }
        val listTurnCount: List<Int> = mapTemp.keys.filterNotNull()
        val lastTurnReceiverLocal = listTurnCount.lastOrNull() ?: AppConstants.INTEGER_DEFAULT
        logErr(
            "modeCheckSumWithTurn",
            listTurnCount.joinToString("-")
        )
        if (lastTurnReceive == AppConstants.INTEGER_DEFAULT) {
            lastTurnReceive = lastTurnReceiverLocal
        } else {
            if (lastTurnReceive < lastTurnReceiverLocal) {
                lastTurnReceive = lastTurnReceiverLocal
            }
        }

        for (i in 0..lastTurnReceive) {
            if (listTurnCount.contains(i) && !mutableMapOfTurn.containsKey(i)) {
                mutableMapOfTurn[i] = mapTemp[i]
            }
        }
        for (i in 0..lastTurnReceive) {
            if (!mutableMapOfTurn.containsKey(i)) {
                if (!listTurnError.contains(i)) {
                    listTurnError.add(i)
                }
            }
        }
        logErr("mutableMapOfTurn", mutableMapOfTurn.values.joinToString("\n"))
        if (listTurnError.isEmpty()) {
            countErrorBleSend = 0
            val json = mutableMapOfTurn.toSortedMap().values.joinToString("")
            logErrFull("JSON MAIN ACTIVITY", json)

            //Mode session
            gson.fromJsonSafe<SessionResponse>(json)?.let {
                checkOpenCoachSessionContinue(it)
            }
            //data đòn đánh
            gson.fromJsonSafe<List<BluetoothResponseMachine>>(json).let {
                if (it == null) {
                    if (json.contains("{") || json.contains("[")
                        || json.contains("|") || json.contains("~")
                    ) {
                        if (gson.fromJsonSafe<SessionResponse>(json) != null) {
                            logErr(BLUETOOTH_TAG, "write ble:it == SessionResponse")
                        } else {
                            this.actionWriteBle(CommandBle.END)
                            mainViewModel.logErrorBleMachineJSON(response)
                            logErr(BLUETOOTH_TAG, "write ble:it == null")
                        }
                    } else {
                        mainViewModel.logErrorBleMachineJSON(response)
                        logErr(BLUETOOTH_TAG, "write ble:NOT JSON")
                    }
                } else {
                    if (it.isNotEmpty()) {
                        writeErrorTurn(listTurnError)
                        logErr(BLUETOOTH_TAG, "write ble:it.isNotEmpty()")
                    } else {
                        this.actionWriteBle(CommandBle.END)
                        logErr(BLUETOOTH_TAG, "else")
                    }
                }
                mutableMapOfTurn.clear()
                lastTurnReceive = AppConstants.INTEGER_DEFAULT
                if (!response.contains(AppConstants.SUCCESS_BLE)) {
                    postSticky(EventOpenPopupBle(it.transformToBleServer()))
                }
            }
        } else {
            countErrorBleSend++
            if (countErrorBleSend > 20) {
                listTurnError.clear()
                writeErrorTurn(listTurnError)
                lastTurnReceive = AppConstants.INTEGER_DEFAULT
                countErrorBleSend = 0
                logErr(BLUETOOTH_TAG, "write ble:countErrorBleSend MAX")
            } else {
                writeErrorTurn(listTurnError)
            }
        }
    }

    private fun handleBluetoothResponseSuccess(bluetoothResponses: List<BluetoothResponse>?) {
        if (bluetoothResponses != null) {
            if (bluetoothResponses.isEmpty()) {
                rxCallbackDataBle.onNext(Pair(true, emptyList()))
            } else {
                handleDataBleModeFreedom(bluetoothResponses)
                if (isDataCacheBle) {
                    val dataSession =
                        bluetoothResponses.filter { ble -> ble.sessionId != null }
                    val dataNormal =
                        bluetoothResponses.filter { ble ->
                            val isModeFree =
                                ble.mode == BluetoothResponse.MODE_FREE_FIGHT || ble.mode == BluetoothResponse.MODE_ACCORDING_LED
                            ble.sessionId == null && isModeFree
                        }
                    logErr("post data cache")
                    if (dataNormal.isNotEmpty()) {
                        mainViewModel.submitDataBle(dataNormal)
                    }
                    if (dataSession.isNotEmpty()) {
                        mainViewModel.saveResultSession(dataSession)
                    }
                } else {
                    rxCallbackDataBle.onNext(Pair(true, bluetoothResponses))
                }
            }
        } else {
            rxCallbackDataBle.onNext(Pair(false, emptyList()))
        }
        isDataCacheBle = false
    }

    private var isUserSync: Boolean = false
    private fun listenerBleDataResponse() {
        addDispose(mainViewModel.rxPostModeFreedomSuccess.subscribe { isSuccess ->
            if (isSuccess && isUserSync) {
                postNormal(EventReloadPracticeItem())
//                toast("Đẩy dữ liệu từ máy tập thành công")
            }
        })
        addDispose(rxLoadingBle)
        addDispose(rxResponseDataBle.observeOn(AndroidSchedulers.mainThread()).subscribe {
            if (!isLoadingBleData) isLoadingBleData = true
            response.append(it)
        })
    }

    private fun checkOpenCoachSessionContinue(sessionConnect: SessionResponse) {
        sessionConnect.sessionId?.let { idSession ->
            Handler(Looper.getMainLooper()).post {
                CoachSessionFragment.openFragmentFromSessionOld(
                    idSession, true
                )
            }
        }
    }

    private val json =
        """[{"mode":2,"start_time1":1623859425,"start_time2":4900015,"user_id":293,"machine_id":1,"data":[{"force":30.32,"position":"b","time":4936709},{"force":30.50,"position":"b","time":4937907},{"force":32.20,"position":"4","time":4938675},{"force":65.97,"position":"4","time":4939971},{"force":30.50,"position":"b","time":4940882},{"force":30.50,"position":"b","time":4941808}],"end_time":1623039415}]"""

    private val isCurrentFragmentInPractice: Boolean = currentFrag() is PracticeTestFragment

    private fun handleDataBleModeFreedom(bluetoothResponses: List<BluetoothResponse>) {
        isUserSync =
            bluetoothResponses.any { bleResponse -> bleResponse.userId == userInfo?.id }
        val isFreeDom =
            bluetoothResponses.any { ble -> ble.sessionId == null && (ble.mode == BluetoothResponse.MODE_FREE_FIGHT || ble.mode == BluetoothResponse.MODE_ACCORDING_LED) }
        if (isFreeDom && !isDataCacheBle) {
            mainViewModel.submitDataBle(bluetoothResponses)
        }

        if (isUserSync && isFreeDom) {
            val arr =
                bluetoothResponses.filter { ble ->
                    (ble.mode == BluetoothResponse.MODE_FREE_FIGHT || ble.mode == BluetoothResponse.MODE_ACCORDING_LED)
                            && isUserSync && ble.sessionId == null && ble.userId == userInfo?.id
                }
            if (arr.isNullOrEmpty()) return
            runOnUiThread {
                ConfirmPracticeTestDialog.Builder(arr)
                    .setTitle(getString(R.string.fight_free_led))
                    .setCancelable(false)
                    .setModeCourse(false)
                    .setShowButtonPlayAgain(isCurrentFragmentInPractice)
                    .build(supportFragmentManager)
                    .setSubmitPracticeCallback { isSubmit ->
                        if (currentFrag() is PracticeTestFragment) {
                            if (isSubmit) {
//                            onBackPressed()
                            } else {
                                (currentFrag() as PracticeTestFragment).startPractice()
                            }
                        }
                    }
            }
        }
    }

    private fun findServiceCharacteristic(data: List<BluetoothGattService>?) {
        if (data.isNullOrEmpty()) {
            showPopupWhenNoServiceCharacteristic()
        } else {
            var isCharacterService = false
            data.forEach { bleGattService ->
                if (bleGattService.characteristics.isNotEmpty()) {
                    bleGattService.characteristics.forEach { child ->
                        if (child.isWriteable && child.isReadable && child.isNotifiable) {
                            bluetoothService?.setCharacteristicNotification(child, true)
                            isCharacterService = true
                            handleAfterConnectedBle()
                        }
                    }
                }
            }
            if (isCharacterService) {
                logErr("write characteristic success")
            } else {
                showPopupWhenNoServiceCharacteristic()
            }
        }
    }

    private var isKeepConnectFirst = false
        set(value) {
            field = value
            logErr("isKeepConnectFirst: $value")
        }

    private fun handleAfterConnectedBle() {
        Handler(Looper.getMainLooper()).postDelayed({

            bluetoothService?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
            val isReconnect = bluetoothService?.isReconnect ?: false
            if (isReconnect) {
                logErr("isReconnected")
                rxActionConnection.onNext(actionConnection)
                reConnectWriteBle?.let { reconnectBle ->
                    this.actionWriteBle(reconnectBle.payData)
                }
                reConnectWriteBle = null
            } else {
                isKeepConnectFirst = true
                val userId = mainViewModel.user?.id ?: 0
                isDataCacheBle = this.actionWriteBle(
                    String.format(
                        CommandBle.CONNECT,
                        userId,
                        100,
                        System.currentTimeMillis() / 1000
                    )
                )
            }
        }, 1000)
    }

    private fun showPopupWhenNoServiceCharacteristic() {
        YesNoButtonDialog()
            .setTitle("Bluetooth")
            .setMessage(getString(R.string.error_ble_restart))
            .setDismissWhenClick(true)
            .setTextAccept(getString(R.string.agree))
            .setOnCallbackAcceptButtonListener {
                bluetoothService?.disconnect()
            }
            .setOnCallbackCancelButtonListener {
                bluetoothService?.disconnect()
            }.showDialog(supportFragmentManager)
    }

    fun connectBluetooth() {
        if (isBluetoothFeature()) {
            if (isConnectedBle) {
                triggerDisconnect()
            } else {
                enableDialogConnect()
            }
        }
    }

    private fun enableDialogConnect() {
        if (isLocationPermissionGranted()) {
            ScanBluetoothLeDialog().apply { isCancelable = false }
                .build(supportFragmentManager)
                .setOnCallbackBle({
                    connectBluetoothListener(it)
                }, {
                    bluetoothService?.updateCommand(BleConstants.ACTION.TURN_OFF_BLE_ACTION)
                })
        }
    }


    /**
     * [rxActionConnection]
     * @author KO Huyn
     * @return thực thi hành động sau khi kết nối xong
     */
    private val rxActionConnection: PublishSubject<ActionConnection> = PublishSubject.create()
    var actionConnection = ActionConnection.NONE
        set(value) {
            field = value
            logErr("actionConnection:${field.name}")
        }

    fun showDialogRequestConnect(type: ActionConnection = ActionConnection.NONE) {
        if (!isConnectedBle) {
            ConnectBleWithDeviceDialog()
                .show(supportFragmentManager)
                .onConnectBleCallback { connect ->
                    if (connect) {
                        ConnectFragment.openFragment()
                        actionConnection = type
                    } else {
                        toast(getString(R.string.you_are_not_connected_to_bluetooth_to_use_this_feature))
                        actionConnection = ActionConnection.NONE
                    }
                }
        }
    }

    private fun handleActionConnection() {
        addDispose(
            rxActionConnection.debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe { action ->
                    logErr("rxActionConnection:${action.name}")
                    when (action) {
                        ActionConnection.NONE -> {
                        }
                        ActionConnection.OPEN_MODE_FREE_FIGHT -> {
                            PracticeTestFragment.openFragmentFreeFight()
                        }
                        ActionConnection.OPEN_MODE_LED -> {
                            PracticeTestFragment.openFragmentAccordingToLed()
                        }
                        ActionConnection.OPEN_MODE_COURSE -> {
                            if (currentFrag() is PracticeDetailFragment) {
                                val currFrag = currentFrag() as PracticeDetailFragment
                                currFrag.openFragmentCourse()
                            }
                        }
                        ActionConnection.OPEN_RECORD_PRACTICE -> {
                            if (currentFrag() is PracticeDetailFragment) {
                                val currFrag = currentFrag() as PracticeDetailFragment
                                currFrag.openFragmentRecord()
                            }
                        }
                        ActionConnection.OPEN_COACH_DRAFT -> {
                            VideoRecordFragment.openFromCoach()
                        }
                        ActionConnection.OPEN_SESSION -> {
                            if (currentFrag() is CoachSessionFragment) {
                                val currFrag = currentFrag() as CoachSessionFragment
                                currFrag.playSession()
                            }
                        }
                        ActionConnection.OPEN_CHALLENGE -> {
                            if (currentFrag() is ChallengeDetailFragment) {
                                val currFrag = currentFrag() as ChallengeDetailFragment
                                currFrag.openFragmentRecord()
                            }
                        }
                        else -> {
                        }
                    }
                    actionConnection = ActionConnection.NONE
                })
    }

    /**
     * [BluetoothAdapter.STATE_OFF] lắng nghe trạng thái khi tắt bluetooth
     * [BluetoothAdapter.STATE_TURNING_OFF] lắng nghe trạng thái khi đang tắt bluetooth
     * [BluetoothAdapter.STATE_ON] lắng nghe trạng thái khi bật bluetooth
     * [BluetoothAdapter.STATE_TURNING_ON] lắng nghe trạng thái khi đang bật bluetooth
     */

    private val mOnOffBluetoothReceive = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            action?.let {
                if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            logErr(BLUETOOTH_TAG, "STATE_OFF")
                            if (isConnectedBle) {
                                isConnectedBle = false
                                triggerDisconnect()
                            }
                            rxStateConnectBle.onNext(BluetoothStatus.DISCONNECTED)
                            bluetoothService?.updateCommand(
                                BleConstants.ACTION.TURN_OFF_BLE_ACTION
                            )
                        }
                        BluetoothAdapter.STATE_TURNING_OFF -> {

                        }
                        BluetoothAdapter.STATE_ON -> {
                            logErr(BLUETOOTH_TAG, "STATE_ON")
                        }
                        BluetoothAdapter.STATE_TURNING_ON -> {

                        }
                    }
                }
            }
        }
    }

    // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
// or notification operations.
    private var i = 0
    private val mGattUpdateReceive = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                BluetoothLeManager.ACTION_GATT_CONNECTED -> {
                    logErr(BLUETOOTH_TAG, "ACTION_GATT_CONNECTED")
                    isConnectedBle = true
                    loadingConnectingBle = false
                    rxStateConnectBle.onNext(BluetoothStatus.CONNECTED)
                }
                BluetoothLeManager.ACTION_GATT_DISCONNECTED -> {
                    logErr(BLUETOOTH_TAG, "ACTION_GATT_DISCONNECTED")
                    isConnectedBle = false
                    loadingConnectingBle = false
                    rxStateConnectBle.onNext(BluetoothStatus.DISCONNECTED)
                }
                BluetoothLeManager.ACTION_GATT_CONNECTING -> {
                    logErr(BLUETOOTH_TAG, "ACTION_GATT_CONNECTING")
                    loadingConnectingBle = true
                    rxStateConnectBle.onNext(BluetoothStatus.CONNECTING)
                }
                BluetoothLeManager.ACTION_GATT_SERVICE_DISCOVERED -> {
                    logErr(BLUETOOTH_TAG, "ACTION_GATT_SERVICE_DISCOVERED")
                    findServiceCharacteristic(bluetoothService?.getSupportedGattService())
                }
                BluetoothLeManager.ACTION_DATA_AVAILABLE -> {
                    val notify = intent.getByteArrayExtra(BluetoothLeManager.EXTRA_DATA)
                    i++
                    logErr(
                        BLUETOOTH_TAG,
                        "$i ACTION_DATA_AVAILABLE ${String(notify ?: byteArrayOf())}"
                    )

                    notify?.let {
                        rxResponseDataBle.onNext(String(it))
                    }
                }
                BluetoothLeManager.ACTION_WRITE_AGAIN_BLE -> {
                    val dataWrite = intent.getStringExtra(BluetoothLeManager.EXTRA_WRITE)
                    logErr(
                        BLUETOOTH_TAG,
                        "ACTION_WRITE_AGAIN_BLE :$dataWrite"
                    )
                    dataWrite?.let {
                        reConnectWriteBle = ReconnectWriteBle(it)
                    }
                }
            }
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.onDisconnectListener = {
                if (currentFrag() is PracticeTestFragment || currentFrag() is CoachSessionFragment) {
                    onBackPressed()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bluetoothService = null
        }
    }

    fun requestFINISH() {
        if (response.toString().isBlank()) {
            this.actionWriteBle(CommandBle.FINISH)
        }
    }

    private fun isBluetoothFeature(): Boolean {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast("Bluetooth not supported.")
            return false
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        bluetoothAdapter = bluetoothManager?.adapter
        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            toast("Bluetooth not supported.")
            return false
        }
        if (bluetoothAdapter?.isEnabled == false) {
            requestBluetoothTurnON()
            return false
        }

        val locationManage = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManage?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            requestLocationTurnON()
            return false
        }
        return true
    }

    private fun requestLocationTurnON() {
        YesNoButtonDialog()
            .setTitle("GPS")
            .setMessage(getString(R.string.require_gps_is_not_enable))
            .setTextAccept(getString(R.string.open_setting))
            .setOnCallbackAcceptButtonListener {
                val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(enableLocationIntent)
            }.showDialog(supportFragmentManager)
    }

    private fun requestBluetoothTurnON() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    private val BluetoothGattCharacteristic.isNotifiable: Boolean
        get() = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0

    private val BluetoothGattCharacteristic.isReadable: Boolean
        get() = properties and BluetoothGattCharacteristic.PROPERTY_READ != 0

    private val BluetoothGattCharacteristic.isWriteable: Boolean
        get() = properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothLeManager.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeManager.ACTION_GATT_CONNECTING)
        intentFilter.addAction(BluetoothLeManager.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeManager.ACTION_GATT_SERVICE_DISCOVERED)
        intentFilter.addAction(BluetoothLeManager.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BluetoothLeManager.ACTION_WRITE_AGAIN_BLE)
        return intentFilter
    }

//--------------------------------------------------------------------------|
//                                                                          |
//                                                                          |
//                                 LIFECYCLE                                |
//                                                                          |
//                                                                          |
//--------------------------------------------------------------------------|

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            ViewPumpContextWrapper.wrap(LocalManageUtil.setLocal(newBase) ?: newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        registerReceiver(mGattUpdateReceive, makeGattUpdateIntentFilter())
        registerReceiver(
            mOnOffBluetoothReceive,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        unbindServiceApp()
        super.onDestroy()
    }

    private fun unbindServiceApp() {
        try {
            unregisterReceiver(mGattUpdateReceive)
            unregisterReceiver(mOnOffBluetoothReceive)
            unbindService(mServiceConnection)
        } catch (e: Exception) {
            logErr("unregisterReceiver Exception")
        } finally {
            bluetoothService = null
        }
    }

    override fun onStart() {
        logErr("onStart()")
        DummyTriLucApplication.isRunningBackground = false
        super.onStart()
        register(this)
        if (!isConnectedInternet()) {
            val dialog = NoInternetDialog()
            dialog.show(supportFragmentManager, dialog.tag)
        }
    }

    override fun onStop() {
        logErr("onStop()")
        DummyTriLucApplication.isRunningBackground = true
        super.onStop()
        unregister(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Handler(Looper.getMainLooper()).postDelayed({
                    connectBluetooth()
                }, 500)
            } else {
                toast(getString(R.string.error_location_permission_missing))
            }
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            connectBluetooth()
                        }, 500)
                        logErr("enable Bluetooth")
                    }
                    Activity.RESULT_CANCELED -> {
                        logErr("disable Bluetooth")
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }


    override fun onBackPressed() {
        val curr = currentFrag()
        if (curr is TestConnectFragment) {
            openFragment(R.id.mainContainer, MainFragment::class.java, null, true)
            return
        }
        if (curr is MainFragment) {
            if (curr.isOpenDrawer()) {
                curr.closeDrawer()
                return
            }
            if (curr.currViewPager != 0) {
                curr.currViewPager = 0
                return
            }
        }
        if (curr is VideoRecordFragment) {
            postNormal(EventOverlayCameraView(false))
        }
        if (curr is ChatMessageFragment) {
            postNormal(EventReloadRoomChat())
        }
        super.onBackPressed()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currFrag = currentFrag()
        if (currFrag is PracticeTestFragment) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (currFrag.isDoingPractice()) {
                    toast(getString(R.string.you_have_not_finished_the_exercise_yet))
                    return false
                }
            }
        }
        if (currFrag is CoachSessionFragment) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (currFrag.isDoingSessionPractice()) {
                    toast(getString(R.string.you_have_not_finished_the_session_yet))
                    return false
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

//--------------------------------------------------------------------------|
//                                                                          |
//                                                                          |
//                                 MORE                                     |
//                                                                          |
//                                                                          |
//--------------------------------------------------------------------------|


    @Subscribe
    fun logout(ev: EventUnAuthen) {
        mainViewModel.logout()
    }

    @Subscribe
    fun onOnOffBle(ev: BleTurnOnEvent) {
        if (!isConnectedBle) {
            enableDialogConnect()
        }
        logErr("onOnOffBle: ${ev.isOnBle}")
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true)
    fun onOnOffBleBackground(ev: BleTurnOnBackgroundEvent) {
        if (!isConnectedBle) {
            enableDialogConnect()
        }
        removeStickyEvents(ev)
        logErr("onOnOffBleBackground")
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true)
    fun onPostPopup(ev: EventOpenPopupBle) {
        handleBluetoothResponseSuccess(ev.list)
        removeStickyEvents(ev)
    }

    @Subscribe
    fun nextFragmentMain(ev: EventNextFragmentMain) {
        openFragment(
            R.id.mainContainer,
            ev.clazz,
            ev.bundle,
            ev.isAddToBackStack
        )
    }

    @Subscribe
    fun onLanguageChange(ev: EventChangeLanguage) {
        finish()
        startActivity(MainActivity::class.java)
        bluetoothService?.updateNotificationLang()
    }

    fun getSocket(): Socket {
        if (mSocket == null) {
            setupSocketIO()
        }
        return mSocket!!
    }

    private fun setupSocketIO() {
        try {
            val opts = IO.Options()
            opts.transports = arrayOf(WebSocket.NAME)
            opts.query = "token=${mainViewModel.dataManager.getToken()}"
            mSocket = IO.socket(BuildConfig.SOCKET_URL, opts)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    private fun onHandleIntentNotification(intent: Intent?) {
        intent?.extras?.let { bundle ->
            val notifiArg = bundle.getString(TriLucNotification.NOTIFICATION_ARG, "")
            if (notifiArg.isNotEmpty()) {
                try {
                    logErr(notifiArg)
                    val notification =
                        gson.fromJson(notifiArg, NotificationObjService::class.java)
                    if (currentFrag() !is MainFragment) {
                        popBackToMain()
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        postNormal(EventHandleNotification(notification))
                    }, 500)
                } catch (e: Exception) {
                    e.logErr()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun navigateDeepLink(uri: Uri?) {
        if (uri != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                postNormal(
                    EventNavigateDeepLink(
                        uri.getQueryParameter(ApiConstants.TYPE) ?: "",
                        uri.getQueryParameter(ApiConstants.ID)?.toInt()
                            ?: AppConstants.INTEGER_DEFAULT
                    )
                )
            }, 500)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateDeepLink(intent?.data)
        onHandleIntentNotification(intent)
    }

    private fun saveConfig() {
        mainViewModel.getConfigApp()
        openPopupUpdateApp()
    }

    private fun openPopupUpdateApp() {
        addDispose(
            mainViewModel.rxShowPopupUpdateVersionApp.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val (lastVersionName, isRequireUpdate, description) = it
                    val msgRequireUpdate =
                        if (isRequireUpdate) getString(R.string.you_need_update_to_be_able_to_use_this_app) else ""
                    YesNoButtonDialog()
                        .setTitle(getString(R.string.version_app_new, lastVersionName))
                        .setMessage("${description ?: getString(R.string.do_you_want_update_version)} $msgRequireUpdate")
                        .setTextAccept(getString(R.string.update)).apply {
                            isCancelable = false
                        }
                        .setTextCancel(
                            if (isRequireUpdate) getString(R.string.exit) else getString(
                                R.string.skip
                            )
                        )
                        .setDismissWhenClick(!isRequireUpdate)
                        .showDialog(supportFragmentManager)
                        .setOnCallbackAcceptButtonListener {
                            val packageNameZ: String =
                                if (BuildConfig.FLAVOR == "dev") "com.mobileplus.trilucmaster" else packageName
                            try {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=$packageNameZ")
                                    )
                                )
                            } catch (e: ActivityNotFoundException) {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=$packageNameZ")
                                    )
                                )
                            }
                        }.setOnCallbackCancelButtonListener {
                            if (isRequireUpdate) {
                                finishAffinity()
                                exitProcess(0)
                            } else {
                                mainViewModel.versionUpdateApp = lastVersionName
                            }
                        }
                })
    }

    fun currentFrag() = supportFragmentManager.findFragmentById(R.id.mainContainer)

    fun popBackToMain() {
        supportFragmentManager.popBackStack(MainFragment::class.java.simpleName, 0)
    }

    fun popBackToTrainerMain() {
        supportFragmentManager.popBackStack(CoachMainFragment::class.java.simpleName, 0)
    }

    private fun String.subStringAt(from: Char, to: Char): String {
        var indexFrom = indexOf(from) + 1
        var indexTo = indexOf(to)
        if (indexFrom == -1) indexFrom = 0
        if (indexTo == -1) indexTo = this.length
        return substring(indexFrom, indexTo)
    }

    private fun String.substringAfterTriLuc(
        delimiter: Char,
        missingDelimiterValue: String = this
    ): String {
        val index = indexOf(delimiter)
        return if (index == -1) missingDelimiterValue else substring(index, length)
    }

    private fun isLocationPermissionGranted(): Boolean {
        val isPermissionAccess: Boolean =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                checkSelfPermissionCompat(Manifest.permission.ACCESS_FINE_LOCATION)
            else checkSelfPermissionCompat(Manifest.permission.BLUETOOTH_SCAN) &&
                    checkSelfPermissionCompat(Manifest.permission.BLUETOOTH_CONNECT) &&
                    checkSelfPermissionCompat(Manifest.permission.BLUETOOTH_ADVERTISE)

        val arrPermissionNeedRequest =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            else arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        if (isPermissionAccess) {
            return true
        } else {
            requestPermissionsCompat(
                arrPermissionNeedRequest,
                REQUEST_PERMISSION_LOCATION
            )
            return false
        }
    }
}