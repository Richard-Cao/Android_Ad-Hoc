package com.njupt.adhoc;

/**
 * Created by richardcao on 15/12/8.
 * 使Android手机支持接入adhoc网络
 * 测试通过：Android 4.0 Android 4.1
 */
public class AccessAdhoc {
    private static final String cmdDelete = "sed -i '/network/,$'d wpa_supplicant.conf",
            cmdReplaceap_scan = "sed -i 's/ap_scan=1/ap_scan=2/g' wpa_supplicant.conf",
            cmdAddupdate_config = "sed -i '$a update_config=0' wpa_supplicant.conf",
            cmdAddap_scan = "sed -i '$a ap_scan=2' wpa_supplicant.conf",
            cmdAddeapol_version = "sed -i '$a eapol_version=1' wpa_supplicant.conf",
            cmdReplacedevice_type = "sed -i 's/device_type=\\S\\+/device_type=0-00000000-0/g' wpa_supplicant.conf",
            cmdReplaceupdate_config = "sed -i 's/update_config=1/update_config=0/g' wpa_supplicant.conf",
            cmdAddnetwork = "sed -i '$a network={\\\nssid=\"sos\"\\\nmode=1\\\nkey_mgmt=NONE\\\npriority=0\\\nauth_alg=OPEN SHARED\\\nscan_ssid=1\\\nfrequency=2412\\\n}' wpa_supplicant.conf",
            queryap_scan = "grep -c 'ap_scan' wpa_supplicant.conf",
            queryupdate_config = "grep -c 'update_config' wpa_supplicant.conf",
            queryeapol_version = "grep -c 'eapol_version' wpa_supplicant.conf";

    public static void accessAdhoc() {
        Process process = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try {
            String cmd = "chmod 777 " + "data";
            String ap_scan, update_config, eapol_version;
            process = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(process.getOutputStream());
            dis = new DataInputStream(process.getInputStream());
            InputStreamReader isr = new InputStreamReader(dis);
            dos.writeBytes(cmd + "\n");
            dos.writeBytes("cd /data/misc/wifi" + "\n");
            dos.writeBytes(queryap_scan + "\n");
            ap_scan = getOutput(isr);
            dos.writeBytes(queryupdate_config + "\n");
            update_config = getOutput(isr);
            dos.writeBytes(queryeapol_version + "\n");
            eapol_version = getOutput(isr);
            dos.writeBytes(cmdDelete + "\n");
            if (ap_scan.equals("0")) {
                dos.writeBytes(cmdAddap_scan + "\n");
            } else {
                dos.writeBytes(cmdReplaceap_scan + "\n");
            }
            if (update_config.equals("0")) {
                dos.writeBytes(cmdAddupdate_config + "\n");
            } else {
                dos.writeBytes(cmdReplaceupdate_config + "\n");
            }
            if (eapol_version.equals("0")) {
                dos.writeBytes(cmdAddeapol_version + "\n");
            }
            dos.writeBytes(cmdReplacedevice_type + "\n");
            dos.writeBytes(cmdAddnetwork + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
            } catch (Exception e2) {
            }
        }
    }

    private static String getOutput(InputStreamReader inputStreamReader)
            throws IOException {
        BufferedReader br = new BufferedReader(inputStreamReader);
        return br.readLine();
    }
}
