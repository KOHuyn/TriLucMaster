package com.mobileplus.dummytriluc.ui.main.coach.disciple

import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.coach.disciple.list.DiscipleListFragment
import com.mobileplus.dummytriluc.ui.main.coach.disciple.waiting.DiscipleWaitingFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.widget.ViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_disciple.*

class DiscipleFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_disciple
    private val discipleWaitingFragment by lazy { DiscipleWaitingFragment() }
    private val discipleListFragment by lazy { DiscipleListFragment() }
    private val pagerAdapter by lazy {
        ViewPagerAdapter(
            childFragmentManager, listOf(discipleListFragment, discipleWaitingFragment),
            emptyList()
        )
    }

    private val tabDefault: Int by argument(ARG_KEY_DEFAULT_SWITCH, TAB_LIST)

    companion object {
        const val TAB_LIST = 1
        const val TAB_WAITING = 2
        private const val ARG_KEY_DEFAULT_SWITCH = "KEY_DEFAULT_SWITCH"

        fun openFragment(defaultSwitch: Int = TAB_LIST) {
            val bundle = Bundle().apply {
                putInt(ARG_KEY_DEFAULT_SWITCH, defaultSwitch)
            }
            postNormal(EventNextFragmentMain(DiscipleFragment::class.java, bundle,true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        btnBackDisciple.clickWithDebounce { onBackPressed() }
        setupViewPager()
        initAction()
        if (tabDefault == TAB_WAITING) rbWaitingDisciple.isChecked = true
    }

    private fun setupViewPager() {
        viewPagerDisciple.adapter = pagerAdapter
        viewPagerDisciple.offscreenPageLimit = pagerAdapter.count
    }

    private fun initAction() {
        rgChooseDisciple.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbListDisciple -> {
                    viewPagerDisciple.currentItem = 0
                    txtHeaderDisciple.text = loadStringRes(R.string.label_disciple)
                }
                R.id.rbWaitingDisciple -> {
                    viewPagerDisciple.currentItem = 1
                    txtHeaderDisciple.text = loadStringRes(R.string.title_waiting_list)
                    discipleWaitingFragment.initView()
                }
            }
        }
    }
}