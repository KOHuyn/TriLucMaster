package com.mobileplus.dummytriluc.ui.main.notification.detail

import android.content.Intent
import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.news.detail.NewsDetailFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.setHtmlWithImage
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import kotlinx.android.synthetic.main.fragment_notification_detail.*
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 2/25/2021
 */
class NotificationDetailFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_notification_detail
    private val notificationDetailViewModel by viewModel<NotificationDetailViewModel>()

    companion object {
        private const val ARG_NOTIFICATION_DETAIL_ID = "ARG_NOTIFICATION_DETAIL_ID"
        fun openFragment(id: Int) {
            val bundle = Bundle().apply {
                putInt(ARG_NOTIFICATION_DETAIL_ID, id)
            }
            postNormal(EventNextFragmentMain(NotificationDetailFragment::class.java, bundle, true))
        }
    }

    private val idNotification by argument(ARG_NOTIFICATION_DETAIL_ID, AppConstants.INTEGER_DEFAULT)

    override fun updateUI(savedInstanceState: Bundle?) {
        btnBackNotificationDetail.clickWithDebounce { onBackPressed() }
        disposeViewModel()
        notificationDetailViewModel.getNotificationDetail(idNotification)
    }

    private fun disposeViewModel() {
        notificationDetailViewModel.run {
            addDispose(
                rxMessage.subscribe {
                    toast(it)
                },
                isLoading.subscribe { if (it) showDialog() else hideDialog() },
                rxNotificationDetailResponse.subscribe { response ->
                    txtHeaderNotification.setTextNotNull(response.title)
                    response.content?.let { content ->
                        txtContentNotification.setHtmlWithImage(content, requireContext())
                    }
                })
        }
    }
}