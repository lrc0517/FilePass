package com.ruitai.sockettrans;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;

/**
 * Created by static on 2017/10/27.
 */

public class CameraSurfaceView extends SurfaceView {

    public static final String TAG = "CameraSurfaceView";
    private WindowManager mWm;
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private Context mContext;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }


    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initData();
    }

    private void initData() {
        mWm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);// 为surfaceView的holder对象添加回调函数，
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //+================================================================+//
    private int mCameraId;

    // get Camera
    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(findCamera(false));
        } catch (Exception e) {
            Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
        }
        return c;
    }

    private int findCamera(boolean isFront) {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (isFront) {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                    mCameraId = camIdx;
                    return camIdx;
                }
            } else {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCameraId = camIdx;
                    return camIdx;
                }
            }
        }
        return -1;
    }
    //+================================================================+//

    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mDisplayRotation;
    private int mDisplayOrientation;

    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        // Set the PreviewSize and AutoFocus:
        setOptimalPreviewSize(parameters, width, height);
        setAutoFocus(parameters);
        // And set the parameters:
        mCamera.setParameters(parameters);
    }

    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = Util.getOptimalPreviewSize(mWm, previewSizes, targetRatio);
        mPreviewWidth = previewSize.width;
        mPreviewHeight = previewSize.height;
        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);

    }

    private void setAutoFocus(Camera.Parameters cameraParameters) {
        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    private void setDisplayOrientation() {
        // Now set the display orientation:
        mDisplayRotation = Util.getDisplayRotation(mWm);
        mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, mCameraId);

        Log.i(TAG, "mDisplayOrientation = " + mDisplayOrientation + ",mDisplayRotation =" + mDisplayRotation);
        mCamera.setDisplayOrientation(mDisplayOrientation);
    }

    private void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.setPreviewCallback(mPreViewCallBack);
        }
    }


    //+================================================================+//

    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoFormatIndex;

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {



        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            mCamera = getCameraInstance();
            //mRecorder.setCamera(mCamera);
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (Exception e) {
                Log.e(TAG, "Could not preview the image -->", e);
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width1,
                                   int height1) {

            mPreviewWidth = width1;
            mPreviewHeight = height1;

            if (holder.getSurface() == null) {
                return;
            }
            // Try to stop the current preview:
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // Ignore...
            }

            configureCamera(width1, height1);
            setDisplayOrientation();
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            mVideoWidth=size.width;
            mVideoHeight=size.height;
            mVideoFormatIndex=parameters.getPreviewFormat();
            startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
        }
    };

    private Camera.PreviewCallback mPreViewCallBack = new Camera.PreviewCallback() {


        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mOnCameraSurfaceFrame != null) {

                mOnCameraSurfaceFrame.onFrame(data,camera);
            }
        }
    };
    //+================================================================+//
    private OnCameraSurfaceFrame mOnCameraSurfaceFrame;

    public interface OnCameraSurfaceFrame {
        void onFrame(byte[] frameData, Camera camera);
    }

    public void setOnCameraSurfaceFrame(OnCameraSurfaceFrame l) {
        mOnCameraSurfaceFrame = l;
    }
    //+================================================================+//
}
