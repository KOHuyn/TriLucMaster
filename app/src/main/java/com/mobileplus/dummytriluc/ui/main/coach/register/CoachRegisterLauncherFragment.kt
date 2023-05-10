package com.mobileplus.dummytriluc.ui.main.coach.register

import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.coach.register.mainregister.CoachRegisterFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.popBackStackDelay
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.register
import com.utils.ext.unregister
import kotlinx.android.synthetic.main.fragment_coach_register_launcher.*
import org.greenrobot.eventbus.Subscribe

class CoachRegisterLauncherFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_register_launcher

    override fun updateUI(savedInstanceState: Bundle?) {
        btnBackCoachRegisterLauncher.clickWithDebounce { onBackPressed() }
        btnRegisterCoachLauncher.clickWithDebounce {
            postNormal(
                EventNextFragmentMain(CoachRegisterFragment::class.java)
            )
        }
    }
}