package com.androidyug.marsrover.ui.selectrover

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import butterknife.BindView
import com.androidyug.marsrover.R
import com.androidyug.marsrover.common.Constant
import com.androidyug.marsrover.common.FontsFactory
import com.viewpagerindicator.LinePageIndicator

import butterknife.ButterKnife

/**
 * Created by IAMONE on 12/24/2015.
 */
class SelectRoverFragment : Fragment() {

    @BindView(R.id.vp_select_rover)
    lateinit var vpSelectRover: ViewPager

    @BindView(R.id.lpi)
    lateinit var lpi: LinePageIndicator

    @BindView(R.id.iv_back)
    lateinit var ivBack: ImageView

    @BindView(R.id.tv_title)
    lateinit var tvTitle: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_select_rover, container, false)
        ButterKnife.bind(this, v)

        tvTitle.typeface = FontsFactory.robotoCondensedBold(activity)

        ivBack.setOnClickListener { activity.finish() }


        vpSelectRover.adapter = RoverPagerAdapter(activity.supportFragmentManager)
        lpi.setViewPager(vpSelectRover)

        vpSelectRover.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        return v
    }


    ////////////////////////////// PagerAdapter ////////////////////////////////////////

    class RoverPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        // Returns total number of pages
        override fun getCount(): Int {
            return NUM_ITEMS
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> RoverFragment.newInstance(Constant.ROVER_CURIOUSITY)
                1 -> RoverFragment.newInstance(Constant.ROVER_OPPORTUNITY)
                2 -> RoverFragment.newInstance(Constant.ROVER_SPIRIT)
                else -> null
            }
        }

        // Returns the page title for the top indicator
        override fun getPageTitle(position: Int): CharSequence {
            return "Page $position"
        }

        companion object {
            private const val NUM_ITEMS = 3
        }

    }

}
