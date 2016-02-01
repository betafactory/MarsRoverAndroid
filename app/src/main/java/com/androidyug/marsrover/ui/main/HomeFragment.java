package com.androidyug.marsrover.ui.main;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidyug.marsrover.NetworkConn;
import com.androidyug.marsrover.R;
import com.androidyug.marsrover.common.Constant;
import com.androidyug.marsrover.common.FontsFactory;
import com.androidyug.marsrover.common.Utils;
import com.androidyug.marsrover.common.VerticalSpace;
import com.androidyug.marsrover.data.model.Dataset;
import com.androidyug.marsrover.data.model.Photo;
import com.androidyug.marsrover.data.model.Rover;
import com.androidyug.marsrover.data.rest.MarsDataSourceImple;
import com.androidyug.marsrover.ui.intro.IntroActivity;
import com.androidyug.marsrover.ui.selectrover.SelectRoverActivity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = "HomeFragment";
    private static final int SELECT_ROVER_RQST_CODE = 1000;
    public static final String INTENT_ROVER_ID = "intent_rover_id";
    public static final String INTENT_ROVER_DEFAULT_DATE = "rover_landing_date";

    Bus bus;
    NetworkConn mNetworkConn;
    // requird by date picker
    Date mLandingDate = Utils.stringToDate(Constant.CURIOSITY_LANDING_DATE);
    Date mMaxDate;
    String  mQueryDate = Constant.CURIOSITY_LANDING_DATE;
    MarsDataSourceImple marsDataSourceImple;
    LinearLayoutManager llm;
    PhotoAdapter mPhotoAdapter;
    List<Photo> mDataList;
    
    int mRoverId = Constant.ROVER_CURIOUSITY;
    int mPage = 1;
    boolean mMorePage = false;
    private boolean mLoading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;


    @Bind(R.id.ll_root_home_fragment)
    LinearLayout llRoot;

    @Bind(R.id.rv_home)
    RecyclerView rvHome;

    @Bind(R.id.main_toolbar)
    Toolbar toolbar;

    @Bind(R.id.btn_date)
    Button btnDate;

    @Bind(R.id.tv_title)
    TextView tvTitle;

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bus = new Bus(ThreadEnforcer.ANY);
        llm = new LinearLayoutManager(getActivity());

        setHasOptionsMenu(true);
