package com.mobileplus.dummytriluc.ui.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.core.BaseActivity
import com.google.gson.Gson
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.NotificationObjService
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.service.TriLucNotification
import com.mobileplus.dummytriluc.transceiver.ConnectionState
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.ui.dialog.ConnectBleWithDeviceDialog
import com.mobileplus.dummytriluc.ui.dialog.NoInternetDialog
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.chat.chatmessage.ChatMessageFragment
import com.mobileplus.dummytriluc.ui.main.coach.CoachMainFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionFragment
import com.mobileplus.dummytriluc.ui.main.connect.ConnectFragment
import com.mobileplus.dummytriluc.ui.main.practice.test.PracticeTestFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventHandleNotification
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNavigateDeepLink
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventOverlayCameraView
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReloadRoomChat
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUnAuthen
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.mobileplus.dummytriluc.ui.utils.language.LocalManageUtil
import com.mobileplus.dummytriluc.ui.video.record.VideoRecordFragment
import com.utils.ext.hide
import com.utils.ext.isConnectedInternet
import com.utils.ext.postNormal
import com.utils.ext.register
import com.utils.ext.show
import com.utils.ext.startActivity
import com.utils.ext.unregister
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.android.synthetic.main.activity_main.tvPingServer
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.net.URISyntaxException
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

    fun showDialogRequestConnect() {
        if (!isConnectedBle) {
            ConnectBleWithDeviceDialog()
                .show(supportFragmentManager)
                .onConnectBleCallback { connect ->
                    if (connect) {
                        ConnectFragment.openFragment()
                    } else {
                        toast(getString(R.string.you_are_not_connected_to_bluetooth_to_use_this_feature))
                    }
                }
        }
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