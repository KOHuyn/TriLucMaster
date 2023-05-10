package com.mobileplus.dummytriluc.ui.main.coach.dialog

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.main.coach.adapter.ModifyDiscipleGroupAdapter
import com.mobileplus.dummytriluc.ui.main.coach.group.CoachGroupViewModel
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.dialog_add_group.*
import org.koin.android.viewmodel.ext.android.viewModel

class AddToGroupDialog(private val type: Int = TYPE_ADD) : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_add_group
    private val groupViewModel by viewModel<CoachGroupViewModel>()
    private val groupAdapter by lazy { ModifyDiscipleGroupAdapter() }
    private val page by lazy { Page() }
    private var isReload: Boolean = true
    var listener: AddToGroupDialogListener? = null
    private val rcv by lazy { rcvGroupDialog }

    var idGroupIsAdded: List<Int?> = emptyList()

    companion object {
        const val TYPE_MOVE = 1
        const val TYPE_ADD = 2
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        viewModelCallback(groupViewModel)
        groupViewModel.getGroupList(isGuest = false)
        setupView()
        when (type) {
            TYPE_ADD -> {
                txtTitleAddGroupDialog?.text = loadStringRes(R.string.title_add_to_group)
            }
            TYPE_MOVE -> {
                txtTitleAddGroupDialog?.text = loadStringRes(R.string.title_move_to_group)
            }
        }

        rcv.getRcv().run {
            adapter = groupAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }

        imgCancelAddGroupDialog?.clickWithDebounce {
            dismiss()
        }
        btnAddToGroupDialog?.clickWithDebounce {
            if (groupAdapter.getArrGroupID().isNotEmpty()) {
                listener?.onAddClick(groupAdapter.getArrGroupID())
                dismiss()
            } else {
                toast("Bạn chưa chọn nhóm nào")
            }
        }
    }

    private fun setupView() {
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            groupViewModel.getGroupList(isGuest = false)
        }

        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    groupViewModel.getGroupList(false,currPage)
                }
            }
        }
    }

    private fun viewModelCallback(vm: CoachGroupViewModel) {
        addDispose(
            vm.rxMessage.subscribe {
                toast(it)
            },
            vm.isLoading.subscribe {
                if (page.isLoading) {
                    loadMoreAddGroupDialog.setVisibility(it)
                } else {
                    if (it) rcv.enableRefresh() else rcv.cancelRefresh()
                }
            },
            vm.rxGroups.subscribe {
                val items = it.first.toMutableList()
                val pageResponse = it.second
                pageResponse?.run {
                    page.totalPage = totalPage
                    page.currPage = currPage
                    page.isLoading = false
                }
                noDataGroupDialog.setVisibility(items.isEmpty())
                if (groupAdapter.items.isEmpty() || isReload) {
                    groupAdapter.items = items
                } else {
                    groupAdapter.items.addAll(items)
                }
                groupAdapter.checkGroupAdded(idGroupIsAdded)
            })
    }

    fun interface AddToGroupDialogListener {
        fun onAddClick(arrGroup: List<Int>)
    }

}