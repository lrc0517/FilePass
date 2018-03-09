package com.ruitai.sockettrans;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Created by AA on 2017/3/24.
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    public static final String ROOT_PATH = Environment.getExternalStorageDirectory() + File.separator + "socketDemo/";



    /**
     * 根据文件路径获取文件名称
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath) {
        if(TextUtils.isEmpty(filePath)) {
            return "";
        }
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    }

    /**
     * 生成本地文件路径
     * @param filePath
     * @return
     */
    public static File gerateLocalFile(String filePath) {
        String fileNmae = getFileName(filePath);
        File dirFile = new File(ROOT_PATH);
        if(!dirFile.exists()) {
            dirFile.mkdirs();
        }
        return new File(dirFile, fileNmae);
    }

    /**
     * 转换文件大小
     *
     * @param fileSize
     * @return
     */
    public static String FormetFileSize(long fileSize) {
        if(fileSize <= 0) {
            return "0KB";
        }

        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileSize < 1024) {
            fileSizeString = df.format((double) fileSize) + "B";
        } else if (fileSize < 1048576) {
            fileSizeString = df.format((double) fileSize / 1024) + "K";
        } else if (fileSize < 1073741824) {
            fileSizeString = df.format((double) fileSize / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileSize / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 取得文件大小
     *
     * @param f
     * @return
     * @throws Exception
     */
    @SuppressWarnings("resource")
    public static long getFileSizes(File f) throws Exception {
        long size = 0;
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
            size = fis.available();
        } else {
            f.createNewFile();
        }
        return size;
    }


    /**
     * 获得指定文件的byte数组
     */
    private byte[] getBytes(String filePath){
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG,"getBytes Error --> "+ e);
        }
        return buffer;
    }

    /**
     * 根据byte数组，生成文件
     */
    public static void getFile(byte[] bfile, String filePath,String fileName) {
        Log.e(TAG,"getFile s ");
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            Log.e(TAG,"getFile dir = " +dir);
            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在
                dir.mkdirs();
                Log.e(TAG,"getFile mkdirs = " +dir);
            }

            file = new File(filePath+"\\"+fileName+".jpg");

            Log.e(TAG,"getFile file = " +file);

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);

            Log.e(TAG,"getFile write ");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"getFile Error --> "+ e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        Log.e(TAG,"getFile e ");
    }

    public static void saveBitmap(Bitmap bitmap, String filePath, String fileName) {

        Log.e(TAG, "保存图片");
        File f = new File(filePath, fileName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    //获取系统时间 视频保存的时间
    public static String getDate() {
        Calendar mCalendar = Calendar.getInstance();
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DATE);
        int hour = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);
        String date = "" + year + (month + 1) + day + hour + minute + second;
        Log.d("date", "date:" + date);
        return date;
    }

    //获取SD卡路径
    public static String getSDPath() {
        File sdDir = null;
        // 判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取根目录
            return sdDir.toString();
        }
        return null;
    }


}
