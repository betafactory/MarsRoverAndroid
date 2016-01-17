package com.androidyug.marsscope.ui.intro;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.androidyug.marsscope.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class IntroActivity extends AppCompatActivity {

    @Bind(R.id.vp_intro)
    VerticalViewPager vpIntro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowAttributes();
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);
        vpIntro.setAdapter(new IntroPagerAdapter(getSupportFragmentManager()));
    }



//    private void setWindowSizes() {
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            display.getRealSize(size);
//        } else {
//            display.getSize(size);
//        }
//        width = size.x;
//        height = size.y;
//    }

    private void setWindowAttributes() {
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }



    ///////////////////////////// ViewPager Adapter /////////////////////////

    class IntroPagerAdapter extends FragmentPagerAdapter {
        int itemTotal = 5;

        public IntroPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return itemTotal;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                return new IntroMainFragment();
            } else {
                return MoreInfoFragment.newInstance(position);
            }
        }

    }
}
