package com.mobileplus.dummytriluc.ui.main.challenge

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.*
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.databinding.FragmentChallengeBinding
import com.mobileplus.dummytriluc.ui.main.challenge.adapter.AppellationAdapter
import com.mobileplus.dummytriluc.ui.main.challenge.adapter.ChallengeAdapter
import com.mobileplus.dummytriluc.ui.main.challenge.create.ChallengeChooseModeFragment
import com.mobileplus.dummytriluc.ui.main.challenge.detail.ChallengeDetailFragment
import com.mobileplus.dummytriluc.ui.main.challenge.more.ChallengeMoreFragment
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.applyColorRefresh
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.applyClickShrink
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class ChallengeFragment : BaseFragmentZ<FragmentChallengeBinding>() {

    override fun getLayoutBinding(): FragmentChallengeBinding =
        FragmentChallengeBinding.inflate(layoutInflater)

    private val challengeViewModel by viewModel<ChallengeViewModel>()
    private val challengeAppellationAdapter by lazy { AppellationAdapter() }
    private val myChallengeAdapter by lazy { ChallengeAdapter() }
    private val challengeCommunityAdapter by lazy { ChallengeAdapter() }
    private var initialized = false

    fun initView() {
        if (!initialized) {
            challengeViewModel.getChallenge()
            initialized = true
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        callbackViewModel(challengeViewModel)
        configView()
        setupRcvChallenge()
        controlView()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.refreshChallenge.isRefreshing = isLoading
    }

    private fun callbackViewModel(vm: ChallengeViewModel) {
        addDispose(vm.rxMyReward.subscribe { items ->
            binding.lnMyRewardChallenge.setVisibility(items.isNotEmpty())
            binding.rcvMyAppellationChallenge.setVisibility(items.isNotEmpty())
            challengeAppellationAdapter.items = items.toMutableList()
        }, vm.rxMyChallenge.subscribe { itemsMyChallenge ->
            binding.lnMyChallenge.setVisibility(itemsMyChallenge.isNotEmpty())
            binding.rcvMyChallenge.setVisibility(itemsMyChallenge.isNotEmpty())
            myChallengeAdapter.items = itemsMyChallenge.toMutableList()
        }, vm.rxChallengeCommunity.subscribe { itemsChallengeCommunity ->
            binding.lnCommunityChallenge.setVisibility(itemsChallengeCommunity.isNotEmpty())
            binding.rcvCommunityChallenge.setVisibility(itemsChallengeCommunity.isNotEmpty())
            challengeCommunityAdapter.items = itemsChallengeCommunity.toMutableList()
        }, vm.rxNoData.subscribe {
            binding.noDataChallenge.root.setVisibility(it)
        })
        addDispose(vm.isLoading.subscribe { setLoading(it) })
        addDispose(vm.rxMessage.subscribe { toast(it) })
    }

    private fun controlView() {
        binding.refreshChallenge.setOnRefreshListener {
            challengeViewModel.getChallenge()
        }

        binding.btnCreateChallenge.clickWithDebounce {
            postNormal(EventNextFragmentMain(ChallengeChooseModeFragment::class.java, true))
        }
        binding.btnMoreMyRewardChallenge.clickWithDebounce {
            nextFragmentMore(
                loadStringRes(R.string.my_nick_name),
                ApiConstants.MY_REWARD
            )
        }
        binding.btnMoreMyChallenge.clickWithDebounce {
            nextFragmentMore(
                loadStringRes(R.string.my_challenge),
                ApiConstants.CHALLENGES_JOIN
            )
        }
        binding.btnMoreCommunityChallenge.clickWithDebounce {
            nextFragmentMore(
                loadStringRes(R.string.challenge_from_community),
                ApiConstants.CHALLENGES_PUBLISH
            )
        }
        challengeCommunityAdapter.onItemClick = OnClickItemAdapter { _, position ->
            val item = challengeCommunityAdapter.items[position]
            if (item.typeItemChallenge() == TypeChallenge.TYPE_ACHIEVEMENT) {
                ChallengeDetailFragment.openFragment((item as ItemChallengeAchievement).id)
            }
        }
        myChallengeAdapter.onItemClick = OnClickItemAdapter { _, position ->
            val item = myChallengeAdapter.items[position]
            if (item.typeItemChallenge() == TypeChallenge.TYPE_ACHIEVEMENT) {
                ChallengeDetailFragment.openFragment((item as ItemChallengeAchievement).id)
            }
        }
    }

    private fun nextFragmentMore(header: String, type: String) {
        val bundle = Bundle().apply {
            putString(
                ChallengeMoreFragment.KEY_HEADER_CHALLENGE_MORE_ARG,
                header
            )
            putString(
                ChallengeMoreFragment.KEY_ID_CHALLENGE_MORE_ARG,
                type
            )
        }
        postNormal(EventNextFragmentMain(ChallengeMoreFragment::class.java, bundle, true))
    }

    private fun setupRcvChallenge() {
        binding.rcvMyAppellationChallenge.run {
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_8).toInt(),
                    isHorizontalRecyclerView = true
                )
            )
            setHasFixedSize(true)
            adapter = challengeAppellationAdapter
        }
        setUpRcv(binding.rcvMyChallenge, myChallengeAdapter, true, isNestedScrollingEnabled = false)
        binding.rcvMyChallenge.addPaddingRcv()
        setUpRcv(
            binding.rcvCommunityChallenge,
            challengeCommunityAdapter,
            true,
            isNestedScrollingEnabled = false
        )
        binding.rcvCommunityChallenge.addPaddingRcv()
    }

    private fun RecyclerView.addPaddingRcv() = this.addItemDecoration(
        MarginItemDecoration(
            resources.getDimension(R.dimen.space_8).toInt()
        )
    )

    private fun configView() {
        binding.btnCreateChallenge.applyClickShrink()
        binding.refreshChallenge.applyColorRefresh()
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
        binding.txtTitle.text = loadStringRes(R.string.challenge)
        binding.btnCreateChallenge.text = loadStringRes(R.string.create_challenge)
        binding.btnMoreMyRewardChallenge.text = loadStringRes(R.string.more)
        binding.txtLabelMyAppellation.text = loadStringRes(R.string.my_nick_name)
        binding.txtLabelMyChallenge.text = loadStringRes(R.string.my_challenge)
        binding.btnMoreMyChallenge.text = loadStringRes(R.string.more)
        binding.txtLabelChallengeCommunity.text = loadStringRes(R.string.challenge_from_community)
        binding.btnMoreCommunityChallenge.text = loadStringRes(R.string.more)
    }

}