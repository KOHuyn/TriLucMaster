package com.mobileplus.dummytriluc.ui.login.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.core.BaseActivity
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.login.signin.SignInFragment
import com.mobileplus.dummytriluc.ui.main.MainFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.language.LocalManageUtil
import com.utils.ext.register
import com.utils.ext.unregister
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.greenrobot.eventbus.Subscribe

class LoginActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.activity_login

    override fun updateUI(savedInstanceState: Bundle?) {
        openFragment(R.id.loginContainer, LoginFragment::class.java, null, true)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            ViewPumpContextWrapper.wrap(
                LocalManageUtil.setLocal(newBase) ?: newBase
            )
        )
    }

    override fun onStart() {
        super.onStart()
        register(this)
    }

    override fun onStop() {
        super.onStop()
        unregister(this)
    }

    private fun currentFrag() = supportFragmentManager.findFragmentById(R.id.loginContainer)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val curr = currentFrag()
        if (curr is LoginFragment)
            curr.facebookCallback.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun popBackToMainLogin(){
        supportFragmentManager.popBackStack(SignInFragment::class.java.simpleName, 0)
    }

    @Subscribe
    fun nextFragmentLogin(ev: EventNextFragmentLogin) {
        openFragment(
            R.id.loginContainer,
            ev.clazz,
            ev.bundle,
            ev.isAddToBackStack,
            R.anim.slide_from_right,
            R.anim.slide_in_from_right,
            R.anim.slide_out_right,
            R.anim.slide_out_to_right
        )
    }
}