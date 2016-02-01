package com.androidyug.marsrover.ui.selectrover;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidyug.marsrover.R;
import com.androidyug.marsrover.common.Constant;
import com.androidyug.marsrover.common.FontsFactory;
import com.viewpagerindicator.LinePageIndicator;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by IAMONE on 12/24/2015.
 */
public class SelectRoverFragment extends Fragment {


    @Bind(R.id.vp_select_rover)
    ViewPager vpSelectRover;

    @Bind(R.id.lpi)
    LinePageIndicator lpi;

    @Bind(R.id.iv_back)
    ImageView ivBack;

    @Bind(R.id.tv_title)
    TextView tvTitle;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_select_rover, container, false);
        ButterKnife.bind(this, v);

        tvTitle.setTypeface(FontsFactory.robotoCondensedBold(getActivity()));

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        vpSelectRover.setAdapter(new RoverPagerAdapter(getActivity().getSupportFragmentManager()));
        lpi.setViewPager(vpSelectRover);

        vpSelectRover.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return v;
    }




    ////////////////////////////// PagerAdapter ////////////////////////////////////////

    public static class RoverPagerAdapter extends FragmentPagerAdapter {

        private static int NUM_ITEMS = 3;

        public RoverPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return RoverFragment.newInstance(Constant.ROVER_CURIOUSITY);
                case 1:
                    return RoverFragment.newInstance(Constant.ROVER_OPPORTUNITY);
                case 2:
                    return RoverFragment.newInstance(Constant.ROVER_SPIRIT);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }

}
