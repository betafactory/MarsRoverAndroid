package com.androidyug.marsscope.ui.intro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.androidyug.marsscope.R;

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

        switch (pagePos) {
            case 1:
                // more info one
                tvInfo.setText(getResources().getText(R.string.app_desc));
                break;
            case 2:
                //more info two
                tvInfo.setText(getResources().getText(R.string.spirit_desc));
                break;
            case 3:
                // about developer
                tvInfo.setText(Html.fromHtml(getResources().getText(R.string.opportunity_desc).toString()));
                break;
            case 4:
                // enter button
                tvInfo.setVisibility(View.GONE);
                btnEnter.setVisibility(View.VISIBLE);
                btnEnter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
