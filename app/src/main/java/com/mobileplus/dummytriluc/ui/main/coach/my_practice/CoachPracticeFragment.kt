package com.mobileplus.dummytriluc.ui.main.coach.my_practice

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.BasePracticeItem
import com.mobileplus.dummytriluc.data.model.ItemPracticeItemContent
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.dialog.MenuOptionsDialog
import com.mobileplus.dummytriluc.ui.main.coach.create_course.CoachCreateCourseFragment
import com.mobileplus.dummytriluc.ui.main.editor_exercise.editor.EditVideoFragment
import com.mobileplus.dummytriluc.ui.main.practice.adapter.PracticeMoreAdapter
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.folder.PracticeFolderFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.fragment_coach_practice.*
import org.koin.android.viewmodel.ext.android.viewModel


/**
 * Created by KO Huyn on 12/17/2020.
 */
class CoachPracticeFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_practice
    private val coachPracticeViewModel by viewModel<CoachPracticeViewModel>()
    private val adapterCoach by lazy { PracticeMoreAdapter() }
    private val rcv by lazy { rcvCoachMore }
    private val idGuest by argument(KEY_GUEST_ID, AppConstants.INTEGER_DEFAULT)
    private val page by lazy { Page() }
    private var isReload = true

    companion object {
        private const val KEY_GUEST_ID = "KEY_GUEST_ID"
        fun openFragment() {
            postNormal(EventNextFragmentMain(CoachPracticeFragment::class.java, true))
        }

        fun openFragmentGuest(idGuest: Int) {
            val bundle = Bundle().apply {
                putInt(KEY_GUEST_ID, idGuest)
            }
            postNormal(EventNextFragmentMain(CoachPracticeFragment::class.java, bundle, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel()
        txtHeaderCoachMore.text = if (arguments?.containsKey(KEY_GUEST_ID) == true) {
            loadStringRes(R.string.exercise)
        } else {
            loadStringRes(R.string.my_exercise)
        }
        if (arguments?.containsKey(KEY_GUEST_ID) == true) {
            coachPracticeViewModel.getGuestById(idGuest)
        } else {
            coachPracticeViewModel.getTrainerList()
        }
        btnBackCoachMore.clickWithDebounce { onBackPressed() }
        configRcv()
    }

    private fun configRcv() {
        rcv.getRcv().run {
            adapter = adapterCoach
            layoutManager = GridLayoutManager(requireContext(), 3)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_4).toInt(),
                    isGridLayout = true,
                    spanCount = 3
                )
            )
        }
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            if (arguments?.containsKey(KEY_GUEST_ID) == true) {
                coachPracticeViewModel.getGuestById(idGuest)
            } else {
                coachPracticeViewModel.getTrainerList()
            }
        }

        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    if (arguments?.containsKey(KEY_GUEST_ID) == true) {
                        coachPracticeViewModel.getGuestById(idGuest, currPage)
                    } else {
                        coachPracticeViewModel.getTrainerList(currPage)
                    }
                }
            }
        }

        adapterCoach.clickItem = PracticeMoreAdapter.OnClickPracticeFilter { item, type ->
            when (type) {
                PracticeMoreAdapter.TYPE_SINGLE -> {
                    PracticeDetailFragment.openFragment(item.id)
                }
                PracticeMoreAdapter.TYPE_MULTI -> {
                    PracticeFolderFragment.openFragment(item.id)
                }
            }
        }
        adapterCoach.longClickItem =
            PracticeMoreAdapter.OnLongClickPracticeFilter { item, viewType, pos ->
                when (viewType) {
                    PracticeMoreAdapter.TYPE_SINGLE -> {
                        MenuOptionsDialog.builder()
                            .addMenuOption(
                                R.drawable.ic_pen_edit,
                                getString(R.string.edit_exercise),
                                MenuOptionsDialog.MenuOption.EDITOR_EXERCISE
                            ).addMenuOption(
                                R.drawable.ic_video_delete,
                                getString(R.string.delete_exercise),
                                MenuOptionsDialog.MenuOption.DELETE
                            )
                            .build(parentFragmentManager)
                            .setOnCallbackListener { type ->
                                if (type == MenuOptionsDialog.MenuOption.EDITOR_EXERCISE) {
                                    EditVideoFragment.openFragment(item.id, true)
                                }
                                if (type == MenuOptionsDialog.MenuOption.DELETE) {
                                    coachPracticeViewModel.deletePractice(item.id, pos)
                                }
                            }
                    }
                    PracticeMoreAdapter.TYPE_MULTI -> {
                        MenuOptionsDialog.builder()
                            .addMenuOption(
                                R.drawable.ic_pen_edit,
                                getString(R.string.edit_course),
                                MenuOptionsDialog.MenuOption.EDITOR_FOLDER
                            ).addMenuOption(
                                R.drawable.ic_video_delete,
                                getString(R.string.delete_course),
                                MenuOptionsDialog.MenuOption.DELETE
                            )
                            .build(parentFragmentManager)
                            .setOnCallbackListener { type ->
                                if (type == MenuOptionsDialog.MenuOption.EDITOR_FOLDER) {
                                    CoachCreateCourseFragment.openFragmentEditor(item.id)
                                }
                                if (type == MenuOptionsDialog.MenuOption.DELETE) {
                                    coachPracticeViewModel.deletePractice(item.id, pos)
                                }
                            }
                    }
                }
            }
    }

    private fun callbackViewModel() {
        coachPracticeViewModel.apply {
            addDispose(rxListCoachMore.subscribe {
                val items = it.first.toMutableList()
                val pageResponse = it.second
                pageResponse?.run {
                    page.currPage = currPage
                    page.totalPage = totalPage
                    page.isLoading = false
                }
                noDataCoachMore.setVisibility(items.isEmpty())
                if (adapterCoach.items.isEmpty() || isReload)
                    adapterCoach.items = items
                else
                    adapterCoach.items.addAll(items)
            })
            addDispose(isLoading.subscribe {
                if (page.isLoading) {
                    loadMoreCoach.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
                rcv.getRcv().requestLayout()
            })
            addDispose(rxMessage.subscribe { toast(it) })
            addDispose(rxDeleteAt.subscribe {
                val pos = it.first
                val isSuccess = it.second
                if (isSuccess) {
                    adapterCoach.delete(pos)
                }
            })
        }
    }

    private fun enableRefresh() = rcv.enableRefresh()
    private fun cancelRefresh() = rcv.cancelRefresh()

}