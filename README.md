#Android Ad-Hoc NetWork
基于Google的开源项目[android-wifi_tether](https://code.google.com/p/android-wifi-tether/)分离精简出来的adhoc发起组网独立模块

##使用要求
手机需要有ROOT权限，并且给本软件授权，点击发起组网即可，关闭的话点击关闭组网即可。

##使用步骤
###导入adhoclibrary库到自己的项目当中
修改自己app的gradle文件中的applicationId为"com.googlecode.android.wifi.tether"

在src/main文件夹中建立名为jniLibs的文件夹，然后再在jniLibs文件夹中建立armeabi文件夹，在armeabi文件夹中放入libwtnativetask.so库
简要说明路径为：app/src/main/jniLibs/armeabi/libwtnativetask.so

####Note：项目中jni文件为so库的google源代码，可以自己编译出libwtnativetask.so，这样就不需要改applicationId了。

###在自己项目的AndroidManifest.xml文件中做如下配置：
1.加入权限：
```xml
    <uses-permission android:name="android.permission.WRITE_INTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.CHANGE_WIMAX_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIMAX_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
```
2.配置：
```xml
<application
          android:name="com.googlecode.android.wifi.tether.TetherApplication"
          ...
          />
```
3.加入：
```xml
<service
         android:enabled="true"
         android:name="com.googlecode.android.wifi.tether.TetherService" />
<receiver
       android:name="com.googlecode.android.wifi.tether.TetherServiceReceiver"
       android:exported="false">
         <intent-filter>
             <action android:name="com.googlecode.android.wifi.tether.intent.MANAGE" />
         </intent-filter>
    </receiver>
```
###使用
发起adhoc组网：
```java
AdhocControl.start(getApplicationContext());
```
关闭adhoc组网：
```java
AdhocControl.stop(getApplicationContext());
```
借鉴并感谢[android-wifi-tether](https://code.google.com/p/android-wifi-tether/)项目的开源代码
