package com.mobileplus.dummytriluc.ui.main.coach.check

import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.fragment_coach_not_activated.*

class CoachNotActivatedFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_not_activated

    companion object {
        const val TIME_REQUEST_ARG = "TIME_REQUEST_ARG"
    }

    private val timeRequest by argument(TIME_REQUEST_ARG, "")
    override fun updateUI(savedInstanceState: Bundle?) {
        btnBackCoachNotActive.clickWithDebounce { onBackPressed() }
        txtTimeRequestMasterCoach.text = timeRequest
        hotLineCoachNotActivated.text =
            String.format("Hotline: %s", (activity as MainActivity).hotline)
        btnBackToHomeCoachNotActive.clickWithDebounce { (activity as MainActivity).popBackToMain() }
    }
}