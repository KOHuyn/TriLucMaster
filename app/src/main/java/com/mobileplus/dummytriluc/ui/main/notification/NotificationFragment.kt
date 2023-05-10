package com.mobileplus.dummytriluc.ui.main.notification

import android.os.Bundle
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentNotificationBinding
import com.mobileplus.dummytriluc.ui.main.notification.adapter.NotificationAdapter
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventHandleNotification
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.postNormal
import com.utils.ext.register
import com.utils.ext.setVisibility
import com.utils.ext.unregister
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class NotificationFragment : BaseFragmentZ<FragmentNotificationBinding>() {
    override fun getLayoutBinding(): FragmentNotificationBinding =
        FragmentNotificationBinding.inflate(layoutInflater)

    private val vm by viewModel<NotificationViewModel>()
    private val adapter by lazy { NotificationAdapter() }
    private val rcv by lazy { binding.rcvNotification }
    private val page by lazy { Page() }
    private var isReload: Boolean = true
    private var initialized = false

    fun initView() {
        if (!initialized) {
            vm.getListNotification()
            initialized = true
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        disposeViewModel()
        controlView()
        adapter.onItemClick = OnClickItemAdapter { _, position ->
            val item = adapter.items[position]
            postNormal(EventHandleNotification(item))
        }
    }

    private fun disposeViewModel() {
        vm.run {
            addDispose(
                rxMessage.subscribe {
                    toast(it)
                },
                isLoading.subscribe {
                    if (page.isLoading) {
                        binding.loadMoreNotification.root.setVisibility(it)
                    } else {
                        if (it) rcv.enableRefresh() else rcv.cancelRefresh()
                    }
                },
                rxNotifications.subscribe {
                    val items = it.first.toMutableList()
                    val pageResponse = it.second
                    pageResponse?.run {
                        page.currPage = currPage
                        page.totalPage = totalPage
                        page.isLoading = false
                    }
                    binding.noDataNotification.root.setVisibility(items.isEmpty())
                    if (adapter.items.isEmpty() || isReload) {
                        adapter.items = items
                    } else {
                        adapter.items.addAll(items)
                    }
                    rcv.getRcv().requestLayout()
                },
            )
        }
    }

    private fun controlView() {
        rcv.setUpRcv(adapter)
        rcv.getRcv()
            .addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_8).toInt()
                )
            )
        rcv.onCustomSwipeListener =
            CustomSwipeRcv.OnCustomSwipeListener {
                isReload = true
                vm.getListNotification()
            }
        rcv.onLoadMoreListener =
            CustomSwipeRcv.OnLoadMoreListener {
                page.run {
                    isReload = false
                    if (currPage < totalPage && !isLoading) {
                        isLoading = true
                        currPage++
                        vm.getListNotification(currPage)
                    }
                }
            }
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
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        binding.txtHeader.text = loadStringRes(R.string.notification)
    }
}