package com.mobileplus.dummytriluc.ui.main.news.detail

import android.content.Intent
import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.NewsResponse
import com.mobileplus.dummytriluc.ui.main.news.NewsViewModel
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.setHtmlWithImage
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import kotlinx.android.synthetic.main.fragment_news_detail.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by ThaiNV on 1/22/2021.
 */
class NewsDetailFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_news_detail
    private val newsDetailViewModel by viewModel<NewsDetailViewModel>()

    companion object {
        const val ARG_NEWS_ID = "ARG_NEWS_ID"
        fun openFragment(id: Int) {
            val bundle = Bundle().apply {
                putInt(ARG_NEWS_ID, id)
            }
            postNormal(EventNextFragmentMain(NewsDetailFragment::class.java, bundle, true))
        }
    }

    private val idNews by argument(ARG_NEWS_ID, AppConstants.INTEGER_DEFAULT)

    override fun updateUI(savedInstanceState: Bundle?) {
        btnBackNewsDetail.clickWithDebounce { onBackPressed() }
        disposeViewModel()
        newsDetailViewModel.getNewsDetail(idNews)
    }

    private fun disposeViewModel() {
        newsDetailViewModel.run {
            addDispose(
                rxMessage.subscribe {
                    toast(it)
                },
                isLoading.subscribe { if (it) showDialog() else hideDialog() },
                rxNewsResponse.subscribe { responseNews ->
                    txtHeaderNews.setTextNotNull(responseNews.title)
                    val date = if (responseNews.createdAt != null) DateTimeUtil.convertDate(
                        responseNews.createdAt,
                        "yyyy-MM-dd hh:mm:ss",
                        "EEE, dd MMM , yyyy"
                    ) else null
                    txtDateCreatedNews.setTextNotNull(date)
                    responseNews.content?.let { content ->
                        txtContentNews.setHtmlWithImage(content, requireContext())
                    }
                    btnShareNews.clickWithDebounce {
                        val shareIntent = Intent()
                        shareIntent.action = Intent.ACTION_SEND
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, responseNews.title)
                        shareIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "${responseNews.title}\n ${responseNews.linkShare}"
                        )
                        startActivity(Intent.createChooser(shareIntent, "Send to:"))
                    }
                })
        }
    }
}