package com.androidyug.marsrover.ui.intro

import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Display
import android.view.SurfaceView
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView

import butterknife.BindView
import com.androidyug.marsrover.R
import com.androidyug.marsrover.ui.starfield.StarField
import butterknife.ButterKnife

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */

class IntroActivity : AppCompatActivity() {

    private lateinit var starField: StarField
    private var width: Int = 0
    private var height: Int = 0
    private var fromOnCreate = false

    @BindView(R.id.vp_intro)
    lateinit var vpIntro: VerticalViewPager

    @BindView(R.id.sv_starfield)
    lateinit var svStarfield: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature()
        super.onCreate(savedInstanceState)
        //        setWindowAttributes();
        setWindowSizes()
        setContentView(R.layout.activity_intro)
        ButterKnife.bind(this)


        starField = StarField(svStarfield.holder, width, height)
        starField.start()

        if (supportFragmentManager.fragments != null)
            for (fragment in supportFragmentManager.fragments) {
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }

        vpIntro.adapter = IntroPagerAdapter(supportFragmentManager)
        vpIntro.offscreenPageLimit = 5


    }


    public override fun onPause() {
        super.onPause()
        starField.stop()
    }


    public override fun onResume() {
        super.onResume()
        if (fromOnCreate) {
            fromOnCreate = false
        } else {
            setWindowSizes()
            //starField = new StarField(svStarfield.getHolder(), width, height);
            starField.start()
        }
    }


    private fun setWindowSizes() {
        val display = windowManager.defaultDisplay
        val size = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size)
        } else {
            display.getSize(size)
        }
        width = size.x
        height = size.y
    }

    internal fun zoomInZoomOut(iv: ImageView) {

    }

    private fun requestWindowFeature() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun setWindowAttributes() {
        val window = window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }


    ///////////////////////////// ViewPager Adapter /////////////////////////

    internal inner class IntroPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        var itemTotal = 5

        override fun getCount(): Int {
            return itemTotal
        }

        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                IntroMainFragment()
            } else {
                MoreInfoFragment.newInstance(position)
            }
        }

    }
}
