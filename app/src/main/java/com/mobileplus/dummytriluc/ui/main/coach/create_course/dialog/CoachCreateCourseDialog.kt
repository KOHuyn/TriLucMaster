package com.mobileplus.dummytriluc.ui.main.coach.create_course.dialog

import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.main.coach.create_course.CoachCreateCourseViewModel
import com.mobileplus.dummytriluc.ui.main.coach.create_course.adapter.CoachExerciseSelectAdapter
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.dialog_add_exercise_to_course.*
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 4/5/2021
 */
class CoachCreateCourseDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_add_exercise_to_course

    private val vm by viewModel<CoachCreateCourseViewModel>()
    private val rcv by lazy { rcvExerciseAddToCourseDialog }
    private val page by lazy { Page() }
    private val adapterExercise by lazy { CoachExerciseSelectAdapter() }
    private var isReload = true

    private var listener: OnExerciseSelectedListener? = null
    private var idsSelected = emptyList<Int>()
    private var idRemove: Int = -1
    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel()
        vm.getExercises()
        setupView()
        handleClick()
    }

    private fun callbackViewModel() {
        addDispose(vm.rxExercises.subscribe {
            val items: MutableList<ItemCoachPractice> = it.first.toMutableList()
            val pageResponse = it.second
            pageResponse?.run {
                page.currPage = currPage
                page.totalPage = totalPage
                page.isLoading = false
            }
            noDataExerciseAddToCourseDialog.background =
                ResourcesCompat.getDrawable(resources, R.color.clr_background, null)
            noDataExerciseAddToCourseDialog.setVisibility(items.isEmpty())
            if (idRemove != -1) {
                items.find { item -> item.id == idRemove }
                    ?.let { itemRemove -> items.remove(itemRemove) }
            }
            if (idsSelected.isNotEmpty()) {
                items.map { item ->
                    item.isSelected = idsSelected.contains(item.id)
                    item
                }
            }
            if (adapterExercise.items.isEmpty() || isReload) {
                adapterExercise.items = items
            } else {
                adapterExercise.items.addAll(items)
            }
        })
        addDispose(vm.isLoading.subscribe {
            if (page.isLoading) {
                loadMoreExerciseAddToCourseDialog.setVisibility(it)
            } else {
                if (it) rcv.enableRefresh() else rcv.cancelRefresh()
            }
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
    }

    private fun setupView() {
        rcv.setUpRcv(adapterExercise)
    }

    private fun handleClick() {
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            vm.getExercises()
        }
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    vm.getExercises(currPage)
                }
            }
        }
        btnAddExerciseAddToCourseDialog.clickWithDebounce {
            if (adapterExercise.items.isEmpty()) {
                toast("Bạn chưa chọn bài tập nào")
            } else {
                dismiss()
                listener?.setOnExerciseSelectedListener(adapterExercise.getItemsSelected())
            }
        }

        btnSkipExerciseAddToCourseGroup.clickWithDebounce {
            dismiss()
        }
    }

    fun setIdsSelected(ids: List<Int>): CoachCreateCourseDialog {
        idsSelected = ids
        return this
    }

    fun setMissingId(id: Int): CoachCreateCourseDialog {
        idRemove = id
        return this
    }

    fun build(fm: FragmentManager): CoachCreateCourseDialog {
        show(fm, this::class.java.simpleName)
        return this
    }

    fun setOnCallbackExerciseListener(callback: (List<ItemCoachPractice>) -> Unit) {
        listener = OnExerciseSelectedListener(callback)
    }

    private fun interface OnExerciseSelectedListener {
        fun setOnExerciseSelectedListener(items: List<ItemCoachPractice>)
    }
}