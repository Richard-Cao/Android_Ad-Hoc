/**
 *  This program is free software; you can redistribute it and/or modify it under 
 *  the terms of the GNU General Public License as published by the Free Software 
 *  Foundation; either version 3 of the License, or (at your option) any later 
 *  version.
 *  You should have received a copy of the GNU General Public License along with 
 *  this program; if not, see <http://www.gnu.org/licenses/>. 
 *  Use this application at your own risk.
 *
 *  Copyright (c) 2009 by Harald Mueller and Sofia Lemons.
 */

package com.googlecode.android.wifi.tether;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


import com.googlecode.android.wifi.tether.system.Configuration;
import com.googlecode.android.wifi.tether.system.CoreTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

public class TetherApplication extends Application {

    public static final String MSG_TAG = "TETHER -> TetherApplication";

    public final String DEFAULT_PASSPHRASE = "abcdefghijklm";
    public final String DEFAULT_LANNETWORK = "192.168.2.0/24";
    public final String DEFAULT_ENCSETUP = "wpa_supplicant";

    // TetherService
    //private TetherService tetherService = null;

    // Client-Connect-Thread
    public static final int CLIENT_CONNECT_ACDISABLED = 0;
    public static final int CLIENT_CONNECT_AUTHORIZED = 1;
    public static final int CLIENT_CONNECT_NOTAUTHORIZED = 2;

    //public String tetherNetworkDevice = null;

    // PowerManagement
    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;

    // Preferences
    public SharedPreferences settings = null;
    public SharedPreferences.Editor preferenceEditor = null;

    // Intents
    private PendingIntent mainIntent;
    private PendingIntent accessControlIntent;

    //device string for setup
    String device = "Unknown";

    // Whitelist
    public CoreTask.Whitelist whitelist = null;
    // Supplicant
    public CoreTask.WpaSupplicant wpasupplicant = null;
    // TiWlan.conf
    public CoreTask.TiWlanConf tiwlan = null;
    // tether.conf
    public CoreTask.TetherConfig tethercfg = null;

    // CoreTask
    public CoreTask coretask = null;

    // Configuration
    public Configuration configuration = null;

    @Override
    public void onCreate() {
        Log.d(MSG_TAG, "Calling onCreate()");

        //create CoreTask
        this.coretask = new CoreTask();
        this.coretask.setPath(this.getApplicationContext().getFilesDir().getParent());
        Log.d(MSG_TAG, "Current directory is " + this.getApplicationContext().getFilesDir().getParent());

        // Check Homedir, or create it
        this.checkDirs();

        // Preferences
        this.settings = PreferenceManager.getDefaultSharedPreferences(this);

        // preferenceEditor
        this.preferenceEditor = settings.edit();

        // Whitelist
        this.whitelist = this.coretask.new Whitelist();

        // Supplicant config
        this.wpasupplicant = this.coretask.new WpaSupplicant();

        // tiwlan.conf
        this.tiwlan = this.coretask.new TiWlanConf();

        // tether.cfg
        this.tethercfg = this.coretask.new TetherConfig();
        this.tethercfg.read();

        // Init Device flag
        this.device = android.os.Build.DEVICE;

        // Powermanagement
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TETHER_WAKE_LOCK");

        // Initialize configuration
        this.updateDeviceParameters();
    }

    @Override
    public void onTerminate() {
        Log.d(MSG_TAG, "Calling onTerminate()");
        // Stopping Tether
        //tetherService.stop();
    }

    public void updateDeviceParameters() {
        String device = this.settings.getString("devicepref", "auto");
        if (device.equals("auto")) {
            this.configuration = new Configuration();
        } else {
            this.configuration = new Configuration(device);
        }
    }

    public Configuration getDeviceParameters() {
        return this.configuration;
    }

