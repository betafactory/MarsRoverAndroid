package com.androidyug.marsscope.ui.intro;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidyug.marsscope.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import oak.svg.AnimatedSvgView;

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */
public class IntroMainFragment extends Fragment {

    private Handler mHandler = new Handler();

    @Bind(R.id.animated_svg_view)
    AnimatedSvgView mAnimatedSvgView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_intro_main, container, false);
        ButterKnife.bind(this, v);

        mAnimatedSvgView.setGlyphStrings(AndroidLogoPaths.ANDROID_GLYPHS_2);

        // ARGB values for each glyph
        mAnimatedSvgView.setFillPaints(
                new int[]{120, 120, 120, 120},
                new int[]{0, 0, 0, 0},
                new int[]{173, 173, 173, 173},
                new int[]{239, 239, 239, 239});

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
        }, 1000);
    }
}
