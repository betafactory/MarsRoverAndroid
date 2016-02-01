package com.androidyug.marsrover.ui.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidyug.marsrover.R;
import com.androidyug.marsrover.data.model.Dataset;
import com.androidyug.marsrover.data.model.Photo;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * @author Nitin Anand (nitinnatural@gmail.com)
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnMakeRqst;
    TextView tvResult;
    ImageView ivResult;
    Bus bus;
    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bus = new Bus(ThreadEnforcer.ANY);

        bus.register(this);


        tvResult = (TextView) findViewById(R.id.tv_result);
        ivResult = (ImageView) findViewById(R.id.iv_result);

        btnMakeRqst = (Button) findViewById(R.id.btn_make_rqst);
        btnMakeRqst.setOnClickListener(this);


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_make_rqst){
            Log.d(LOG_TAG, "clicked");

        }
    }

    @Subscribe
    public void onRespponse(Dataset dataset){
       // String res = response.raw().toString();
        //Toast.makeText(this, "from onResponse: " + res, Toast.LENGTH_SHORT).show();
        List<Photo> data = dataset.getPhotos();
        for(Photo p : data){
            Photo fotu  = p;
            Log.d(LOG_TAG, "" + fotu.getImgSrc());
            Log.d(LOG_TAG, "" + fotu.getEarthDate());
            Log.d(LOG_TAG, "=======================================");
        }
        String imgToLoad = data.get(0).getImgSrc();
        Picasso.with(this).load(imgToLoad).into(ivResult);
        tvResult.setText(""+data.size());

    }

    @Subscribe
    public void onResponseTest(String txt){
        Toast.makeText(this, "from onResponse: "+txt, Toast.LENGTH_SHORT).show();
    }


}
