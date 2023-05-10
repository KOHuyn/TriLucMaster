package com.mobileplus.dummytriluc.ui.main.challenge.create

import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.challenge.create.offline.ChallengeOfflineFreeFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.utils.applyClickShrink
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import kotlinx.android.synthetic.main.fragment_challenge_choose_mode.*

class ChallengeChooseModeFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_challenge_choose_mode

    override fun updateUI(savedInstanceState: Bundle?) {
        rbLessonChallenge.applyClickShrink()
        rbFreeChallenge.applyClickShrink()

        btnBackChooseModeChallenge.clickWithDebounce { onBackPressed() }
        btnNextChallengeMode.clickWithDebounce {
            if (rbLessonChallenge.isChecked || rbFreeChallenge.isChecked) {
                if (rbLessonChallenge.isChecked) {
                    toast(loadStringRes(R.string.feature_developing))
                }
                if (rbFreeChallenge.isChecked) {
                    postNormal(
                        EventNextFragmentMain(ChallengeOfflineFreeFragment::class.java, true)
                    )
                }
            } else {
                toast("Bạn chưa chọn loại thách đấu")
            }
        }
    }
}