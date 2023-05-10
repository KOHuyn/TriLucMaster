package com.mobileplus.dummytriluc.ui.splash

import android.os.Bundle
import com.core.BaseActivity
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.NotificationObjService
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.service.MessageService
import com.mobileplus.dummytriluc.service.TriLucNotification
import com.mobileplus.dummytriluc.ui.login.main.LoginActivity
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.startActivity
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity() {

    private val viewModel by viewModel<SplashViewModel>()

    override fun getLayoutId(): Int = R.layout.activity_splash
    private val gson by inject<Gson>()
    override fun updateUI(savedInstanceState: Bundle?) {
        viewModel.deleteTokenPushWhenFirstApp()
        viewModel.refreshToken()
        checkPushScreen()
    }

    private fun handleBundleWhenNotification(): Bundle? {
        var bundle: Bundle? = null
        if (intent.extras != null && intent.hasExtra(ApiConstants.ID) && intent.hasExtra(
                ApiConstants.TYPE
            )
        ) {
            val notifi = NotificationObjService()
            notifi.linkId = intent?.getIntExtra(ApiConstants.ID, -1) ?: 0
            notifi.type = intent?.getStringExtra(ApiConstants.TYPE) ?: "DETAIL"
            bundle = Bundle().apply {
                putString(TriLucNotification.NOTIFICATION_ARG, gson.toJson(notifi))
            }
        }
        return bundle
    }

    private fun checkPushScreen() {
        val bundleNoti = handleBundleWhenNotification()
        if (viewModel.isLogin) {
            if (bundleNoti != null) {
                startActivity(MainActivity::class.java, bundleNoti)
            } else {
                startActivity(MainActivity::class.java)
            }
        } else {
            startActivity(LoginActivity::class.java)
        }
        finish()
    }
}