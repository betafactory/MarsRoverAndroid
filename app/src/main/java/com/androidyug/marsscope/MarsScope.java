package com.androidyug.marsscope;

import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by IAMONE on 12/22/2015.
 */
public class MarsScope extends Application{

//    public static Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();

//        bus = new Bus(ThreadEnforcer.ANY);

    }

//    public static Bus provideBus(){
//        return bus;
//    }

}
