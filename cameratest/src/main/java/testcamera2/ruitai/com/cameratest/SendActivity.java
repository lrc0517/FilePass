package testcamera2.ruitai.com.cameratest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SendActivity extends Activity {

    private static final String TAG = "SendActivity";
    private TextView tvMsg;
    private EditText txtEt;
    private CameraSurfaceView mCameraSurface;
    private SocketManager socketManager;

    public static final int SOCKET_PORT = 9999;

    public static final int sVideoWidthRatio = 1;
    public static final int sVideoHeightRatio = 1;
    public static final int sVideoQuality = 80;

    private Timer mTimer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                    txtEt.append("\n[" + format.format(new Date()) + "]" + msg.obj.toString());
                    break;
                case 1:
                    tvMsg.setText("本机IP：" + GetIpAddress() + " 监听端口:" + msg.obj.toString());
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        initView();
        iniData();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    private void initListener() {
        mCameraSurface.setOnCameraSurfaceFrame(new CameraSurfaceView.OnCameraSurfaceFrame() {
            @Override
            public void onFrame(byte[] frameData, Camera camera) {
                if (frameData != null) {
                   /* YuvImage image = new YuvImage(frameData, videoFormatIndex, videoWidth, videoHeight, null);
                    if (image != null) {
                        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                        //在此设置图片的尺寸和质量
                        image.compressToJpeg(new Rect(0, 0, (int) (sVideoWidthRatio * videoWidth),
                                (int) (sVideoHeightRatio * videoHeight)), sVideoQuality, outstream);
                        outstream.flush();

                    }*/
                    //Trans Camera data to jpg data
                    Camera.Size size = camera.getParameters().getPreviewSize();//获取大小
                    YuvImage image = new YuvImage(frameData, ImageFormat.NV21, size.width, size.height, null);            //ImageFormat.NV21  640 480
                    ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 80, outputSteam); // 将NV21格式图片，以质量70压缩成Jpeg，并得到JPEG数据流
                    byte[] jpegData = outputSteam.toByteArray();                                                //从outputSteam得到byte数据
                    if (!isThreadWorking) {
                        isThreadWorking = true;
                        waitForFdetThreadComplete();
                        mSaveThread = new SaveBitmapThread(handler, getApplicationContext());
                        mSaveThread.setData(jpegData);
                        mSaveThread.start();
                    }

                }
            }
        });
    }

    private void iniData() {
        if (!isWifiApOpen(this)){
            Toast.makeText(getApplicationContext(),"请打开热点",Toast.LENGTH_SHORT).show();
            finish();
        }


        socketManager = new SocketManager(handler);
        mTimer = new Timer();
        mTimer.schedule(mClearTask, 10000, 5000);

    }

    private void initView() {
        tvMsg = (TextView) findViewById(R.id.tvMsg);
        txtEt = (EditText) findViewById(R.id.et);
        mCameraSurface = (CameraSurfaceView) findViewById(R.id.sf_presend_view);

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


    //+=============================================================================+//
    private ArrayList<String> getConnectedHotIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }

    //+=============================================================================+//
    String filePath = Environment.getExternalStorageDirectory().getPath() + "/Socket";
    private File mCurrentFile;

    private void save(InputStream is) {
        Log.e(TAG, "save");
        FileOutputStream fos = null;
        String fileName = "Send" + FileUtils.getDate();

        try {
            File dir = new File(filePath);
            Log.e(TAG, "getFile dir = " + dir);
            if (!dir.exists()) {//判断文件目录是否存在
                boolean mkdirs = dir.mkdirs();
                Log.e(TAG, "getFile dir = " + dir + ",mk = " + mkdirs);
            }
            mCurrentFile = new File(filePath + "/" + fileName + ".jpg");
            if (mCurrentFile.exists()) {

            }

            Log.e(TAG, "getFile file = " + mCurrentFile);
            fos = new FileOutputStream(mCurrentFile);
            byte[] buffer = new byte[1024 * 4];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "save Error -->" + e);
        } finally {
            try {
                is.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isThreadWorking = false;
            }
        }, 1000);
    }

    public SaveBitmapThread mSaveThread;
    private boolean isThreadWorking = false;

    private void waitForFdetThreadComplete() {
        if (mSaveThread == null) {
            return;
        }

        if (mSaveThread.isAlive()) {
            try {
                mSaveThread.join();
                mSaveThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    class SaveBitmapThread extends Thread {

        private Handler handler;
        private byte[] jpegData = null;
        private Context ctx;
        private Bitmap faceCroped;

        public SaveBitmapThread(Handler handler, Context ctx) {
            this.ctx = ctx;
            this.handler = handler;
        }

        public void setData(byte[] data) {
            this.jpegData = data;
        }

        public void run() {
            //Trans jpg data to bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            InputStream input = new ByteArrayInputStream(jpegData);
            //FileUtils.getFile(data, filePath, fileName);
            save(input);
            sendToSocket();
        }
    }

    private void sendToSocket() {
        ArrayList<String> connectedHotIP = getConnectedHotIP();
        final ArrayList<String> fileNames = new ArrayList<>();
        final ArrayList<String> paths = new ArrayList<>();
        paths.add(mCurrentFile.getPath());
        fileNames.add(mCurrentFile.getName());
        for (String connIp : connectedHotIP) {
            final String ip = connIp;
            Log.e(TAG, "ip =" + ip);
            Message.obtain(handler, 0, "正在发送至" + ip + ":" + SOCKET_PORT).sendToTarget();
            Thread sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    socketManager.SendFile(fileNames, paths, ip, SOCKET_PORT);
                }
            });
            sendThread.start();
        }
    }

    //+=============================================================================+//
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
    public  boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //通过放射获取 getWifiApState()方法
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int state = (int) method.invoke(manager);
            //通过放射获取 WIFI_AP的开启状态属性
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(manager);
            //判断是否开启
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }
    //+=============================================================================+//
}
