package com.mobileplus.dummytriluc.ui.main.practice.detail.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.data.model.ItemPracticeDetailMain
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.LayoutPracticeDetailMainBottomBinding
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailViewModel
import com.mobileplus.dummytriluc.ui.main.practice.detail.main.adapter.PracticeDetailMainAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.setVisibility
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PracticeDetailMainBottomFragment : BaseFragmentZ<LayoutPracticeDetailMainBottomBinding>() {
    override fun getLayoutBinding(): LayoutPracticeDetailMainBottomBinding =
        LayoutPracticeDetailMainBottomBinding.inflate(layoutInflater)

    private val vm by sharedViewModel<PracticeDetailViewModel>()
    private val adapterPractice by lazy { PracticeDetailMainAdapter() }
    private var isReload = true
    private val page by lazy { Page() }
    private val rcv by lazy { binding.rcvPracticeDetailMain }
    private var idPractice: Int? = null

    var callbackToTopMain: CallbackToTopMain? = null
    var onEmptyPracticesListener: OnEmptyPracticesListener? = null


    fun setDataMainBottom(idPractice: Int) {
        this.idPractice = idPractice
    }

    fun reloadDetailMain() {
        Handler(Looper.getMainLooper()).postDelayed({
            idPractice?.let { vm.getResultPractices(it) }
        }, 1000)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(vm)
        idPractice?.let { vm.getResultPractices(it) }
        rcv.setUpRcv(adapterPractice)
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    idPractice?.let { vm.getResultPractices(it, currPage) }
                }
            }
        }
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            idPractice?.let { vm.getResultPractices(it) }
        }
        adapterPractice.onClickItem = OnClickItemAdapter { _, position ->
            callbackToTopMain?.setOnCallbackTopMain(adapterPractice.items[position])
        }
    }

    private fun callbackViewModel(vm: PracticeDetailViewModel) {
        addDispose(vm.rxResultPractices.subscribe {
            val items = it.first.toMutableList()
            val pageResponse = it.second
            pageResponse?.run {
                page.totalPage = totalPage
                page.currPage = currPage
                page.isLoading = false
            }
            binding. noDataPracticeDetailMain.root.setVisibility(items.isEmpty())
            onEmptyPracticesListener?.setOnEmptyPracticesListener(items.isEmpty())
            if (adapterPractice.items.isEmpty() || isReload) {
                adapterPractice.items = items
            } else {
                adapterPractice.items.addAll(items)
            }
            rcv.getRcv().requestLayout()
            if (isReload) {
                if (adapterPractice.items.isNotEmpty()) {
                    adapterPractice.setOnSelectedItem(0)
                    callbackToTopMain?.setOnCallbackTopMain(adapterPractice.items[0])
                }
            }
        },
            vm.rxLoadingResultPractice.subscribe {
                if (page.isLoading) {
                    binding.loadMoreDetailMainBottom.root.setVisibility(it)
                } else {
                    if (it) rcv.enableRefresh() else rcv.cancelRefresh()
                }
            })
    }

    fun interface CallbackToTopMain {
        fun setOnCallbackTopMain(data: ItemPracticeDetailMain)
    }

    fun interface OnEmptyPracticesListener {
        fun setOnEmptyPracticesListener(isEmpty: Boolean)
    }


}