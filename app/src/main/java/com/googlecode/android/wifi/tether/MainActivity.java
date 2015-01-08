package com.googlecode.android.wifi.tether;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = "TETHER -> MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startAdhoc = (Button) findViewById(R.id.start_adhoc);
        Button endAdhoc = (Button) findViewById(R.id.end_adhoc);
        startAdhoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "StartBtn pressed ...");
                // Sending intent to TetherServiceReceiver that we want to start the service-now
                Intent intent = new Intent(TetherService.SERVICEMANAGE_INTENT);
                intent.setAction(TetherService.SERVICEMANAGE_INTENT);
                intent.putExtra("state", TetherService.SERVICE_START);
                sendBroadcast(intent);
            }
        });
        endAdhoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sending intent to TetherServiceReceiver that we want to start the service-now
                Intent intent = new Intent(TetherService.SERVICEMANAGE_INTENT);
                intent.setAction(TetherService.SERVICEMANAGE_INTENT);
                intent.putExtra("state", TetherService.SERVICE_STOP);
                sendBroadcast(intent);
            }
        });
    }
}
