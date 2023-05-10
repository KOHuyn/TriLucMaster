package com.mobileplus.dummytriluc.ui.main.coach.session.practitioner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.databinding.FragmentCoachSessionPractitionerBinding
import com.mobileplus.dummytriluc.ui.main.coach.dialog.AddDiscipleDialog
import com.mobileplus.dummytriluc.ui.main.coach.dialog.AddMemberInGroupDialog
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.practitioner.adapter.CoachSessionPractitionerAdapter
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.widget.ItemMoveCallback
import com.mobileplus.dummytriluc.ui.widget.StartDragListener
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by KOHuyn on 4/21/2021
 */
class CoachSessionPractitionerFragment : BaseFragmentZ<FragmentCoachSessionPractitionerBinding>() {

    override fun getLayoutBinding(): FragmentCoachSessionPractitionerBinding =
        FragmentCoachSessionPractitionerBinding.inflate(layoutInflater)

    val adapter by lazy { CoachSessionPractitionerAdapter() }

    private val rcv by lazy { binding.rcvCoachSessionPractitioner }

    var groupId: Int? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        attachRcv()
        handleClick()
    }

    private fun attachRcv() {
        setUpRcv(rcv, adapter)
        rcv.addItemDecoration(MarginItemDecoration(resources.getDimensionPixelOffset(R.dimen.space_8)))
        val callback: ItemTouchHelper.Callback = ItemMoveCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rcv)
        adapter.startDragListener = object : StartDragListener {
            override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper.startDrag(viewHolder)
            }
        }
        adapter.onDeleteChange = CoachSessionPractitionerAdapter.OnDeleteChange {
            Handler(Looper.getMainLooper()).postDelayed({ rcv.requestLayout() }, 500)
        }
        addDispose(
            (parentFragment as CoachSessionFragment).rxState.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val isEnable =
                        it == CoachSessionFragment.STATE_PLAY.PLAY || it == CoachSessionFragment.STATE_PLAY.REPLAY || it == CoachSessionFragment.STATE_PLAY.PREPARE
                    binding.btnAddPractitionerCoachSession.setEnableAlpha(isEnable)
                    binding.btnAddFromGroupCoachSession.setEnableAlpha(isEnable)
                    adapter.isEnableChangeData = isEnable
                })
    }

    private fun View?.setEnableAlpha(isEnableAlpha: Boolean) {
        this?.isEnabled = isEnableAlpha
        this?.alpha = if (isEnableAlpha) 1F else 0.5F
    }

    fun setShowViewControl(isShow: Boolean) {
        binding.btnAddPractitionerCoachSession.setVisibility(isShow)
        binding.btnAddFromGroupCoachSession.setVisibility(isShow)
    }

    private fun handleClick() {
        binding.btnAddPractitionerCoachSession.clickWithDebounce {
            AddDiscipleDialog.Builder()
                .setItemsSelected(adapter.items)
                .build(parentFragmentManager)
                .setItemsCallbackListener { items ->
                    adapter.items = items
                }
        }

        binding.btnAddFromGroupCoachSession.clickWithDebounce {
            AddMemberInGroupDialog.Builder()
                .setGroupSelected(groupId)
                .build(parentFragmentManager)
                .setItemsCallbackListener { idGroup, items ->
                    adapter.items = items
                    groupId = idGroup
                }
        }
    }

}