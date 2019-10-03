package com.androidyug.marsrover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast

/**
 * Created by IAMONE on 1/8/2016.
 */
class NetworkConn(internal var mView: View) : BroadcastReceiver() {
    private var mSnackbar: Snackbar? = null

    override fun onReceive(context: Context, intent: Intent) {
        /*
        * pull down refresh to refresh when internet is active
        * broadcast receiver so that you can able to monitor internet
        *
        * */

        if (!isOnline(context)) {
            Toast.makeText(context, "Internet Connection Lost", Toast.LENGTH_SHORT).show()
            mSnackbar = Snackbar.make(mView, "INTERNET CONNECTION LOST", Snackbar.LENGTH_INDEFINITE)
            mSnackbar!!.show()
        } else {

            if (mSnackbar != null)
                mSnackbar!!.dismiss()
        }
    }


    fun isOnline(context: Context): Boolean {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        //should check null because in air plan mode it will be null
        return netInfo != null && netInfo.isConnected

    }

}
