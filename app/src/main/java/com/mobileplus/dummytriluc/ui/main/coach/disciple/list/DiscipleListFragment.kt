package com.mobileplus.dummytriluc.ui.main.coach.disciple.list

import android.os.Bundle
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentDiscipleListBinding
import com.mobileplus.dummytriluc.ui.main.coach.dialog.AddToGroupDialog
import com.mobileplus.dummytriluc.ui.main.coach.dialog.AddToGroupDialog.Companion.TYPE_ADD
import com.mobileplus.dummytriluc.ui.main.coach.dialog.DeleteDiscipleDialog
import com.mobileplus.dummytriluc.ui.main.coach.adapter.DiscipleListAdapter
import com.mobileplus.dummytriluc.ui.main.coach.assign_exercise.CoachAssignExerciseFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.setVisibility
import org.koin.android.viewmodel.ext.android.viewModel

class DiscipleListFragment : BaseFragmentZ<FragmentDiscipleListBinding>() {
    override fun getLayoutBinding(): FragmentDiscipleListBinding =
        FragmentDiscipleListBinding.inflate(layoutInflater)

    private val discipleListAdapter by lazy { DiscipleListAdapter() }
    private val discipleListViewModel by viewModel<DiscipleListViewModel>()
    private val rcv by lazy { binding.rcvDiscipleList }
    private val page by lazy { Page() }
    private var lastPosition: Int = AppConstants.INTEGER_DEFAULT
    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(discipleListViewModel)
        discipleListViewModel.getListDisciple()
        setupView()
        handlerClick()
    }

    private fun callbackViewModel(vm: DiscipleListViewModel) {
        addDispose(vm.rxDisciples.subscribe {
            val items = it.first.toMutableList()
            val pageResponse = it.second
            pageResponse?.run {
                page.currPage = currPage
                page.totalPage = totalPage
                page.isLoading = false
            }
            binding.noDataDiscipleList.root.setVisibility(items.isEmpty())
            if (page.currPage == 1) {
                discipleListAdapter.items = items
            } else {
                discipleListAdapter.items.addAll(items)
            }
        })
        addDispose(vm.isLoading.subscribe {
            if (page.isLoading) {
                binding.loadMoreDiscipleList.root.setVisibility(it)
            } else {
                if (it) rcv.enableRefresh() else rcv.cancelRefresh()
            }
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.rxDeleteDiscipleSuccess.subscribe {
            if (it) discipleListAdapter.delete(lastPosition)
        })
    }

    private fun setupView() {
        rcv.setUpRcv(discipleListAdapter)
        rcv.getRcv().addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
    }

    private fun handlerClick() {
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            discipleListViewModel.getListDisciple()
        }
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    discipleListViewModel.getListDisciple(currPage)
                }
            }
        }
        discipleListAdapter.listener = object : DiscipleListAdapter.DiscipleItemClickListener {
            override fun onAddGroupClick(position: Int, item: ItemDisciple) {
                val dialog =
                    AddToGroupDialog(TYPE_ADD)
                dialog.idGroupIsAdded = item.groupJoined?.map { it.id } ?: emptyList()
                dialog.listener = AddToGroupDialog.AddToGroupDialogListener { arrClass ->
                    item.studentId?.let {
                        discipleListViewModel.moveMemberToGroups(arrClass, it) {
                            discipleListViewModel.getListDisciple()
                        }
                    }
                }
                dialog.show(parentFragmentManager, dialog.tag)
            }

            override fun onAssignExercise(position: Int, item: ItemDisciple) {
                item.studentId?.let { CoachAssignExerciseFragment.openFragmentStudent(it) }
            }

            override fun onDeleteDiscipleClick(position: Int, item: ItemDisciple) {
                lastPosition = position
                deleteGroup(item)
            }
        }
    }

    private fun deleteGroup(item: ItemDisciple) {
        val dialog =
            DeleteDiscipleDialog()
        dialog.setUp(
            item.fullName,
            loadStringRes(R.string.format_delete_disciple_question),
            requireContext(),
            loadStringRes(R.string.label_delete_disciple)
        )
        dialog.listener = DeleteDiscipleDialog.OnDeleteListener {
            item.studentId?.let { discipleListViewModel.deleteDisciple(it) }
        }
        dialog.show(parentFragmentManager, dialog.tag)
    }
}
