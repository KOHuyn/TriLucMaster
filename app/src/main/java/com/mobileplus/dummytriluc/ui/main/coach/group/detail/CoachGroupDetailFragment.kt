package com.mobileplus.dummytriluc.ui.main.coach.group.detail

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.core.BaseFragment
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDiscipleGroup
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.lesson.CoachGroupDetailLessonFragment
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.member.CoachGroupDetailMemberFragment
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.message.CoachGroupDetailMessageFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.widget.ViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_coach_group_detail.*
import org.koin.android.ext.android.inject

/**
 * Created by KOHuyn on 3/12/2021
 */
class CoachGroupDetailFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_group_detail
    private val gson by inject<Gson>()
    var groupInfo: ItemDiscipleGroup? = null
    private val fragMember by lazy { CoachGroupDetailMemberFragment() }
    private val fragLesson by lazy { CoachGroupDetailLessonFragment() }
    private val fragChat by lazy { CoachGroupDetailMessageFragment() }
    val isGuest: Boolean by argument(ARG_IS_GUEST_GROUP_DETAIL, false)

    private val adapterViewPager by lazy {
        ViewPagerAdapter(
            childFragmentManager,
            listOf(fragMember, fragLesson, fragChat),
            emptyList()
        )
    }

    companion object {
        private const val ARG_GROUP_INFO = "ARG_GROUP_INFO"
        private const val ARG_IS_GUEST_GROUP_DETAIL = "IS_GUEST_GROUP_DETAIL_ARG"
        fun openFragment(
            isGuest: Boolean,
            itemDiscipleGroup: ItemDiscipleGroup,
            gson: Gson
        ) {
            val bundle = Bundle().apply {
                putBoolean(ARG_IS_GUEST_GROUP_DETAIL, isGuest)
                putString(
                    ARG_GROUP_INFO,
                    gson.toJson(itemDiscipleGroup)
                )
            }
            postNormal(
                EventNextFragmentMain(
                    CoachGroupDetailFragment::class.java,
                    bundle,
                    true
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        getArg()
        setupViewPager()
        handleClick()
    }

    private fun setupViewPager() {
        vpGroupDisciple.adapter = adapterViewPager
        vpGroupDisciple.offscreenPageLimit = adapterViewPager.count
    }

    private fun getArg() {
        try {
            gson.fromJson(argument(ARG_GROUP_INFO, "").value, ItemDiscipleGroup::class.java).run {
                groupInfo = this
                txtTitleGroupDialog.setTextNotNull(name)
                fragLesson.classId = groupInfo?.id
            }
        } catch (e: Exception) {
            e.logErr()
            Handler(Looper.getMainLooper()).postDelayed({ onBackPressed() }, 500)
        }
    }

    private fun handleClick() {
        btnBackGroupMember.clickWithDebounce { onBackPressed() }
        rgChooseModeGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbListDisciple -> {
                    vpGroupDisciple.setCurrentItem(0, false)
                }
                R.id.rbLesson -> {
                    vpGroupDisciple.setCurrentItem(1, false)
                    fragLesson.initView()
                }
                R.id.rbMessageGroup -> {
                    vpGroupDisciple.setCurrentItem(2, false)
                }
            }
        }
    }
}