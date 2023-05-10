package com.mobileplus.dummytriluc.ui.main.news

import android.os.Bundle
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentNewsBinding
import com.mobileplus.dummytriluc.ui.main.challenge.detail.ChallengeDetailFragment
import com.mobileplus.dummytriluc.ui.main.news.adapter.NewsFeedAdapter
import com.mobileplus.dummytriluc.ui.main.news.detail.NewsDetailFragment
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.register
import com.utils.ext.setVisibility
import com.utils.ext.unregister
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class NewsFragment : BaseFragmentZ<FragmentNewsBinding>() {
    override fun getLayoutBinding(): FragmentNewsBinding =
        FragmentNewsBinding.inflate(layoutInflater)

    private val vm by viewModel<NewsViewModel>()
    private val adapter by lazy { NewsFeedAdapter() }
    private val rcv by lazy { binding.rcvListNews }
    private val page by lazy { Page() }
    private var isReload: Boolean = true
    private var initialized = false

    fun initView() {
        if (!initialized) {
            vm.getNewsFeed()
            initialized = true
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        disposeViewModel()
        controlView()
    }

    private fun disposeViewModel() {
        vm.run {
            addDispose(
                rxMessage.subscribe {
                    toast(it)
                },
                isLoading.subscribe {
                    if (page.isLoading) {
                        binding.loadMoreNews.root.setVisibility(it)
                    } else {
                        if (it) rcv.enableRefresh() else rcv.cancelRefresh()
                    }
                },
                rxNewsFeed.subscribe {
                    val items = it.first.toMutableList()
                    val pageResponse = it.second
                    pageResponse?.run {
                        page.currPage = currPage
                        page.totalPage = totalPage
                        page.isLoading = false
                    }
//                    rcv.setVisibility(items.isNotEmpty())
                    if (adapter.items.isEmpty() || isReload) {
                        binding.noDataNews.root.setVisibility(items.isEmpty())
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
                vm.getNewsFeed()
            }
        rcv.onLoadMoreListener =
            CustomSwipeRcv.OnLoadMoreListener {
                page.run {
                    isReload = false
                    if (currPage < totalPage && !isLoading) {
                        isLoading = true
                        currPage++
                        vm.getNewsFeed(currPage)
                    }
                }
            }

        adapter.listener = NewsFeedAdapter.NewFeedAdapterListener { idNews, type ->
            when (type) {
                NewsFeedAdapter.TYPE_NEWS_CHALLENGE -> {
                    ChallengeDetailFragment.openFragment(idNews)
                }
                NewsFeedAdapter.TYPE_NEWS_FEED -> {
                    NewsDetailFragment.openFragment(idNews)
                }
                else -> {
                    NewsDetailFragment.openFragment(idNews)
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
        binding.txtHeader.text = loadStringRes(R.string.news_header)
        adapter.notifyDataSetChanged()
    }
}