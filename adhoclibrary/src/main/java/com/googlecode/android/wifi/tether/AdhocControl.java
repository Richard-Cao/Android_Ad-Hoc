package com.googlecode.android.wifi.tether;

import android.content.Context;
import android.content.Intent;

/**
 * Created by caolicheng on 15/2/3.
 */
public class AdhocControl {

    public synchronized static void start(Context context, TetherApplication application, String ssid) {
        application.preferenceEditor.putString("ssidpref", ssid).apply();
        // Sending intent to TetherServiceReceiver that we want to start the service-now
        Intent intent = new Intent(TetherService.SERVICEMANAGE_INTENT);
        intent.setAction(TetherService.SERVICEMANAGE_INTENT);
        intent.putExtra("state", TetherService.SERVICE_START);
        context.sendBroadcast(intent);
    }

    public synchronized static void stop(Context context) {
        // Sending intent to TetherServiceReceiver that we want to stop the service-now
        Intent intent = new Intent(TetherService.SERVICEMANAGE_INTENT);
        intent.setAction(TetherService.SERVICEMANAGE_INTENT);
        intent.putExtra("state", TetherService.SERVICE_STOP);
        context.sendBroadcast(intent);
    }
}
