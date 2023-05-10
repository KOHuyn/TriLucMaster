package com.mobileplus.dummytriluc.ui.main.coach.check

import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.MainViewModel
import com.mobileplus.dummytriluc.ui.utils.extensions.fillGradientPrimary
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.fragment_coach_unqualified.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CoachUnqualifiedFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_unqualified
    private val viewModel by sharedViewModel<MainViewModel>()

    companion object {
        const val RANK_MASTER_ARG = "RANK_MASTER_ARG"
    }

    private val rankMaster by argument(RANK_MASTER_ARG, "")

    override fun updateUI(savedInstanceState: Bundle?) {
        txtRankCoachQualified.text = rankMaster ?: "------"
        txtRankCurrentInfo.text = viewModel.user?.level?.title ?: "------"
        btnBackCoachUnqualified.clickWithDebounce { onBackPressed() }
        txtRankCurrentInfo.fillGradientPrimary()
        txtRankCoachQualified.fillGradientPrimary()
    }
}