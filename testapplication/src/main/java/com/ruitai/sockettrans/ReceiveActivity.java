package com.ruitai.sockettrans;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ReceiveActivity extends Activity {

    private static final String TAG = "ReceiveActivity";

    private TextView mTvMsg;
    private EditText mTxtEt;
    private ImageView mIvReceive;
    private SocketManager socketManager;

    private Timer mTimer;
    public static final int SOCKET_PORT = 9999;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                    mTxtEt.append("\n[" + format.format(new Date()) + "]" + msg.obj.toString());
                    break;
                case 1:
                    mTvMsg.setText("本机IP：" + GetIpAddress() + " 监听端口:" + msg.obj.toString());
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case 1001:
                    final Uri uri = Uri.fromFile(new File(msg.obj.toString()));
                    Log.e(TAG, "uri = " + uri);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (uri != null)
                                mIvReceive.setImageURI(uri);
                        }
                    });
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        initView();
        iniData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    private void iniData() {
        //if (!isWifiConne(getApplicationContext())) {
        if (!isWifiConnected(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "请连接到指定无线网", Toast.LENGTH_SHORT).show();
            finish();
        }
        mTimer = new Timer();
        mTimer.schedule(mClearTask, 10000, 5000);
        socketManager = new SocketManager(handler);
    }


    //+=============================================================================+//
    public String GetIpAddress() {
        @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int i = wifiInfo.getIpAddress();
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }

    private void initView() {
        mTvMsg = (TextView) findViewById(R.id.tvMsg);
        mTxtEt = (EditText) findViewById(R.id.et);
        mIvReceive = (ImageView) findViewById(R.id.iv_receive);
    }

    //+=============================================================================+//
    String filePath = Environment.getExternalStorageDirectory().getPath() + "/Socket";

    public void clearData() {
        File dir = new File(filePath);
        File[] listFiles = dir.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.getName().endsWith(".jpg")) {
                    Log.e(TAG, "clearData file = " + file);
                    file.delete();
                }
            }
        }

    }

    private TimerTask mClearTask = new TimerTask() {
        @Override
        public void run() {
            clearData();
        }
    };

    //+=============================================================================+//
    public static boolean isWifiConne(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        Log.e(TAG, "wifiInfo = " + wifiInfo);
        Log.e(TAG, "ssid = " + ssid);
        if ("RuiTai_Ap".equals(ssid)) {
            return true;
        } else {
            //ScanResult
        }
        return false;
    }


    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }

        return false;
    }
    //+=============================================================================+//


}
