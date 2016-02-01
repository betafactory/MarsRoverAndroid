package com.androidyug.marsrover.ui.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.androidyug.marsrover.R;
import com.androidyug.marsrover.common.Constant;
import com.androidyug.marsrover.common.FontsFactory;
import com.androidyug.marsrover.ui.main.HomeActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */
public class MoreInfoFragment extends Fragment {

    public static final String ARGS_POSITION = "args_position";
    public static final String CURRENT_PAGE = "current_page";
    int pagePos = 1;


    public static Fragment newInstance(int position){
        final Fragment fragment = new MoreInfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null){
            pagePos = getArguments() != null ? getArguments().getInt(ARGS_POSITION) : 1;
        } else {
            pagePos = savedInstanceState.getInt(CURRENT_PAGE);
        }
    }

    @Bind(R.id.tv_info)
    TextView tvInfo;

    @Bind(R.id.btn_enter)
    Button btnEnter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_more_info, container, false);
        ButterKnife.bind(this, v);
        tvInfo.setTypeface(FontsFactory.robotoRegular(getActivity()));

        switch (pagePos) {
            case 1:
                // more info one
                tvInfo.setText(getResources().getText(R.string.app_desc));
                break;
            case 2:
                //more info two
                tvInfo.setText(getResources().getText(R.string.info_more));
                break;
            case 3:
                // about developer
                tvInfo.setText(Html.fromHtml("Built on planet Earth by a Human Being, </br>Nitin Anand nitinnatural@gmail.com, (+91) 8929754594"));
                break;
            case 4:
                // enter button
                tvInfo.setVisibility(View.GONE);
                btnEnter.setTypeface(FontsFactory.robotoCondensedLight(getActivity()));
                btnEnter.setVisibility(View.VISIBLE);
                btnEnter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor edit = sharedPref.edit();
                        edit.putBoolean(Constant.PREFKEY_FIRST_TIME, false);
                        edit.commit();
                        Intent i = new Intent(getActivity(), HomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        getActivity().overridePendingTransition(android.R.anim.fade_in, R.anim.zoom_in_exit);
                        getActivity().finish();
                    }
                });
                break;
        }

        return v;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_PAGE, pagePos);
        super.onSaveInstanceState(outState);

    }
}
