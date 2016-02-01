package com.androidyug.marsrover.ui.intro;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidyug.marsrover.R;
import com.androidyug.marsrover.common.FontsFactory;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import butterknife.Bind;
import butterknife.ButterKnife;
import oak.svg.AnimatedSvgView;

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */
public class IntroMainFragment extends Fragment {

    private Handler mHandler = new Handler();
    private Handler mTitleHandler;

    private Context mContext;

    @Bind(R.id.animated_svg_view)
    AnimatedSvgView mAnimatedSvgView;

    @Bind(R.id.tv_title)
    TextView tvTitle;

    @Bind(R.id.tv_scroll_down)
    TextView tvScrollDown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_intro_main, container, false);
        ButterKnife.bind(this, v);

        mAnimatedSvgView.setGlyphStrings(AndroidLogoPaths.ANDROID_GLYPHS_2);

        // ARGB values for each glyph
        // opaque
        // red 255
        // green 255
        // blue 255 i.e white.
        mAnimatedSvgView.setFillPaints(
                new int[]{255, 255, 255, 255},
                new int[]{255, 255, 255, 255},
                new int[]{255, 255, 255, 255},
                new int[]{255, 255, 255, 255});

        int traceColor = Color.argb(255, 255, 255, 255); // default 255, 0 ,0 ,0 white
        int[] traceColors = new int[4]; // 4 glyphs
        int residueColor = Color.argb(50, 0, 0, 0);
        int[] residueColors = new int[4]; // 4 glyphs

        // Every glyph will have the same trace/residue
        for (int i = 0; i < traceColors.length; i++) {
            traceColors[i] = traceColor;
            residueColors[i] = residueColor;
        }
        mAnimatedSvgView.setTraceColors(traceColors);
        mAnimatedSvgView.setTraceResidueColors(residueColors);

        tvTitle.setTypeface(FontsFactory.robotoBlack(mContext));
        tvScrollDown.setTypeface(FontsFactory.robotoLight(mContext));

        mTitleHandler = new Handler();
        mTitleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvTitle.setVisibility(View.VISIBLE);
                tvScrollDown.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FlipInX).duration(1000).playOn(tvTitle);
                YoYo.with(Techniques.FadeIn).duration(1000).playOn(tvScrollDown);
            }
        }, 2000);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAnimatedSvgView.start();
            }
        }, 600);
    }
}