    public void updateConfiguration() {

        long startStamp = System.currentTimeMillis();

        // Updating configuration
        updateDeviceParameters();

        boolean encEnabled = this.settings.getBoolean("encpref", false);
        boolean acEnabled = this.settings.getBoolean("acpref", false);
        String ssid = this.settings.getString("ssidpref", "AndroidTether");
        String txpower = this.settings.getString("txpowerpref", "disabled");
        String lannetwork = this.settings.getString("lannetworkpref", DEFAULT_LANNETWORK);
        boolean currentMacSpoofEnabled = this.settings.getBoolean("tether.macspoof", false);
        String currentMAC = this.settings.getString("macspoof.addr", "00:11:22:33:44:55");
        String wepkey = this.settings.getString("passphrasepref", DEFAULT_PASSPHRASE);
        String wepsetupMethod = this.settings.getString("encsetuppref", DEFAULT_ENCSETUP);
        String channel = this.settings.getString("channelpref", "1");
        boolean mssclampingEnabled = this.settings.getBoolean("mssclampingpref", this.coretask.isMSSClampingSupported());
        boolean routefixEnabled = this.settings.getBoolean("routefixpref", this.coretask.isRoutefixSupported());
        String primaryDns = this.settings.getString("dnsprimarypref", "8.8.8.8");
        String secondaryDns = this.settings.getString("dnssecondarypref", "8.8.4.4");
        boolean hideSSID = this.settings.getBoolean("hidessidpref", false);
        boolean reloadDriver = this.settings.getBoolean("driverreloadpref", false);
        boolean reloadDriver2 = this.settings.getBoolean("driverreloadpref2", true);
        boolean netdMaxClientCmd = this.settings.getBoolean("netd.maxclientcmd", false);
        // Check if "auto"-setup method is selected
        String setupMethod = this.settings.getString("setuppref", "auto");
        boolean netdNoIfaceCmd = this.settings.getBoolean("netd.notetherifacecmd", this.coretask.isNdcNoTetherCmdSupported());

        if (!configuration.isTiadhocSupported()) {
            if (setupMethod.equals("auto")) {
                setupMethod = configuration.getAutoSetupMethod();
            }
        } else {
            setupMethod = "tiwlan0";
        }

        // tether.conf
        String subnet = lannetwork.substring(0, lannetwork.lastIndexOf("."));
        //this.tethercfg.read();
        this.tethercfg.put("device.type", configuration.getDevice());
        this.tethercfg.put("wifi.essid", ssid);
        this.tethercfg.put("wifi.channel", channel);
        this.tethercfg.put("ip.network", lannetwork.split("/")[0]);
        this.tethercfg.put("ip.gateway", subnet + ".254");
        this.tethercfg.put("ip.netmask", "255.255.255.0");

        //macspoof
        if (currentMacSpoofEnabled) {
            this.tethercfg.put("tether.macspoof", "true");
        } else {
            this.tethercfg.put("tether.macspoof", "false");
        }
        this.tethercfg.put("macspoof.addr", currentMAC);


        // dns
        this.tethercfg.put("dns.primary", primaryDns);
        this.tethercfg.put("dns.secondary", secondaryDns);

        if (mssclampingEnabled) {
            this.tethercfg.put("mss.clamping", "true");
        } else {
            this.tethercfg.put("mss.clamping", "false");
        }

        if (netdMaxClientCmd) {
            //netdndcmaxclientcmd sets max clients to 25, true might fix stuff
            this.tethercfg.put("netd.maxclientcmd", "true");
        } else {
            this.tethercfg.put("netd.maxclientcmd", "false");
        }

        if (netdNoIfaceCmd) {
            this.tethercfg.put("netd.noifacecmd", "true");
        } else {
            this.tethercfg.put("netd.noifacecmd", "false");
        }

        if (hideSSID) {
            this.tethercfg.put("wifi.essid.hide", "1");
        } else {
            this.tethercfg.put("wifi.essid.hide", "0");
        }

        //wifi driver reload inside tether script
        if (reloadDriver) {
            this.tethercfg.put("wifi.driver.reload", "true");
        } else {
            this.tethercfg.put("wifi.driver.reload", "false");
        }

        //TODO: wifi driver hack for outside tether script
        if (reloadDriver2) {
            this.tethercfg.put("wifi.driver.reload2", "true");
        } else {
            this.tethercfg.put("wifi.driver.reload2", "false");
        }

        if (routefixEnabled) {
            this.tethercfg.put("tether.fix.route", "true");
        } else {
            this.tethercfg.put("tether.fix.route", "false");
        }

        if (configuration.doWifiFinalDriverLoad()) {
            this.tethercfg.put("wifi.final.load.cmd", Configuration.getWifiFinalloadCmd());
        } else {
            this.tethercfg.put("wifi.final.load.cmd", "none");
        }

        // Write tether-section variable
        this.tethercfg.put("setup.section.generic", "" + configuration.isGenericSetupSection());

        // Wifi-interface
        if (this.coretask.getProp("wifi.interface").equals("undefined")) {
            //TODO: put in better undefined check.  this wires it to netd's interface
            this.tethercfg.put("wifi.interface", configuration.getNetdInterface());
        } else {
            this.tethercfg.put("wifi.interface", this.coretask.getProp("wifi.interface"));
        }

        this.tethercfg.put("wifi.driver", setupMethod);
        if (setupMethod.equals("wext")) {
            this.tethercfg.put("tether.interface", this.tethercfg.get("wifi.interface"));
            if (encEnabled) {
                this.tethercfg.put("wifi.encryption", "wep");
            }
        } else if (setupMethod.equals("netd") || setupMethod.equals("netdndc")) {
            this.tethercfg.put("tether.interface", configuration.getNetdInterface());
            if (encEnabled) {
                this.tethercfg.put("wifi.encryption", configuration.getEncryptionIdentifier());
            } else {
                this.tethercfg.put("wifi.encryption", configuration.getOpennetworkIdentifier());
            }
        } else if (setupMethod.equals("hostapd")) {
            this.tethercfg.put("hostapd.module.path", configuration.getHostapdKernelModulePath());
            this.tethercfg.put("hostapd.module.name", configuration.getHostapdKernelModuleName());
            this.tethercfg.put("hostapd.bin.path", configuration.getHostapdPath());
            this.tethercfg.put("tether.interface", configuration.getHostapdInterface());
            if (encEnabled) {
                this.tethercfg.put("wifi.encryption", "unused");
            }
            if (configuration.getHostapdLoaderCmd() == null || configuration.getHostapdLoaderCmd().length() <= 0) {
                this.tethercfg.put("hostapd.loader.cmd", "disabled");
            } else {
                this.tethercfg.put("hostapd.loader.cmd", configuration.getHostapdLoaderCmd());
            }
        } else if (setupMethod.equals("tiwlan0")) {
            this.tethercfg.put("tether.interface", configuration.getTiadhocInterface());
            if (encEnabled) {
                this.tethercfg.put("wifi.encryption", "wep");
            }
        } else if (setupMethod.startsWith("softap")) {
            this.tethercfg.put("tether.interface", configuration.getSoftapInterface());
            this.tethercfg.put("wifi.firmware.path", configuration.getSoftapFirmwarePath());
            if (encEnabled) {
                this.tethercfg.put("wifi.encryption", configuration.getEncryptionIdentifier());
            } else {
                this.tethercfg.put("wifi.encryption", configuration.getOpennetworkIdentifier());
            }
        }

        this.tethercfg.put("wifi.load.cmd", Configuration.getWifiLoadCmd());
        this.tethercfg.put("wifi.unload.cmd", Configuration.getWifiUnloadCmd());

        this.tethercfg.put("wifi.txpower", txpower);

        // Encryption
        if (encEnabled) {
            // Storing wep-key
            this.tethercfg.put("wifi.encryption.key", wepkey);

            // Getting encryption-method if setup-method on auto
            if (wepsetupMethod.equals("auto")) {
                if (configuration.isWextSupported()) {
                    wepsetupMethod = "iwconfig";
                } else if (configuration.isTiadhocSupported()) {
                    wepsetupMethod = "wpa_supplicant";
                }
            }
            // Setting setup-mode
            this.tethercfg.put("wifi.setup", wepsetupMethod);
            // Prepare wpa_supplicant-config if wpa_supplicant selected
            if (wepsetupMethod.equals("wpa_supplicant")) {
                // Install wpa_supplicant.conf-template
                if (!this.wpasupplicant.exists()) {
                    this.installWpaSupplicantConfig();
                }

                // Update wpa_supplicant.conf
                Hashtable<String, String> values = new Hashtable<String, String>();
                values.put("ssid", "\"" + this.settings.getString("ssidpref", "AndroidTether") + "\"");
                values.put("wep_key0", "\"" + this.settings.getString("passphrasepref", DEFAULT_PASSPHRASE) + "\"");
                this.wpasupplicant.write(values);
            }
        } else {
            this.tethercfg.put("wifi.encryption", "open");
            this.tethercfg.put("wifi.encryption.key", "none");

            // Make sure to remove wpa_supplicant.conf
            if (this.wpasupplicant.exists()) {
                this.wpasupplicant.remove();
            }
        }

        // DNS Ip-Range
        String[] lanparts = lannetwork.split("\\.");
        this.tethercfg.put("dhcp.iprange", lanparts[0] + "." + lanparts[1] + "." + lanparts[2] + ".100," + lanparts[0] + "." + lanparts[1] + "." + lanparts[2] + ".108,12h");

        // writing config-file
        if (!this.tethercfg.write()) {
            Log.e(MSG_TAG, "Unable to update tether.conf!");
        }

        // whitelist
        if (acEnabled) {
            if (!this.whitelist.exists()) {
                try {
                    this.whitelist.touch();
                } catch (IOException e) {
                    Log.e(MSG_TAG, "Unable to update whitelist-file!");
                    e.printStackTrace();
                }
            }
        } else {
            if (this.whitelist.exists()) {
                this.whitelist.remove();
            }
        }

        if (configuration.isTiadhocSupported()) {
            TetherApplication.this.copyFile(CoreTask.DATA_FILE_PATH + "/conf/tiwlan.ini", "0644", R.raw.tiwlan_ini);
            Hashtable<String, String> values = this.tiwlan.get();
            values.put("dot11DesiredSSID", this.settings.getString("ssidpref", "AndroidTether"));
            values.put("dot11DesiredChannel", this.settings.getString("channelpref", "1"));
            this.tiwlan.write(values);
        } else {
            File tiwlanconf = new File(CoreTask.DATA_FILE_PATH + "/conf/tiwlan.ini");
            if (tiwlanconf.exists()) {
                tiwlanconf.delete();
            }
        }

        Log.d(MSG_TAG, "Creation of configuration-files took ==> " + (System.currentTimeMillis() - startStamp) + " milliseconds.");
    }

