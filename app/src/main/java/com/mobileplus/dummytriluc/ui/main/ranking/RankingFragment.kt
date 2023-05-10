package com.mobileplus.dummytriluc.ui.main.ranking

import android.os.Bundle
import android.widget.TextView
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.response.Rank
import com.mobileplus.dummytriluc.databinding.FragmentRankingBinding
import com.mobileplus.dummytriluc.ui.main.ranking.adapter.RankingAdapter
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class RankingFragment : BaseFragmentZ<FragmentRankingBinding>() {
    override fun getLayoutBinding(): FragmentRankingBinding =
        FragmentRankingBinding.inflate(layoutInflater)

    private val rankingAdapter by lazy { RankingAdapter() }
    private val vm by viewModel<RankingViewModel>()
    private val gson by inject<Gson>()
    private val page by lazy { Page() }
    private val rcv by lazy { binding.rcvRanking }
    private var isReload = true

    companion object {
        private const val ARG_RANKING = "argRanking"
        fun openFragment(rank: Rank?, gson: Gson) {
            val bundle = Bundle().apply {
                if (rank != null) {
                    putString(ARG_RANKING, gson.toJson(rank))
                }
            }
            postNormal(EventNextFragmentMain(RankingFragment::class.java, bundle, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        callBackViewModel(vm)
        vm.getRanking()
        configView()
        handleClick()
    }

    private fun callBackViewModel(vm: RankingViewModel) {
        addDispose(
            vm.rxRankings.subscribe {
                val items = it.first.toMutableList()
                val pageResponse = it.second
                pageResponse?.run {
                    page.currPage = currPage
                    page.totalPage = totalPage
                    page.isLoading = false
                }
                if (rankingAdapter.items.isEmpty() || isReload)
                    rankingAdapter.items = items
                else
                    rankingAdapter.items.addAll(items)
            },
            vm.rxMessage.subscribe { toast(it) },
            vm.isLoading.subscribe {
                if (page.isLoading) {
                    binding.loadingRanking.root.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
            })
    }

    private fun configView() {
        rcv.setUpRcv(rankingAdapter)
        rcv.getRcv()
            .addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_4).toInt()
                )
            )
        if (arguments?.containsKey(ARG_RANKING) == true) {
            val myRank = gson.fromJson(argument(ARG_RANKING, "").value, Rank::class.java)
            binding.txtRankNumber.text = String.format("#%s", myRank.top)
            setRankingUp(binding.txtRankingDifference, myRank.rankUp ?: 0)
            binding.txtScorePersonRank.text = myRank.scoreZ
            binding.txtNamePersonRank.text = vm.userInfo?.fullName
            binding.imgAvatarPersonRank.show(vm.userInfo?.avatarPath)
        } else {
            binding.lnMyRanking.hide()
        }
    }

    private fun enableRefresh() = rcv.enableRefresh()
    private fun cancelRefresh() = rcv.cancelRefresh()

    private fun handleClick() {
        binding.btnBackRanking.clickWithDebounce { onBackPressed() }

        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    vm.getRanking(currPage)
                }
            }
        }
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            vm.getRanking()
        }
//        spinnerTypeRank.clickWithDebounce {
//            CustomSpinner(it, requireContext())
//                .setDataSource(
//                    mutableListOf(
//                        CustomSpinner.SpinnerItem("TỔNG", ""),
//                        CustomSpinner.SpinnerItem("SỨC MẠNH", ""),
//                        CustomSpinner.SpinnerItem("CHÍNH XÁC", ""),
//                        CustomSpinner.SpinnerItem("PHẢN XẠ", ""),
//                        CustomSpinner.SpinnerItem("CHĂM CHỈ", ""),
//                        CustomSpinner.SpinnerItem("TỐC ĐỘ", "")
//                    )
//                )
//                .build()
//                .setOnSelectedItemCallback { item ->
//                    enableRefresh()
//                    Handler().postDelayed({
//                        cancelRefresh()
//                    }, 1000)
//                }
//        }
    }

    private fun setRankingUp(txt: TextView, rankUp: Int) {
        when {
            rankUp == 0 -> {
                txt.run {
                    text = " "
                    setDrawableStart(R.drawable.ic_rank_normal)
                }
            }
            rankUp > 0 -> {
                txt.run {
                    text = "+$rankUp"
                    setDrawableStart(R.drawable.ic_rank_up)
                    setTextColorz(R.color.clr_green)
                }
            }
            rankUp ?: 0 < 0 -> {
                txt.run {
                    text = "$rankUp"
                    setDrawableStart(R.drawable.ic_rank_down)
                    setTextColorz(R.color.clr_red)
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
        binding.txtHeader.text = loadStringRes(R.string.ranking)
        binding.txtRankingChart.text = loadStringRes(R.string.ranking_charts)
        binding.spinnerTypeRank.text = loadStringRes(R.string.total)
    }
}