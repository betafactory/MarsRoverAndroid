package com.androidyug.marsrover.ui.intro;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.androidyug.marsrover.R;
import com.androidyug.marsrover.ui.starfield.StarField;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */

public class IntroActivity extends AppCompatActivity {

    private StarField starField;
    int width;
    int height;
    boolean fromOnCreate = false;

    @Bind(R.id.vp_intro)
    VerticalViewPager vpIntro;

    @Bind(R.id.sv_starfield)
    SurfaceView svStarfield;

//    @Bind(R.id.iv_bg)
//    ImageView ivBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature();
        super.onCreate(savedInstanceState);
//        setWindowAttributes();
        setWindowSizes();
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);


        starField = new StarField(svStarfield.getHolder(), width, height);
        starField.start();

        if (getSupportFragmentManager().getFragments() != null)
               for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                     getSupportFragmentManager().beginTransaction().remove(fragment).commit();
               }

        vpIntro.setAdapter(new IntroPagerAdapter(getSupportFragmentManager()));
        vpIntro.setOffscreenPageLimit(5);



    }


    @Override
    public void onPause() {
        super.onPause();
        starField.stop();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (fromOnCreate) {
            fromOnCreate = false;
        } else {
            setWindowSizes();
            //starField = new StarField(svStarfield.getHolder(), width, height);
            starField.start();
        }
    }


    private void setWindowSizes() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
        } else {
            display.getSize(size);
        }
        width = size.x;
        height = size.y;
    }

    void zoomInZoomOut(ImageView iv){

    }

    private void requestWindowFeature(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

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