    public String getTetherNetworkDevice() {
        return this.tethercfg.get("tether.interface");
    }

    // gets user preference on whether wakelock should be disabled during tethering
    public boolean isWakeLockDisabled() {
        return this.settings.getBoolean("wakelockpref", true);
    }

    // WakeLock
    public void releaseWakeLock() {
        try {
            if (this.wakeLock != null && this.wakeLock.isHeld()) {
                Log.d(MSG_TAG, "Trying to release WakeLock NOW!");
                this.wakeLock.release();
            }
        } catch (Exception ex) {
            Log.d(MSG_TAG, "Ups ... an exception happend while trying to release WakeLock - Here is what I know: " + ex.getMessage());
        }
    }

    public void acquireWakeLock() {
        try {
            if (!this.isWakeLockDisabled()) {
                Log.d(MSG_TAG, "Trying to acquire WakeLock NOW!");
                this.wakeLock.acquire();
            }
        } catch (Exception ex) {
            Log.d(MSG_TAG, "Ups ... an exception happend while trying to acquire WakeLock - Here is what I know: " + ex.getMessage());
        }
    }

    public boolean binariesExists() {
        File file = new File(CoreTask.DATA_FILE_PATH + "/bin/tether");
        return file.exists();
    }

    public void installWpaSupplicantConfig() {
        this.copyFile(CoreTask.DATA_FILE_PATH + "/conf/wpa_supplicant.conf", "0644", R.raw.wpa_supplicant_conf);
    }

