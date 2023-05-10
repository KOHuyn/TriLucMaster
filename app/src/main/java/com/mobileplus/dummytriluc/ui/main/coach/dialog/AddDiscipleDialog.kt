package com.mobileplus.dummytriluc.ui.main.coach.dialog

import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.main.coach.adapter.AddDiscipleAdapter
import com.mobileplus.dummytriluc.ui.main.coach.disciple.list.DiscipleListViewModel
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.dialog_add_disciple.*
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 3/15/2021
 */
class AddDiscipleDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_add_disciple

    private val vm by viewModel<DiscipleListViewModel>()
    private val rcv by lazy { rcvDiscipleDialog }
    private val page by lazy { Page() }
    private val discipleListAdapter by lazy { AddDiscipleAdapter() }
    private var itemsSelected = mutableListOf<ItemDisciple>()
    private var isReload = true

    private var chooseIdsListener: OnIdsSelectedListener? = null
    private var chooseItemsListener: OnItemsSelectedListener? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(vm)
        vm.getListDisciple()
        setupView()
        handleClick()
    }

    class Builder {
        private val dialog by lazy { AddDiscipleDialog() }

        fun setItemsSelected(items: MutableList<ItemDisciple>): Builder {
            dialog.itemsSelected = items
            return this
        }

        fun build(fm: FragmentManager): AddDiscipleDialog {
            dialog.show(fm, AddDiscipleDialog::class.java.simpleName)
            return dialog
        }
    }


    fun setItemsCallbackListener(callback: (items: MutableList<ItemDisciple>) -> Unit) {
        chooseItemsListener = OnItemsSelectedListener { callback(it) }
    }

    fun setIdsCallbackListener(callback: (ids: List<Int>) -> Unit) {
        chooseIdsListener = OnIdsSelectedListener { callback(it) }
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
            noDataDiscipleDialog.background =
                ResourcesCompat.getDrawable(resources, R.color.clr_background, null)
            noDataDiscipleDialog.setVisibility(items.isEmpty())
//            rcvDiscipleDialog.setVisibility(items.isNotEmpty())

            val idsSelected = itemsSelected.mapNotNull { item -> item.studentId }
            items.map { item ->
                item.isSelected = idsSelected.contains(item.studentId)
            }

            if (discipleListAdapter.items.isEmpty() || isReload) {
                discipleListAdapter.items = items
            } else {
                discipleListAdapter.items.addAll(items)
            }
        })
        addDispose(vm.isLoading.subscribe {
            if (page.isLoading) {
                loadMoreAddDiscipleDialog.setVisibility(it)
            } else {
                if (it) rcv.enableRefresh() else rcv.cancelRefresh()
            }
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
    }

    private fun setupView() {
        rcv.setUpRcv(discipleListAdapter)
        rcv.getRcv().itemAnimator = null
    }

    private fun handleClick() {
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            vm.getListDisciple()
        }
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    vm.getListDisciple(currPage)
                }
            }
        }
        btnAddDiscipleDialog.clickWithDebounce {
            if (discipleListAdapter.getIdsSelected().isEmpty()) {
                toast(getString(R.string.you_have_not_selected_any_members_yet))
            } else {
                chooseIdsListener?.setOnIdsSelectedListener(discipleListAdapter.getIdsSelected())
                chooseItemsListener?.setOnItemsSelectedListener(itemsSelected)
                dismiss()
            }
        }

        discipleListAdapter.itemChangeListener =
            AddDiscipleAdapter.OnItemChangeListener { item, isSelected ->
                val itemFind = itemsSelected.find { it.studentId == item.studentId }
                if (isSelected) {
                    itemsSelected.add(item)
                } else {
                    itemsSelected.remove(itemFind)
                }
            }

        btnSkipDiscipleGroup.clickWithDebounce {
            dismiss()
        }
    }

    private fun interface OnIdsSelectedListener {
        fun setOnIdsSelectedListener(ids: List<Int>)
    }

    private fun interface OnItemsSelectedListener {
        fun setOnItemsSelectedListener(items: MutableList<ItemDisciple>)
    }

}