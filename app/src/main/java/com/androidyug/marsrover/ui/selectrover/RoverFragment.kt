package com.androidyug.marsrover.ui.selectrover

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout

import butterknife.BindView
import com.androidyug.marsrover.R
import com.androidyug.marsrover.common.Constant
import com.androidyug.marsrover.ui.main.HomeFragment

import butterknife.ButterKnife

/**
 * Created by IAMONE on 12/24/2015.
 */
class RoverFragment : Fragment(), View.OnClickListener {

    @BindView(R.id.fl_curiosity)
    lateinit var flCuriosity: FrameLayout

    @BindView(R.id.fl_opportunity)
    lateinit var flOpportunity: FrameLayout

    @BindView(R.id.fl_spirit)
    lateinit var flSpirit: FrameLayout

    @BindView(R.id.btn_see_photo_curiosity)
    lateinit var btnCurosity: Button

    @BindView(R.id.btn_see_photo_opportunity)
    lateinit var btnOpportunity: Button

    @BindView(R.id.btn_see_photo_spirit)
    lateinit var btnSpirit: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.rover, container, false)
        ButterKnife.bind(this, v)

        initOnClickListener()

        if (arguments.containsKey(ROVER_ARGS)) {
            val argsRover = arguments.getInt(ROVER_ARGS)
            if (argsRover == Constant.ROVER_CURIOUSITY) {

                showCuriosity()
            } else if (argsRover == Constant.ROVER_OPPORTUNITY) {

                showOpportunity()
            } else if (argsRover == Constant.ROVER_SPIRIT) {

                showSpirit()
            }
        }

        return v

    }


    private fun initOnClickListener() {
        btnCurosity.setOnClickListener(this)
        btnOpportunity.setOnClickListener(this)
        btnSpirit.setOnClickListener(this)
    }

    private fun showCuriosity() {
        flCuriosity.visibility = View.VISIBLE
        flSpirit.visibility = View.GONE
        flOpportunity.visibility = View.GONE
    }

    private fun showOpportunity() {
        flCuriosity.visibility = View.GONE
        flSpirit.visibility = View.GONE
        flOpportunity.visibility = View.VISIBLE
    }


    private fun showSpirit() {
        flCuriosity.visibility = View.GONE
        flSpirit.visibility = View.VISIBLE
        flOpportunity.visibility = View.GONE
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.btn_see_photo_spirit -> {
                setResult(Constant.ROVER_SPIRIT, Constant.SPIRIT_LANDING_DATE)
                activity.finish()
            }
            R.id.btn_see_photo_opportunity -> {
                setResult(Constant.ROVER_OPPORTUNITY, Constant.OPPORTUNITY_LANDING_DATE)
                activity.finish()
            }
            R.id.btn_see_photo_curiosity -> {
                setResult(Constant.ROVER_CURIOUSITY, Constant.CURIOSITY_LANDING_DATE)
                activity.finish()
            }
        }

    }

    internal fun setResult(roverId: Int, landingEarthDate: String) {

        val i = Intent()
        i.putExtra(HomeFragment.INTENT_ROVER_ID, roverId)
        i.putExtra(HomeFragment.INTENT_ROVER_DEFAULT_DATE, landingEarthDate)
        activity.setResult(Activity.RESULT_OK, i)
    }

    companion object {
        const val ROVER_ARGS = "rover_args"


        fun newInstance(rover: Int): RoverFragment {

            val args = Bundle()
            args.putInt(ROVER_ARGS, rover)
            val fragment = RoverFragment()
            fragment.arguments = args
            return fragment
        }
    }


}