    public void installHostapdConfig(String hostapdTemplate) {
        switch (hostapdTemplate) {
            case "droi":
                this.copyFile(CoreTask.DATA_FILE_PATH + "/conf/hostapd.conf", "0644", R.raw.hostapd_conf_droi);
                break;
            case "mini":
                this.copyFile(CoreTask.DATA_FILE_PATH + "/conf/hostapd.conf", "0644", R.raw.hostapd_conf_mini);
                break;
            case "tiap":
                this.copyFile(CoreTask.DATA_FILE_PATH + "/conf/hostapd.conf", "0644", R.raw.hostapd_conf_tiap);
                break;
        }
    }

    Handler displayMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                TetherApplication.this.displayToastMessage((String) msg.obj);
            }
            super.handleMessage(msg);
        }
    };

    public void installFiles() {
        String message = null;
        // tether
        if (message == null) {
            message = TetherApplication.this.copyFile(CoreTask.DATA_FILE_PATH + "/bin/tether", "0755", R.raw.tether);
        }
        // dnsmasq
        if (message == null) {
            message = TetherApplication.this.copyFile(CoreTask.DATA_FILE_PATH + "/bin/dnsmasq", "0755", R.raw.dnsmasq);
        }
        // iptables
        if (message == null) {
            message = TetherApplication.this.copyFile(CoreTask.DATA_FILE_PATH + "/bin/iptables", "0755", R.raw.iptables);
        }
        // iwconfig
        if (message == null) {
            message = TetherApplication.this.copyFile(CoreTask.DATA_FILE_PATH + "/bin/iwconfig", "0755", R.raw.iwconfig);
        }
        // ifconfig
        if (message == null) {
            message = TetherApplication.this.copyFile(CoreTask.DATA_FILE_PATH + "/bin/ifconfig", "0755", R.raw.ifconfig);
        }
        // rfkill
        if (message == null) {
            message = TetherApplication.this.copyFile(CoreTask.DATA_FILE_PATH + "/bin/rfkill", "0755", R.raw.rfkill);
        }
        /*
		if (configuration.enableFixPersist()) {	
			// fixpersist.sh
			if (message == null) {
				message = TetherApplication.this.copyFile(TetherApplication.this.coretask.DATA_FILE_PATH+"/bin/fixpersist.sh", "0755", R.raw.fixpersist_sh);
			}				
		}*/
        // edify script
        if (message == null) {
            TetherApplication.this.copyFile(CoreTask.DATA_FILE_PATH + "/conf/tether.edify", "0644", R.raw.tether_edify);
        }
        // tether.cfg
		/*if (message == null) {
			TetherApplication.this.copyFile(TetherApplication.this.coretask.DATA_FILE_PATH+"/conf/tether.conf", "0644", R.raw.tether_conf);
		}*/

        // wpa_supplicant drops privileges, we need to make files readable.
        TetherApplication.this.coretask.chmod(CoreTask.DATA_FILE_PATH + "/conf/", "0755");

        if (message == null) {
            message = getString(R.string.global_application_installed);
        }

        // Sending message
        Message msg = new Message();
        msg.obj = message;
        displayMessageHandler.sendMessage(msg);
    }

    private String copyFile(String filename, String permission, int ressource) {
        String result = this.copyFile(filename, ressource);
        if (result != null) {
            return result;
        }
        if (!this.coretask.chmod(filename, permission)) {
            result = "Can't change file-permission for '" + filename + "'!";
        }
        return result;
    }

    private String copyFile(String filename, int ressource) {
        File outFile = new File(filename);
        Log.d(MSG_TAG, "Copying file '" + filename + "' ...");
        InputStream is = this.getResources().openRawResource(ressource);
        byte buf[] = new byte[1024];
        int len;
        try {
            OutputStream out = new FileOutputStream(outFile);
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            is.close();
        } catch (IOException e) {
            return "Couldn't install file - " + filename + "!";
        }
        return null;
    }

    private void checkDirs() {
        File dir = new File(CoreTask.DATA_FILE_PATH);
        if (!dir.exists()) {
            this.displayToastMessage("Application data-dir does not exist!");
        } else {
            //String[] dirs = { "/bin", "/var", "/conf", "/library" };
            String[] dirs = {"/bin", "/var", "/conf"};
            for (String dirname : dirs) {
                dir = new File(CoreTask.DATA_FILE_PATH + dirname);
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        this.displayToastMessage("Couldn't create " + dirname + " directory!");
                    }
                } else {
                    Log.d(MSG_TAG, "Directory '" + dir.getAbsolutePath() + "' already exists!");
                }
            }
        }
    }

    // Display Toast-Message
    public void displayToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
