package com.mobileplus.dummytriluc.ui.main.practice.video

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.databinding.FragmentPracticeWithVideoCountdownBinding

/**
 * Created by KO Huyn on 05/07/2023.
 */
class PracticeCountDownFragment : BaseFragmentZ<FragmentPracticeWithVideoCountdownBinding>() {
    override fun getLayoutBinding(): FragmentPracticeWithVideoCountdownBinding {
        return FragmentPracticeWithVideoCountdownBinding.inflate(layoutInflater)
    }

    override fun updateUI(savedInstanceState: Bundle?) {

    }

    fun updateCounter(textCounter: String) {
        lifecycleScope.launchWhenStarted {
            binding.tvTimeCounter.text = textCounter
        }
    }
}