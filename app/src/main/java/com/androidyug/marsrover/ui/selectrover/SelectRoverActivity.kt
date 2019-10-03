package com.androidyug.marsrover.ui.selectrover

import android.graphics.Color
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager

import com.androidyug.marsrover.R

class SelectRoverActivity : AppCompatActivity() {

    private lateinit var fm: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {

        // call this method before super as well as settting onCreateView()
        //        requestWindowFeature();
        //setWindowAttributes();
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_rover)
        fm = supportFragmentManager
        var fragment: Fragment? = fm.findFragmentById(R.id.fragment_container)

        if (fragment == null) {
            fragment = SelectRoverFragment()
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
        }


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

}