//        getActivity().
//        toolbar.setNavigationIcon();
//        toolbar.setNavigationContentDescription();
//        toolbar.setLogo();
//        toolbar.setLogoDescription();
//        toolbar.setNavigationOnClickListener();
//        toolbar.setTitle(null);

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, v);

        initializeActionToolbar();

        btnDate.setOnClickListener(this);
        tvTitle.setTypeface(FontsFactory.robotoCondensedBold(getActivity()));
        tvTitle.setText("CURIOSITY");
        marsDataSourceImple = new MarsDataSourceImple(getContext(),bus);
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        mQueryDate = pref.getString(Constant.PREFKEY_QDATE, Constant.CURIOSITY_LANDING_DATE);
        mRoverId = pref.getInt(Constant.PREFKEY_ROVER_ID, Constant.ROVER_CURIOUSITY);
        Log.d(LOG_TAG, "from on start" + mQueryDate + mRoverId);
        queryApi(mRoverId, mQueryDate, mPage);
        setTitle(mRoverId);
        btnDate.setText(mQueryDate);

        rvHome.setOnScrollListener(new RecyclerView.OnScrollListener() {

            boolean flag;
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                // Is scrolling up
                if (dy > 10) {

                    if (!flag) {

                        showToolbar();
                        flag = true;
                    }

                    // Is scrolling down
                } else if (dy < -10) {

                    if (flag) {

                        hideToolbar();
                        rvHome.setPadding(0,0,0,0);
                        flag = false;
                    }
                }



                if (dy > 0) { // scrolling vertically
                    visibleItemCount = llm.getChildCount();
//                    totalItemCount = llm.getItemCount();
                    totalItemCount = mPhotoAdapter.getItemCount();
                    pastVisiblesItems = llm.findFirstVisibleItemPosition();

                    if (mLoading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            mLoading = false;
                            Log.d("HomeFragment", "Last Item Wow !");
                            if (mMorePage) {
                                // make a query to load more
                                marsDataSourceImple.makeCallToService(mRoverId, mPage, mQueryDate);
                                Snackbar snackbar = Snackbar.make(llRoot, "LOADING MORE PHOTOS", Snackbar.LENGTH_LONG)
                                        .setAction("CANCEL", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
//                                                Toast.makeText(getActivity(), "snack action clicked", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setActionTextColor(Color.CYAN);

                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.DKGRAY);
                                TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(Color.GREEN);
                                snackbar.show();
                                //Do pagination.. i.e. fetch new data
                            }
                        }
                    }


                }

            }
        });

        return v;
    }


    private void showToolbar() {

        toolbar.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.translate_up_off));
    }

    private void hideToolbar() {

        toolbar.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.translate_up_on));

    }

    @Override
    public void onStart() {
        super.onStart();

        bus.register(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        saveToSharedPref(mRoverId, mQueryDate);
        getActivity().unregisterReceiver(mNetworkConn);
    }

    @Override
    public void onResume() {
        super.onResume();
        mNetworkConn = new NetworkConn(llRoot);
        getActivity().registerReceiver(mNetworkConn,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }



    void initilizeRecyclerView(List<Photo> dataset){
        mPhotoAdapter = new PhotoAdapter(getActivity(), dataset);
        rvHome.setLayoutManager(llm);
        rvHome.addItemDecoration(new VerticalSpace(20));
        rvHome.setAdapter(mPhotoAdapter);
    }

    void initializeActionToolbar(){
        toolbar.setTitle("");
        HomeActivity homeActivity = ((HomeActivity) getActivity());
        homeActivity.setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_action_overflow));
        toolbar.setNavigationIcon(R.drawable.ic_menu_main);
        toolbar.setPopupTheme(android.R.style.Widget_Material_Light_PopupWindow);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SelectRoverActivity.class);
                startActivityForResult(i, SELECT_ROVER_RQST_CODE);
            }
        });
        homeActivity.getSupportActionBar().setTitle("");
        homeActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);

    }

    @Subscribe
    public void onResponse(Dataset dataset){
        progressBar.setVisibility(View.GONE);
        btnDate.setVisibility(View.VISIBLE);
        List<Photo> photos = dataset.getPhotos();
        Log.d("HomeFragment", "" + photos.size());

        if (mDataList == null){ // mPhotoAdapter == null
            mDataList = photos;
            initilizeRecyclerView(mDataList);
            if (photos.size()>0) {
                Rover rover = photos.get(0).getRover();
                mMaxDate = Utils.stringToDate(rover.getMaxDate());
            }
            Log.d(LOG_TAG, "mLandingDate " + mLandingDate);
            Log.d(LOG_TAG, "mMaxDate " + mMaxDate);
            if (photos.size()==25) {
                mPage++;
                mMorePage = true;
            }

        } else { // if mDatalist !=null append photo into this

            if (photos.size()==25){
                mPage++;
                mMorePage = true;
                mLoading = true;
            } else {
                mMorePage = false;
                mLoading = false;
            }

            mDataList.addAll(photos);
            mPhotoAdapter.notifyItemRangeInserted((mDataList.size()), photos.size()); // check here
//            Toast.makeText(getActivity(), ""+photos.size(), Toast.LENGTH_SHORT).show();
        }

    }


    void saveToSharedPref(int roverId, String qDate){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putInt(Constant.PREFKEY_ROVER_ID, roverId);
        edit.putString(Constant.PREFKEY_QDATE, qDate);
        edit.commit();
        Log.d(LOG_TAG, "===========================================");
        Log.d(LOG_TAG, "saved to pref:" + roverId + " " + qDate);
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id){
            case R.id.btn_date:
                final Calendar c = Calendar.getInstance(); // default time to show in dialog datepicker
                c.setTime(Utils.stringToDate(mQueryDate));
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);


                        // passed 2 for old device like datepicker on lollipop
                        DatePickerDialog dialog = new DatePickerDialog(getActivity(),2, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                mDataList.clear();
                                mPhotoAdapter.notifyDataSetChanged();
                                mPage = 1;
                                mMorePage = false;

                        // TODO: 1/5/2016 refactor code a/t below two line code
//                        mDataList.clear();
//                        mPhotoAdapter.notifyDataSetChanged();



                        // update the mQueryDate variable
                        Calendar cal  = Calendar.getInstance();
                        cal.set(year, monthOfYear, dayOfMonth);
                        mQueryDate = Utils.readableDate(cal.getTime());
                        Log.d(LOG_TAG, "mQueryDate:" + mQueryDate);

                        btnDate.setText(mQueryDate);

                        queryApi(mRoverId, mQueryDate, mPage);

//                        Toast.makeText(getActivity(), "dateSEt", Toast.LENGTH_SHORT).show();
                    }

                }, year, month, day);

                DatePicker dp = dialog.getDatePicker();
                dp.setSpinnersShown(true);
                dp.setCalendarViewShown(false);
                dp.setMinDate(mLandingDate.getTime());
                dp.setMaxDate(mMaxDate.getTime());

                dialog.show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        Toast.makeText(getActivity(), "from onActitivy Result", Toast.LENGTH_SHORT).show();
        if (requestCode == SELECT_ROVER_RQST_CODE && resultCode== Activity.RESULT_OK){


            mRoverId = data.getIntExtra(INTENT_ROVER_ID, -1);
            mQueryDate = data.getStringExtra(INTENT_ROVER_DEFAULT_DATE);
            mLandingDate = Utils.stringToDate(mQueryDate);

            setTitle(mRoverId);

            mPage = 1;
            mMorePage = false;

            if (mDataList!=null)  // chances is mDataList is not initilize from on response and you select different rover causing null pointer exception
                mDataList.clear();

            if (mPhotoAdapter!=null)
                mPhotoAdapter.notifyDataSetChanged();

            btnDate.setVisibility(View.GONE);
            btnDate.setText(mQueryDate);

            queryApi(mRoverId, mQueryDate, mPage);

        }
    }

    void setTitle(int roverId){

        if (roverId == Constant.ROVER_CURIOUSITY){
            tvTitle.setText("CURIOSITY");
        } else if (mRoverId == Constant.ROVER_OPPORTUNITY){
            tvTitle.setText("OPPORTUNITY");
        } else if (mRoverId == Constant.ROVER_SPIRIT){
            tvTitle.setText("SPIRIT");
        }
    }

    void queryApi(int roverId, String qdate, int page){
        progressBar.setVisibility(View.VISIBLE);
        marsDataSourceImple.makeCallToService(roverId, page, qdate);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_about:
                startActivity(new Intent(getActivity(), IntroActivity.class));
                break;
//            case R.id.action_about_dev:
//                Toast.makeText(getActivity(), "about_dev", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.action_opensource:
//                Toast.makeText(getActivity(), "opensource", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.action_rate_us:
//                Toast.makeText(getActivity(), "rateus", Toast.LENGTH_SHORT).show();
//                break;
        }

        return true;
    }

}
