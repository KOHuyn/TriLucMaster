package com.mobileplus.dummytriluc.ui.main.challenge.more

import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemChallengeAchievement
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.model.TypeChallenge
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.ui.main.challenge.adapter.ChallengeAdapter
import com.mobileplus.dummytriluc.ui.main.challenge.detail.ChallengeDetailFragment
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.fragment_challenge_more.*
import org.koin.android.viewmodel.ext.android.viewModel

class ChallengeMoreFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_challenge_more
    private val challengeMoreViewModel by viewModel<ChallengeMoreViewModel>()
    private val challengeAdapter by lazy { ChallengeAdapter() }
    private val strHeader by argument(KEY_HEADER_CHALLENGE_MORE_ARG, "")
    private val typeChallenge by argument(KEY_ID_CHALLENGE_MORE_ARG, "")
    private val page by lazy { Page() }
    private var isReload = true

    companion object {
        const val KEY_HEADER_CHALLENGE_MORE_ARG = "KEY_HEADER_CHALLENGE_MORE"
        const val KEY_ID_CHALLENGE_MORE_ARG = "KEY_ID_CHALLENGE_MORE"
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        txtHeaderChallengeMore.text = strHeader
        viewModelCallback(challengeMoreViewModel)
        challengeMoreViewModel.getMoreChallenge(typeChallenge)
        controlView()
        rcvChallengeMore.setUpRcv(challengeAdapter)
        rcvChallengeMore.getRcv().addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
    }

    private fun requestLayout() {
        rcvChallengeMore.getRcv().requestLayout()
    }

    private fun viewModelCallback(vm: ChallengeMoreViewModel) {
        addDispose(vm.rxChallengeArr.subscribe {
            val items = it.first
            val pageResponse = it.second
            page.run {
                currPage = pageResponse.currPage
                totalPage = pageResponse.totalPage
                isLoading = false
            }
            if (challengeAdapter.items.isEmpty() || isReload)
                challengeAdapter.items = items.toMutableList()
            else challengeAdapter.items.addAll(items)
            requestLayout()
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.isLoading.subscribe {
            if (page.isLoading) {
                loadMoreChallenge.setVisibility(it)
            } else {
                if (it) enableRefresh() else cancelRefresh()
            }
        })
    }

    private fun controlView() {
        btnBackChallengeMore.clickWithDebounce { onBackPressed() }
        rcvChallengeMore.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    challengeMoreViewModel.getMoreChallenge(typeChallenge, currPage)
                }
            }
        }
        rcvChallengeMore.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            challengeMoreViewModel.getMoreChallenge(typeChallenge)
        }
        challengeAdapter.onItemClick = OnClickItemAdapter { _, position ->
            if (typeChallenge != ApiConstants.MY_REWARD) {
                val item = challengeAdapter.items[position]
                if (item.typeItemChallenge() == TypeChallenge.TYPE_ACHIEVEMENT) {
                    nextFragmentDetail((item as ItemChallengeAchievement).id)
                }
            }
        }
    }

    private fun nextFragmentDetail(id: Int) {
        val bundle = Bundle().apply {
            putInt(ChallengeDetailFragment.ID_DETAIL_CHALLENGE_ARG, id)
        }
        postNormal(EventNextFragmentMain(ChallengeDetailFragment::class.java, bundle, true))
    }

    private fun enableRefresh() = rcvChallengeMore.enableRefresh()
    private fun cancelRefresh() = rcvChallengeMore.cancelRefresh()
}