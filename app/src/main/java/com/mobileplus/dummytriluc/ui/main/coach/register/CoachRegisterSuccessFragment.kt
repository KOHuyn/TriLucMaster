package com.mobileplus.dummytriluc.ui.main.coach.register

import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.fragment_coach_register_success.*

class CoachRegisterSuccessFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_register_success

    override fun updateUI(savedInstanceState: Bundle?) {
        hotLineCoachRegisterSuccess.text = String.format("Hotline: %s",(activity as MainActivity).hotline)
        btnBackCoachRegisterSuccess.clickWithDebounce { onBackPressed() }
        btnBackToHomeCoachSuccess.clickWithDebounce {
            (activity as MainActivity).popBackToMain()
        }
    }
}