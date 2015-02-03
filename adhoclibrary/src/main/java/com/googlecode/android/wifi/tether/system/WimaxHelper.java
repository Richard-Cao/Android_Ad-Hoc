package com.googlecode.android.wifi.tether.system;

import android.content.Context;

import java.lang.reflect.Method;

public class WimaxHelper {

    public void htcWimax(Context context, boolean state) {

        //http://forum.xda-developers.com/archive/index.php/t-909206.html
        Object htcWimaxManager = context.getSystemService("wimax");

        Method setWimaxEnabled = null;
        try {
            setWimaxEnabled = htcWimaxManager.getClass().getMethod("setWimaxEnabled",
                    new Class[]{Boolean.TYPE});
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (state) {
                if (setWimaxEnabled != null) {
                    setWimaxEnabled.invoke(htcWimaxManager, Boolean.TRUE);
                }
            } else {
                if (setWimaxEnabled != null) {
                    setWimaxEnabled.invoke(htcWimaxManager, Boolean.FALSE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void samsungWimax(Context context, boolean state) {
        //http://forum.xda-developers.com/archive/index.php/t-909206.html
        Object samsungWimaxManager = context.getSystemService("WiMax");
        Method setWimaxEnabled = null;
        try {
            setWimaxEnabled = samsungWimaxManager.getClass().getMethod("setWimaxEnabled",
                    new Class[]{Boolean.TYPE});
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (state) {
                if (setWimaxEnabled != null) {
                    setWimaxEnabled.invoke(samsungWimaxManager, Boolean.TRUE);
                }
            } else {
                if (setWimaxEnabled != null) {
                    setWimaxEnabled.invoke(samsungWimaxManager, Boolean.FALSE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
