package com.androidyug.marsrover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by IAMONE on 1/8/2016.
 */
public class NetworkConn extends BroadcastReceiver {
    View mView;
    Snackbar mSnackbar;

    public NetworkConn(View view){

        this.mView = view;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        /*
        * pull down refresh to refresh when internet is active
        * broadcast receiver so that you can able to monitor internet
        *
        * */

        if (!isOnline(context)){
            Toast.makeText(context, "Internet Connection Lost", Toast.LENGTH_SHORT).show();
            mSnackbar = Snackbar.make(mView, "INTERNET CONNECTION LOST", Snackbar.LENGTH_INDEFINITE);
            mSnackbar.show();
        } else {

            if (mSnackbar!=null)
                mSnackbar.dismiss();
        }
    }


    public boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in air plan mode it will be null
        return (netInfo != null && netInfo.isConnected());

    }

}
