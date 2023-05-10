package com.mobileplus.dummytriluc.ui.main.coach.dialog

import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.main.coach.adapter.CoachGroupSimpleAdapter
import com.mobileplus.dummytriluc.ui.main.coach.group.CoachGroupViewModel
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.dialog_coach_add_member_in_group.*
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 5/20/2021
 */
class AddMemberInGroupDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_coach_add_member_in_group
    private val vm by viewModel<CoachGroupViewModel>()
    private val rcv by lazy { rcvAddMemberInGroupDialog }
    private val page by lazy { Page() }

    private val groupListAdapter by lazy { CoachGroupSimpleAdapter() }
    private var idSelected: Int = -1

    private var chooseItemsListener: OnItemsSelectedListener? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel()
        vm.getGroupList(false)
        setupView()
        handleClick()
    }

    class Builder {
        private val dialog by lazy { AddMemberInGroupDialog() }

        fun setGroupSelected(idGroup: Int? = null): Builder {
            if (idGroup != null) {
                dialog.idSelected = idGroup
            }
            return this
        }

        fun build(fm: FragmentManager): AddMemberInGroupDialog {
            dialog.show(fm, AddDiscipleDialog::class.java.simpleName)
            return dialog
        }
    }

    private fun callbackViewModel() {
        addDispose(vm.rxGroups.subscribe {
            val items = it.first.toMutableList()
            val pageResponse = it.second
            pageResponse?.run {
                page.currPage = currPage
                page.totalPage = totalPage
                page.isLoading = false
            }
            noDataAddMemberInGroupDialog.background =
                ResourcesCompat.getDrawable(resources, R.color.clr_background, null)
            noDataAddMemberInGroupDialog.setVisibility(items.isEmpty())

            items.map { item ->
                item.isChecked = item.id == idSelected
            }
            if (page.currPage == 1) {
                groupListAdapter.items = items
            } else {
                groupListAdapter.items.addAll(items)
            }
            rcv.getRcv().requestLayout()
        })
        addDispose(vm.isLoading.subscribe {
            if (page.isLoading) {
                loadMoreAddMemberInGroupDialog.setVisibility(it)
            } else {
                if (it) rcv.enableRefresh() else rcv.cancelRefresh()
            }
        })
        addDispose(vm.rxLoadingMember.subscribe { if (it) showDialog() else hideDialog() })
        addDispose(vm.rxMemberInGroup.subscribe {
            val idGroup = it.first
            val items = it.second
            chooseItemsListener?.setOnItemsSelectedListener(idGroup, items)
            dismiss()
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
    }

    private fun setupView() {
        rcv.setUpRcv(groupListAdapter)
        rcv.getRcv().itemAnimator = null
    }

    private fun handleClick() {
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            vm.getGroupList(false)
        }
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    vm.getGroupList(false, currPage)
                }
            }
        }
        btnAddMemberInGroupDialog.clickWithDebounce {
            val itemSelected = groupListAdapter.getItemSelected()
            if (itemSelected == null) {
                toast("Bạn chưa chọn nhóm nào")
            } else {
                if (itemSelected.id != null) {
                    if (itemSelected.discipleCount == 0) {
                        toast("Nhóm này chưa có thành viên nào!!!")
                    } else {
                        vm.getAllMemberInGroup(itemSelected.id!!)
                    }
                }
            }
        }

        btnSkipAddMemberInGroupGroup.clickWithDebounce {
            dismiss()
        }
    }

    fun setItemsCallbackListener(callback: (idGroup: Int, items: MutableList<ItemDisciple>) -> Unit) {
        chooseItemsListener = OnItemsSelectedListener { idGroup, items ->
            callback(idGroup, items)
        }
    }

    private fun interface OnItemsSelectedListener {
        fun setOnItemsSelectedListener(idParent: Int, items: MutableList<ItemDisciple>)
    }

}