package com.njupt.adhoc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.googlecode.android.wifi.tether.AdhocControl;
import com.googlecode.android.wifi.tether.TetherApplication;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "TETHER -> MainActivity";
    private TetherApplication application = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.application = (TetherApplication) this.getApplication();
        final Button startAdhoc = (Button) findViewById(R.id.start_adhoc);
        final Button endAdhoc = (Button) findViewById(R.id.end_adhoc);
        startAdhoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdhocControl.start(getApplicationContext(), application, "sos");
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
