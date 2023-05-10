package com.mobileplus.dummytriluc.ui.main.coach.session.exercise

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.session.CoachSessionSavedListRequest
import com.mobileplus.dummytriluc.databinding.FragmentCoachSessionExerciseBinding
import com.mobileplus.dummytriluc.ui.dialog.RenameDialog
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionViewModel
import com.mobileplus.dummytriluc.ui.main.coach.session.exercise.adapter.CoachSessionExerciseAdapter
import com.mobileplus.dummytriluc.ui.main.coach.session.folder.CoachSessionChooseFolderFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.saved_list.CoachSessionSavedListFragment
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPostCoachSessionItemSavedList
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPostExerciseSession
import com.mobileplus.dummytriluc.ui.widget.ItemMoveCallback
import com.mobileplus.dummytriluc.ui.widget.StartDragListener
import com.utils.ext.clickWithDebounce
import com.utils.ext.register
import com.utils.ext.setVisibility
import com.utils.ext.unregister
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.util.*

/**
 * Created by KOHuyn on 4/21/2021
 */
class CoachSessionExerciseFragment : BaseFragmentZ<FragmentCoachSessionExerciseBinding>() {
    override fun getLayoutBinding(): FragmentCoachSessionExerciseBinding =
        FragmentCoachSessionExerciseBinding.inflate(layoutInflater)

    private val vm by sharedViewModel<CoachSessionViewModel>()

    val adapter by lazy { CoachSessionExerciseAdapter() }

    private val rcv by lazy { binding.rcvCoachSessionExercise }

    private val gson by inject<Gson>()

    private var isEnableSaveList: Boolean = false
        set(value) {
            field = value
            runOnUiThread {
                binding.btnSaveExerciseCoachSession.isEnabled = field
                binding.btnSaveExerciseCoachSession.alpha = if (field) 1.0F else 0.5F
            }
        }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        attachRcv()
        handleClick()
    }

    private fun attachRcv() {
        setUpRcv(rcv, adapter)
        isEnableSaveList = adapter.items.isNotEmpty()
        rcv.addItemDecoration(MarginItemDecoration(resources.getDimensionPixelOffset(R.dimen.space_8)))
        val callback: ItemTouchHelper.Callback = ItemMoveCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rcv)
        adapter.startDragListener = object : StartDragListener {
            override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper.startDrag(viewHolder)
            }
        }
        adapter.onDeleteChange = CoachSessionExerciseAdapter.OnDeleteChange {
            Handler(Looper.getMainLooper()).postDelayed({ rcv.requestLayout() }, 500)
        }
    }

    private fun handleClick() {
        binding.btnAddExerciseCoachSession.clickWithDebounce {
            CoachSessionChooseFolderFragment.openFragmentFolder(
                CoachSessionChooseFolderFragment.DataFolderArg(null, adapter.items), gson
            )
        }
        binding.btnAddExerciseSavedCoachSession.clickWithDebounce { CoachSessionSavedListFragment.openFragmentChoicePractice() }
        addDispose(adapter.onValueChangeListener.subscribe { isNotEmpty ->
            isEnableSaveList = isNotEmpty
        })
        binding.btnSaveExerciseCoachSession.clickWithDebounce {
            RenameDialog.builder()
                .setTitle(getString(R.string.create_name_for_list))
                .setDescription(getString(R.string.label_new_name))
                .setRenameOld(
                    String.format(
                        getString(R.string.label_for_new_list_default),
                        DateTimeUtil.convertTimeStampToDate(
                            Calendar.getInstance().time.time / 1000,
                            "dd/MM hh:mm"
                        )
                    )
                )
                .build(parentFragmentManager)
                .setListenerCallback { title ->
                    val idsRoundPractice = adapter.getIdsRound()
                        .map { CoachSessionSavedListRequest.PracticeIdsRound(it.first, it.second) }
                    vm.savedPracticeList(CoachSessionSavedListRequest(title, idsRoundPractice))
                }
        }
    }

    private fun disposableViewModel() {
        addDispose(vm.isLoading.subscribe { if (it) showDialog() else hideDialog() })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        val parentView = (parentFragment as CoachSessionFragment)
        addDispose(parentView.rxState.observeOn(AndroidSchedulers.mainThread()).subscribe {
            val isEnable =
                it == CoachSessionFragment.STATE_PLAY.PLAY || it == CoachSessionFragment.STATE_PLAY.REPLAY || it == CoachSessionFragment.STATE_PLAY.PREPARE
            binding.btnAddExerciseSavedCoachSession.setEnableAlpha(isEnable)
            binding.btnAddExerciseCoachSession.setEnableAlpha(isEnable)
            binding.btnSaveExerciseCoachSession.setEnableAlpha(isEnable)
            adapter.isEnableChangeData = isEnable
        })
    }

    fun setShowViewControl(isShow: Boolean) {
        binding.btnAddExerciseSavedCoachSession.setVisibility(isShow)
        binding.btnAddExerciseCoachSession.setVisibility(isShow)
        binding.btnSaveExerciseCoachSession.setVisibility(isShow)
    }

    fun View?.setEnableAlpha(isEnableAlpha: Boolean) {
        this?.isEnabled = isEnableAlpha
        this?.alpha = if (isEnableAlpha) 1F else 0.5F
    }

    override fun onStart() {
        super.onStart()
        register(this)
    }

    override fun onStop() {
        super.onStop()
        unregister(this)
    }

    @Subscribe
    fun getAllExercise(ev: EventPostExerciseSession) {
        adapter.items = ev.items.toMutableList()
    }

    @Subscribe
    fun getItemSavedList(ev: EventPostCoachSessionItemSavedList) {
        adapter.items = ev.items
    }
}