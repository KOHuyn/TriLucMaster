package com.mobileplus.dummytriluc.ui.main

import android.content.*
import android.net.Uri
import android.os.*
import android.view.KeyEvent
import com.core.BaseActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.*
import com.mobileplus.dummytriluc.data.model.NotificationObjService
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.service.TriLucNotification
import com.mobileplus.dummytriluc.transceiver.ConnectionState
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.transceiver.TransceiverControllerImpl
import com.mobileplus.dummytriluc.transceiver.observer.IObserverMachine
import com.mobileplus.dummytriluc.ui.dialog.ConnectBleWithDeviceDialog
import com.mobileplus.dummytriluc.ui.dialog.NoInternetDialog
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.challenge.detail.ChallengeDetailFragment
import com.mobileplus.dummytriluc.ui.main.chat.chatmessage.ChatMessageFragment
import com.mobileplus.dummytriluc.ui.main.coach.CoachMainFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionFragment
import com.mobileplus.dummytriluc.ui.main.connect.ConnectFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.dialog.ConfirmPracticeTestDialog
import com.mobileplus.dummytriluc.ui.main.practice.test.PracticeTestFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.eventbus.*
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.mobileplus.dummytriluc.ui.utils.language.LocalManageUtil
import com.mobileplus.dummytriluc.ui.video.record.VideoRecordFragment
import com.utils.ext.*
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.android.synthetic.main.activity_main.tvPingServer
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
    private val transceiver by lazy { ITransceiverController.getInstance() }
    override fun updateUI(savedInstanceState: Bundle?) {
        saveConfig()
        listenerBleDataResponse()
        handleActionConnection()
        userInfo = mainViewModel.user
        openFragment(R.id.mainContainer, MainFragment::class.java, null, true)
        navigateDeepLink(intent?.data)
        onHandleIntentNotification(intent)
        transceiver.onPingChange { ping, rssi ->
            runOnUiThread {
                tvPingServer.text = "$ping"
            }
        }
        transceiver.onConnectionStateChange(lifecycle) { state ->
            when (state) {
                ConnectionState.CONNECTED -> tvPingServer.show()
                else -> tvPingServer.hide()
            }
        }
    }

    //--------------------------------------------------------------------------|
    //                                                                          |
    //                                                                          |
    //                                 Transceiver                              |
    //                                                                          |
    //                                                                          |
    //--------------------------------------------------------------------------|

    /**
     * [isConnectedBle]
     * @author KO Huyn
     * @return kiểm tra trạng thái kết nối của bluetooth
     */
    val isConnectedBle: Boolean
        get() = transceiver.isConnected()

    /**
     * @param [command] câu lệnh truyền xuống máy tập
     * @author KO Huyn
     */
    fun actionWriteBle(command: String): Boolean {
//        transceiver.send(command)
        return false
    }

    private fun listenerBleDataResponse() {
        addDispose(mainViewModel.rxPostModeFreedomSuccess.subscribe { isSuccess ->
            if (isSuccess) {
                postNormal(EventReloadPracticeItem())
            }
        })
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
                    actionConnection = if (connect) type else ActionConnection.NONE
                    if (connect) {
                        ConnectFragment.openFragment()
                    } else {
                        toast(getString(R.string.you_are_not_connected_to_bluetooth_to_use_this_feature))
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

    override fun onBackPressed() {
        val curr = currentFrag()
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
}