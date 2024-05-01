package com.snowman.neverlate.ui.profile

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.widget.ViewPager2

@Suppress("DEPRECATION") // TODO: swith to pagerView2 (I don't know how to use it)
class ViewPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    val fragments = listOf(ProfileTabPersonalFragment(), ProfileTabFriendsFragment(), ProfileTabBadgesFragment())
    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Personal"
            1 -> "Friends"
            2 -> "Badges"
            else -> null
        }
    }
}