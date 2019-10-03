package com.androidyug.marsrover.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import com.androidyug.marsrover.R
import com.androidyug.marsrover.common.Constant
import com.androidyug.marsrover.ui.intro.IntroActivity

import butterknife.ButterKnife

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */

class HomeActivity : AppCompatActivity() {

    private lateinit var fm: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ButterKnife.bind(this)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val isFirstTime = pref.getBoolean(Constant.PREFKEY_FIRST_TIME, true) // default:firstTime true
        if (isFirstTime) {
            val i = Intent(this, IntroActivity::class.java)
            startActivity(i)
            finish()
        }
        fm = supportFragmentManager
        var fragment: Fragment? = fm.findFragmentById(R.id.fragment_container)

        if (fragment == null) {
            fragment = HomeFragment()
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // getMenuInflater().inflate(R.menu.main_menu, menu);

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return super.onOptionsItemSelected(item)
    }

}
