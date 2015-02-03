package com.njupt.adhoc;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.googlecode.android.wifi.tether.system.AdhocControl;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = "TETHER -> MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button startAdhoc = (Button) findViewById(R.id.start_adhoc);
        final Button endAdhoc = (Button) findViewById(R.id.end_adhoc);
        startAdhoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "StartBtn pressed ...");
                AdhocControl.start(getApplicationContext());
                startAdhoc.setClickable(false);
                endAdhoc.setClickable(true);
            }
        });
        endAdhoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdhocControl.stop(getApplicationContext());
                startAdhoc.setClickable(true);
                endAdhoc.setClickable(false);
            }
        });
    }
}
