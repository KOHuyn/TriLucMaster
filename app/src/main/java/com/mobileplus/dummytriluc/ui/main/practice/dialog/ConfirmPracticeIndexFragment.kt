package com.mobileplus.dummytriluc.ui.main.practice.dialog

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.databinding.FragmentConfirmPracticeIndexBinding
import com.mobileplus.dummytriluc.ui.main.practice.dialog.adapter.AdapterIndexPractice
import com.mobileplus.dummytriluc.ui.main.practice.dialog.adapter.BaseItemIndexPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.hide
import kotlinx.android.synthetic.main.fragment_confirm_practice_index.*


/**
 * Created by KOHuyn on 3/2/2021
 */
class ConfirmPracticeIndexFragment : BaseFragmentZ<FragmentConfirmPracticeIndexBinding>() {
    override fun getLayoutBinding(): FragmentConfirmPracticeIndexBinding =
        FragmentConfirmPracticeIndexBinding.inflate(layoutInflater)

    private val adapter by lazy { AdapterIndexPractice() }

    private var isCreateItems = false
    var items: MutableList<BaseItemIndexPractice> = mutableListOf()
        set(value) {
            field = value
            if (this.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                updateItems(value)
                logErr("CREATED")
            } else {
                isCreateItems = true
                logErr("NOT CREATE")
            }
        }

    override fun updateUI(savedInstanceState: Bundle?) {
        setUpRcv(rcvIndexPractice, adapter)
        if (isCreateItems) {
            updateItems(items)
        }
    }

    private fun updateItems(items: MutableList<BaseItemIndexPractice>) {
        runOnUiThread {
            try {
                adapter.items = items
                binding.layoutLoading.root.hide()
            } catch (e: Exception) {
                e.logErr()
            }
        }
    }
}