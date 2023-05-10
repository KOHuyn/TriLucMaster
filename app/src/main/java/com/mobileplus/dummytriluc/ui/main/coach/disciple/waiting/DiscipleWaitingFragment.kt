package com.mobileplus.dummytriluc.ui.main.coach.disciple.waiting

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDiscipleWaiting
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.main.coach.dialog.RequestDiscipleDialog
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.coach.adapter.DiscipleWaitingAdapter
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.fragment_disciple_waiting.*
import org.koin.android.viewmodel.ext.android.viewModel

class DiscipleWaitingFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_disciple_waiting
    private val waitingDiscipleAdapter by lazy { DiscipleWaitingAdapter() }
    private val discipleWaitingViewModel by viewModel<DiscipleWaitingViewModel>()
    private val page by lazy { Page() }
    private val rcv by lazy { rcvDiscipleWaiting }
    private var isReload = true
    private var lastPosition: Int = AppConstants.INTEGER_DEFAULT
    private var initialized = false

    fun initView() {
        if (!initialized) {
            discipleWaitingViewModel.getListWaiting()
            initialized = true
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(discipleWaitingViewModel)
        setupView()
        initAction()
    }

    private fun setupView() {
        rcv.setUpRcv(waitingDiscipleAdapter)
        rcv.getRcv().addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
    }

    private fun callbackViewModel(vm: DiscipleWaitingViewModel) {
        addDispose(vm.rxListWaiting.subscribe {
            val items = it.first.toMutableList()
            val pageResponse = it.second
            pageResponse?.run {
                page.currPage = currPage
                page.totalPage = totalPage
                page.isLoading = false
            }
            noDataDiscipleWaiting.setVisibility(items.isEmpty())
            if (waitingDiscipleAdapter.items.isEmpty() || isReload) {
                waitingDiscipleAdapter.items = items
            } else {
                waitingDiscipleAdapter.items.addAll(items)
            }
        })
        addDispose(vm.isLoading.subscribe {
            if (page.isLoading) {
                loadMoreDiscipleWaiting.setVisibility(it)
            } else {
                if (it) rcv.enableRefresh() else rcv.cancelRefresh()
            }
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.rxRequest.subscribe { isSuccess ->
            if (isSuccess) {
                if (lastPosition != AppConstants.INTEGER_DEFAULT)
                    waitingDiscipleAdapter.delete(lastPosition)
            }
        })
    }

    private fun initAction() {
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            discipleWaitingViewModel.getListWaiting()
        }
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    discipleWaitingViewModel.getListWaiting(currPage)
                }
            }
        }

        waitingDiscipleAdapter.listener =
            object : DiscipleWaitingAdapter.WaitingDiscipleClickListener {
                override fun onRejectClick(position: Int, item: ItemDiscipleWaiting) {
                    YesNoButtonDialog()
                        .setMessage(getString(R.string.do_you_want_cancel_request_from_disciple))
                        .setTextAccept(getString(R.string.yes))
                        .setTextCancel(getString(R.string.no))
                        .showDialog(parentFragmentManager, YesNoButtonDialog.TAG)
                        .setOnCallbackAcceptButtonListener {
                            lastPosition = position
                            item.studentId?.let { discipleWaitingViewModel.rejectDisciple(it) }
                        }
                }

                override fun onAcceptClick(position: Int, item: ItemDiscipleWaiting) {
                    lastPosition = position
                    item.studentId?.let { discipleWaitingViewModel.acceptDisciple(it) }
                }

                override fun onDetailClick(position: Int, item: ItemDiscipleWaiting) {
                    val dialog =
                        RequestDiscipleDialog(item)
                    dialog.show(parentFragmentManager, dialog.tag)
                    dialog.listener = object : RequestDiscipleDialog.AcceptRejectListener {
                        override fun onAcceptClickListener(dialog: DialogFragment) {
                            lastPosition = position
                            item.studentId?.let { discipleWaitingViewModel.acceptDisciple(it) }
                        }

                        override fun onRejectClickListener(dialog: DialogFragment) {
                            YesNoButtonDialog()
                                .setMessage(getString(R.string.do_you_want_cancel_request_from_disciple))
                                .setTextAccept(getString(R.string.yes))
                                .setTextCancel(getString(R.string.no))
                                .showDialog(parentFragmentManager, YesNoButtonDialog.TAG)
                                .setOnCallbackAcceptButtonListener {
                                    lastPosition = position
                                    item.studentId?.let { discipleWaitingViewModel.rejectDisciple(it) }
                                }
                        }
                    }
                }
            }
    }
}