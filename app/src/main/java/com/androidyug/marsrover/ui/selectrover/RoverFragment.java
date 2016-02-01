package com.androidyug.marsrover.ui.selectrover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.androidyug.marsrover.R;
import com.androidyug.marsrover.common.Constant;
import com.androidyug.marsrover.ui.main.HomeFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by IAMONE on 12/24/2015.
 */
public class RoverFragment extends Fragment implements View.OnClickListener {
    public static final String ROVER_ARGS = "rover_args";

    @Bind(R.id.fl_curiosity)
    FrameLayout flCuriosity;

    @Bind(R.id.fl_opportunity)
    FrameLayout flOpportunity;

    @Bind(R.id.fl_spirit)
    FrameLayout flSpirit;

    @Bind(R.id.btn_see_photo_curiosity)
    Button btnCurosity;

    @Bind(R.id.btn_see_photo_opportunity)
    Button btnOpportunity;

    @Bind(R.id.btn_see_photo_spirit)
    Button btnSpirit;

    public RoverFragment(){

    }


    public static RoverFragment newInstance(int rover) {

        Bundle args = new Bundle();
        args.putInt(ROVER_ARGS, rover);
        RoverFragment fragment = new RoverFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.rover, container, false);
        ButterKnife.bind(this, v);

        initOnClickListener();

        if(getArguments().containsKey(ROVER_ARGS)){
            Integer argsRover = getArguments().getInt(ROVER_ARGS);
            if (argsRover == Constant.ROVER_CURIOUSITY){

                showCuriosity();
            } else if (argsRover == Constant.ROVER_OPPORTUNITY){

                showOpportunity();
            } else if(argsRover == Constant.ROVER_SPIRIT){

                showSpirit();
            }
        }

        return v;

    }


    private void initOnClickListener(){
        btnCurosity.setOnClickListener(this);
        btnOpportunity.setOnClickListener(this);
        btnSpirit.setOnClickListener(this);
    }

    private void showCuriosity(){
        flCuriosity.setVisibility(View.VISIBLE);
        flSpirit.setVisibility(View.GONE);
        flOpportunity.setVisibility(View.GONE);
    }

    private void showOpportunity(){
        flCuriosity.setVisibility(View.GONE);
        flSpirit.setVisibility(View.GONE);
        flOpportunity.setVisibility(View.VISIBLE);
    }


    private void showSpirit(){
        flCuriosity.setVisibility(View.GONE);
        flSpirit.setVisibility(View.VISIBLE);
        flOpportunity.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id){
            case R.id.btn_see_photo_spirit:
                setResult(Constant.ROVER_SPIRIT, Constant.SPIRIT_LANDING_DATE);
                getActivity().finish();
                break;
            case R.id.btn_see_photo_opportunity:
                setResult(Constant.ROVER_OPPORTUNITY, Constant.OPPORTUNITY_LANDING_DATE);
                getActivity().finish();
                break;
            case R.id.btn_see_photo_curiosity:
                setResult(Constant.ROVER_CURIOUSITY, Constant.CURIOSITY_LANDING_DATE);
                getActivity().finish();
                break;
        }

    }

    void setResult(int roverId, String landingEarthDate){

        Intent i = new Intent();
        i.putExtra(HomeFragment.INTENT_ROVER_ID, roverId);
        i.putExtra(HomeFragment.INTENT_ROVER_DEFAULT_DATE, landingEarthDate);
        getActivity().setResult(Activity.RESULT_OK, i);
    }


}
