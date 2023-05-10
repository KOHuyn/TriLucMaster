package com.mobileplus.dummytriluc.ui.main.practice.adapter

import android.os.Parcelable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.mobileplus.dummytriluc.ui.widget.ViewPagerDynamic

class WrapPagerAdapter(
    fm: FragmentManager,
    private val fragmentList: List<Fragment>,
    private val titles: List<String>
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var posFragment = -1

    override fun getItem(position: Int): Fragment {
        return this.fragmentList[position]
    }

    override fun getCount(): Int {
        return this.fragmentList.size
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        if (position != posFragment) {
            val fragment: Fragment? = `object` as? Fragment
            val page: ViewPagerDynamic? = container as? ViewPagerDynamic
            if (fragment != null && fragment.view != null) {
                posFragment = position
                page?.measureCurrentView(fragment.view)
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return this.titles[position]
    }

    override fun saveState(): Parcelable? {
        return null
    }
}