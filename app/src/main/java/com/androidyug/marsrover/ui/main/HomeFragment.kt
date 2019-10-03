package com.androidyug.marsrover.ui.main

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import butterknife.BindView
import com.androidyug.marsrover.NetworkConn
import com.androidyug.marsrover.R
import com.androidyug.marsrover.common.Constant
import com.androidyug.marsrover.common.FontsFactory
import com.androidyug.marsrover.common.Utils
import com.androidyug.marsrover.common.VerticalSpace
import com.androidyug.marsrover.data.model.Dataset
import com.androidyug.marsrover.data.model.Photo
import com.androidyug.marsrover.data.model.Rover
import com.androidyug.marsrover.data.rest.MarsDataSourceImple
import com.androidyug.marsrover.ui.intro.IntroActivity
import com.androidyug.marsrover.ui.selectrover.SelectRoverActivity
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.squareup.otto.ThreadEnforcer

import java.util.Calendar
import java.util.Date

import butterknife.ButterKnife

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */
class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var bus: Bus
    private lateinit var mNetworkConn: NetworkConn
    // requird by date picker
    private var mLandingDate = Utils.stringToDate(Constant.CURIOSITY_LANDING_DATE)
    private lateinit var mMaxDate: Date
    private var mQueryDate: String = Constant.CURIOSITY_LANDING_DATE
    private lateinit var marsDataSourceImple: MarsDataSourceImple
    private lateinit var llm: LinearLayoutManager
    private lateinit var mPhotoAdapter: PhotoAdapter
    private var mDataList: MutableList<Photo>? = null

    private var mRoverId = Constant.ROVER_CURIOUSITY
    private var mPage = 1
    private var mMorePage = false
    private var mLoading = true
    private var pastVisiblesItems: Int = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0

    @BindView(R.id.ll_root_home_fragment)
    lateinit var llRoot: LinearLayout

    @BindView(R.id.rv_home)
    lateinit var rvHome: RecyclerView

    @BindView(R.id.main_toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.btn_date)
    lateinit var btnDate: Button

    @BindView(R.id.tv_title)
    lateinit var tvTitle: TextView

    @BindView(R.id.progress_bar)
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bus = Bus(ThreadEnforcer.ANY)
        llm = LinearLayoutManager(activity)

        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_home, container, false)
        ButterKnife.bind(this, v)

        initializeActionToolbar()

        btnDate.setOnClickListener(this)
        tvTitle.typeface = FontsFactory.robotoCondensedBold(activity)
        tvTitle.text = "CURIOSITY"
        marsDataSourceImple = MarsDataSourceImple(context, bus)
        val pref = activity.getPreferences(Context.MODE_PRIVATE)
        mQueryDate = pref.getString(Constant.PREFKEY_QDATE, Constant.CURIOSITY_LANDING_DATE)
        mRoverId = pref.getInt(Constant.PREFKEY_ROVER_ID, Constant.ROVER_CURIOUSITY)
        Log.d(LOG_TAG, "from on start$mQueryDate$mRoverId")
        queryApi(mRoverId, mQueryDate, mPage)
        setTitle(mRoverId)
        btnDate.text = mQueryDate

        rvHome.setOnScrollListener(object : RecyclerView.OnScrollListener() {

            private var flag: Boolean = false
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)


                // Is scrolling up
                if (dy > 10) {

                    if (!flag) {

                        showToolbar()
                        flag = true
                    }

                    // Is scrolling down
                } else if (dy < -10) {

                    if (flag) {

                        hideToolbar()
                        rvHome.setPadding(0, 0, 0, 0)
                        flag = false
                    }
                }



                if (dy > 0) { // scrolling vertically
                    visibleItemCount = llm.childCount
                    //                    totalItemCount = llm.getItemCount();
                    totalItemCount = mPhotoAdapter!!.itemCount
                    pastVisiblesItems = llm.findFirstVisibleItemPosition()

                    if (mLoading) {
                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                            mLoading = false
                            Log.d("HomeFragment", "Last Item Wow !")
                            if (mMorePage) {
                                // make a query to load more
                                marsDataSourceImple.makeCallToService(mRoverId, mPage, mQueryDate)
                                val snackbar = Snackbar.make(llRoot!!, "LOADING MORE PHOTOS", Snackbar.LENGTH_LONG)
                                        .setAction("CANCEL") {
                                            //                                                Toast.makeText(getActivity(), "snack action clicked", Toast.LENGTH_SHORT).show();
                                        }
                                        .setActionTextColor(Color.CYAN)

                                val snackbarView = snackbar.view
                                snackbarView.setBackgroundColor(Color.DKGRAY)
                                val textView = snackbarView.findViewById<View>(android.support.design.R.id.snackbar_text) as TextView
                                textView.setTextColor(Color.GREEN)
                                snackbar.show()
                                //Do pagination.. i.e. fetch new data
                            }
                        }
                    }


                }

            }
        })

        return v
    }


    private fun showToolbar() {

        toolbar.startAnimation(AnimationUtils.loadAnimation(activity,
                R.anim.translate_up_off))
    }

    private fun hideToolbar() {

        toolbar.startAnimation(AnimationUtils.loadAnimation(activity,
                R.anim.translate_up_on))

    }

    override fun onStart() {
        super.onStart()

        bus.register(this)

    }

    override fun onStop() {
        super.onStop()
        bus.unregister(this)
    }

    override fun onPause() {
        super.onPause()

        saveToSharedPref(mRoverId, mQueryDate)
        activity.unregisterReceiver(mNetworkConn)
    }

    override fun onResume() {
        super.onResume()
        mNetworkConn = NetworkConn(llRoot!!)
        activity.registerReceiver(mNetworkConn,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }


    private fun initilizeRecyclerView(dataset: List<Photo>) {
        mPhotoAdapter = PhotoAdapter(activity, dataset)
        rvHome.layoutManager = llm
        rvHome.addItemDecoration(VerticalSpace(20))
        rvHome.adapter = mPhotoAdapter
    }

    private fun initializeActionToolbar() {
        toolbar.title = ""
        val homeActivity = activity as HomeActivity
        homeActivity.setSupportActionBar(toolbar)
        toolbar.overflowIcon = resources.getDrawable(R.drawable.ic_action_overflow)
        toolbar.setNavigationIcon(R.drawable.ic_menu_main)
        toolbar.popupTheme = android.R.style.Widget_Material_Light_PopupWindow

        toolbar.setNavigationOnClickListener {
            val i = Intent(activity, SelectRoverActivity::class.java)
            startActivityForResult(i, SELECT_ROVER_RQST_CODE)
        }
        homeActivity.supportActionBar!!.title = ""
        homeActivity.supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_action_menu)

    }

    @Subscribe
    internal fun onResponse(dataset: Dataset) {
        progressBar.visibility = View.GONE
        btnDate.visibility = View.VISIBLE
        val photos = mutableListOf<Photo>()
        photos.addAll(dataset.photos)
        Log.d("HomeFragment", "" + photos.size)

        if (mDataList == null) { // mPhotoAdapter == null
            mDataList = photos
            initilizeRecyclerView(mDataList!!)
            if (photos.isNotEmpty()) {
                val rover = photos[0].getRover()
                mMaxDate = Utils.stringToDate(rover!!.getMaxDate())
            }
            Log.d(LOG_TAG, "mLandingDate $mLandingDate")
            Log.d(LOG_TAG, "mMaxDate $mMaxDate")
            if (photos.size == 25) {
                mPage++
                mMorePage = true
            }

        } else { // if mDatalist !=null append photo into this

            if (photos.size == 25) {
                mPage++
                mMorePage = true
                mLoading = true
            } else {
                mMorePage = false
                mLoading = false
            }

            mDataList!!.addAll(photos)
            mPhotoAdapter!!.notifyItemRangeInserted(mDataList!!.size, photos.size) // check here
            //            Toast.makeText(getActivity(), ""+photos.size(), Toast.LENGTH_SHORT).show();
        }

    }


    private fun saveToSharedPref(roverId: Int, qDate: String?) {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        val edit = sharedPref.edit()
        edit.putInt(Constant.PREFKEY_ROVER_ID, roverId)
        edit.putString(Constant.PREFKEY_QDATE, qDate)
        edit.commit()
        Log.d(LOG_TAG, "===========================================")
        Log.d(LOG_TAG, "saved to pref:$roverId $qDate")
    }


    @SuppressLint("ResourceType")
    override fun onClick(v: View) {

        val id = v.id

        when (id) {
            R.id.btn_date -> {
                val c = Calendar.getInstance() // default time to show in dialog datepicker
                c.time = Utils.stringToDate(mQueryDate)
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)


                // passed 2 for old device like datepicker on lollipop
                val dialog = DatePickerDialog(activity, 2, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    mDataList!!.clear()
                    mPhotoAdapter!!.notifyDataSetChanged()
                    mPage = 1
                    mMorePage = false

                    // TODO: 1/5/2016 refactor code a/t below two line code
                    //                        mDataList.clear();
                    //                        mPhotoAdapter.notifyDataSetChanged();


                    // update the mQueryDate variable
                    val cal = Calendar.getInstance()
                    cal.set(year, monthOfYear, dayOfMonth)
                    mQueryDate = Utils.readableDate(cal.time)
                    Log.d(LOG_TAG, "mQueryDate:" + mQueryDate)

                    btnDate.text = mQueryDate

                    queryApi(mRoverId, mQueryDate, mPage)

                    //                        Toast.makeText(getActivity(), "dateSEt", Toast.LENGTH_SHORT).show();
                }, year, month, day)

                val dp = dialog.datePicker
                dp.spinnersShown = true
                dp.calendarViewShown = false
                dp.minDate = mLandingDate.time
                dp.maxDate = mMaxDate.time

                dialog.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //        Toast.makeText(getActivity(), "from onActitivy Result", Toast.LENGTH_SHORT).show();
        if (requestCode == SELECT_ROVER_RQST_CODE && resultCode == Activity.RESULT_OK) {

            mRoverId = data!!.getIntExtra(INTENT_ROVER_ID, -1)
            mQueryDate = data.getStringExtra(INTENT_ROVER_DEFAULT_DATE)
            mLandingDate = Utils.stringToDate(mQueryDate)

            setTitle(mRoverId)

            mPage = 1
            mMorePage = false

            if (mDataList != null)
            // chances is mDataList is not initilize from on response and you select different rover causing null pointer exception
                mDataList!!.clear()

            if (mPhotoAdapter != null)
                mPhotoAdapter!!.notifyDataSetChanged()

            btnDate.visibility = View.GONE
            btnDate.text = mQueryDate

            queryApi(mRoverId, mQueryDate, mPage)

        }
    }

    private fun setTitle(roverId: Int) {

        when {
            roverId == Constant.ROVER_CURIOUSITY -> tvTitle.text = "CURIOSITY"
            mRoverId == Constant.ROVER_OPPORTUNITY -> tvTitle.text = "OPPORTUNITY"
            mRoverId == Constant.ROVER_SPIRIT -> tvTitle.text = "SPIRIT"
        }
    }

    private fun queryApi(roverId: Int, qdate: String, page: Int) {
        progressBar.visibility = View.VISIBLE
        marsDataSourceImple.makeCallToService(roverId, page, qdate)
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> startActivity(Intent(activity, IntroActivity::class.java))
        }//            case R.id.action_about_dev:
        //                Toast.makeText(getActivity(), "about_dev", Toast.LENGTH_SHORT).show();
        //                break;
        //            case R.id.action_opensource:
        //                Toast.makeText(getActivity(), "opensource", Toast.LENGTH_SHORT).show();
        //                break;
        //            case R.id.action_rate_us:
        //                Toast.makeText(getActivity(), "rateus", Toast.LENGTH_SHORT).show();
        //                break;

        return true
    }

    companion object {

        private const val LOG_TAG = "HomeFragment"
        private const val SELECT_ROVER_RQST_CODE = 1000
        const val INTENT_ROVER_ID = "intent_rover_id"
        const val INTENT_ROVER_DEFAULT_DATE = "rover_landing_date"
    }

}
