package com.androidyug.marsrover.ui.intro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import butterknife.BindView
import com.androidyug.marsrover.R
import com.androidyug.marsrover.common.Constant
import com.androidyug.marsrover.common.FontsFactory
import com.androidyug.marsrover.ui.main.HomeActivity

import butterknife.ButterKnife

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */
class MoreInfoFragment : Fragment() {
    private var pagePos = 1

    @BindView(R.id.tv_info)
    lateinit var tvInfo: TextView

    @BindView(R.id.btn_enter)
    lateinit var btnEnter: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            pagePos = if (arguments != null) arguments.getInt(ARGS_POSITION) else 1
        } else {
            pagePos = savedInstanceState.getInt(CURRENT_PAGE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_more_info, container, false)
        ButterKnife.bind(this, v)
        tvInfo.typeface = FontsFactory.robotoRegular(activity)

        when (pagePos) {
            1 ->
                // more info one
                tvInfo.text = resources.getText(R.string.app_desc)
            2 ->
                //more info two
                tvInfo.text = resources.getText(R.string.info_more)
            3 ->
                // about developer
                tvInfo.text = Html.fromHtml("Built on planet Earth by a Human Being, </br>Nitin Anand nitinnatural@gmail.com, (+91) 8929754594")
            4 -> {
                // enter button
                tvInfo.visibility = View.GONE
                btnEnter.typeface = FontsFactory.robotoCondensedLight(activity)
                btnEnter.visibility = View.VISIBLE
                btnEnter.setOnClickListener {
                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
                    val edit = sharedPref.edit()
                    edit.putBoolean(Constant.PREFKEY_FIRST_TIME, false)
                    edit.commit()
                    val i = Intent(activity, HomeActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)
                    activity.overridePendingTransition(android.R.anim.fade_in, R.anim.zoom_in_exit)
                    activity.finish()
                }
            }
        }

        return v
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_PAGE, pagePos)
        super.onSaveInstanceState(outState)

    }

    companion object {

        const val ARGS_POSITION = "args_position"
        const val CURRENT_PAGE = "current_page"


        fun newInstance(position: Int): Fragment {
            val fragment = MoreInfoFragment()
            val args = Bundle()
            args.putInt(ARGS_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }
}
