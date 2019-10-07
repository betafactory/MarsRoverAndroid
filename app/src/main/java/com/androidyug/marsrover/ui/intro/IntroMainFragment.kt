package com.androidyug.marsrover.ui.intro

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import butterknife.BindView
import com.androidyug.marsrover.R
import com.androidyug.marsrover.common.FontsFactory
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo

import butterknife.ButterKnife
import oak.svg.AnimatedSvgView

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */
class IntroMainFragment : Fragment() {

    private val mHandler = Handler()
    private lateinit var mTitleHandler: Handler
    private lateinit var mContext: Context

    @BindView(R.id.animated_svg_view)
    lateinit var mAnimatedSvgView: AnimatedSvgView

    @BindView(R.id.tv_title)
    lateinit var tvTitle: TextView

    @BindView(R.id.tv_scroll_down)
    lateinit var tvScrollDown: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_intro_main, container, false)
        ButterKnife.bind(this, v)

        mAnimatedSvgView.setGlyphStrings(AndroidLogoPaths.ANDROID_GLYPHS_2)

        // ARGB values for each glyph
        // opaque
        // red 255
        // green 255
        // blue 255 i.e white.
        mAnimatedSvgView.setFillPaints(
                intArrayOf(255, 255, 255, 255),
                intArrayOf(255, 255, 255, 255),
                intArrayOf(255, 255, 255, 255),
                intArrayOf(255, 255, 255, 255))

        val traceColor = Color.argb(255, 255, 255, 255) // default 255, 0 ,0 ,0 white
        val traceColors = IntArray(4) // 4 glyphs
        val residueColor = Color.argb(50, 0, 0, 0)
        val residueColors = IntArray(4) // 4 glyphs

        // Every glyph will have the same trace/residue
        for (i in traceColors.indices) {
            traceColors[i] = traceColor
            residueColors[i] = residueColor
        }
        mAnimatedSvgView.setTraceColors(traceColors)
        mAnimatedSvgView.setTraceResidueColors(residueColors)

        tvTitle.typeface = FontsFactory.robotoBlack(mContext!!)
        tvScrollDown!!.typeface = FontsFactory.robotoLight(mContext!!)

        mTitleHandler = Handler()
        mTitleHandler.postDelayed({
            tvTitle.visibility = View.VISIBLE
            tvScrollDown!!.visibility = View.VISIBLE
            YoYo.with(Techniques.FlipInX).duration(1000).playOn(tvTitle)
            YoYo.with(Techniques.FadeIn).duration(1000).playOn(tvScrollDown)
        }, 2000)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mHandler.postDelayed({ mAnimatedSvgView.start() }, 600)
    }
}
