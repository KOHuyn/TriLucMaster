package com.mobileplus.dummytriluc.ui.main.practice.dialog.adapter

import android.os.Parcelable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.mobileplus.dummytriluc.ui.widget.ViewPagerDynamic

/**
 * Created by KOHuyn on 3/5/2021
 */
class FixedFirstFragmentViewPagerAdapter(
    fm: FragmentManager,
    private val fragmentList: List<Fragment>,
    private val titles: List<String>
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return this.fragmentList[position]
    }

    override fun getCount(): Int {
        return this.fragmentList.size
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        val page: ViewPagerDynamic? = container as? ViewPagerDynamic
        page?.measureCurrentView(fragmentList[0].view)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return this.titles[position]
    }

    override fun saveState(): Parcelable? {
        return null
    }
}