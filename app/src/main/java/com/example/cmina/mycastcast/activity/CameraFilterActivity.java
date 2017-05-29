package com.example.cmina.mycastcast.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.util.CameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by cmina on 2017-03-13.
 */

public class CameraFilterActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static final String TAG = "OpenCV::CameraFilterActivity";

    public static int anInt = 4;
    private Button gray, edge, blur, blue;

    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS = {"android.permission.CAMERA", Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,};


    private CameraView mOpenCvCameraView;
    public static Mat mGray;
    public static Mat mGauss;

    static {

        System.loadLibrary("native-lib");
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_camfilter);

        gray = (Button) findViewById(R.id.gray);
        edge = (Button) findViewById(R.id.edge);
        blur = (Button) findViewById(R.id.blur);
        blue = (Button) findViewById(R.id.blue);

        gray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gray.setBackgroundResource(R.color.colorHighlight);
                edge.setBackgroundResource(R.color.colorAccent);
                blur.setBackgroundResource(R.color.colorAccent);
                blue.setBackgroundResource(R.color.colorAccent);
               // Toast.makeText(CameraFilterActivity.this, "그레이효과를 선택했습니다.", Toast.LENGTH_SHORT).show();
                anInt = 0;
            }
        });

        edge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gray.setBackgroundResource(R.color.colorAccent);
                edge.setBackgroundResource(R.color.colorHighlight);
                blur.setBackgroundResource(R.color.colorAccent);
                blue.setBackgroundResource(R.color.colorAccent);
                //Toast.makeText(CameraFilterActivity.this, "엣찌효과를 선택했습니다.", Toast.LENGTH_SHORT).show();
                anInt = 1;
            }
        });

        blur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gray.setBackgroundResource(R.color.colorAccent);
                edge.setBackgroundResource(R.color.colorAccent);
                blur.setBackgroundResource(R.color.colorHighlight);
                blue.setBackgroundResource(R.color.colorAccent);
              //  Toast.makeText(CameraFilterActivity.this, "블러효과를 선택했습니다.", Toast.LENGTH_SHORT).show();
                anInt = 2;
            }
        });

        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gray.setBackgroundResource(R.color.colorAccent);
                edge.setBackgroundResource(R.color.colorAccent);
                blur.setBackgroundResource(R.color.colorAccent);
                blue.setBackgroundResource(R.color.colorHighlight);
              //  Toast.makeText(CameraFilterActivity.this, "공포효과를 선택했습니다.", Toast.LENGTH_SHORT).show();
                anInt = 3;
            }
        });

        if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        } else {
            //이미 사용자에게 퍼미션 허가를 받음.
        }

        mOpenCvCameraView = (CameraView) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        // mOpenCvCameraView.setFocusableInTouchMode(true);
        //    mOpenCvCameraView.getFocusables(View.FOCUS_UP);
        // mOpenCvCameraView.setMaxFrameSize(1000, 800);

        //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mOpenCvCameraView.setCvCameraViewListener(this);


    }


    public void onCameraViewStarted(int width, int height) {
        // Initialize the Mat objects when the Camera starts
        mGray = new Mat(height, width, CvType.CV_8UC4);
        mGauss = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mGray.release();
        mGauss.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


        if (anInt == 0) {
            //그레이화2
            mGray = inputFrame.rgba();
            convertNativeGray(mGray.getNativeObjAddr(), mGauss.getNativeObjAddr());
            return mGauss;
        } else if (anInt == 1) {
            //엣지
            //mGray = inputFrame.gray();
            mGray = inputFrame.rgba();
            convertNativeGray(mGray.getNativeObjAddr(), mGauss.getNativeObjAddr());
            nativeCanny(mGauss.getNativeObjAddr());
            return mGauss;
        } else if (anInt == 2) {
            mGray = inputFrame.rgba();
            // Then we perform a Gaussian blur on mGray and save it in the mGauss
            Imgproc.medianBlur(mGray, mGauss, 13);
            // Imgproc.GaussianBlur(mGray, mGauss, new Size(), 5);
            return mGauss;
        } else if (anInt == 3) {

            mGauss = inputFrame.rgba();
            Imgproc.cvtColor(mGauss, mGauss, Imgproc.COLOR_RGB2RGBA);
            //   Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_RGB2YCrCb);//기괴스럽...초록..
            //Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_RGB2HLS);//이상 기괴..

            return mGauss;

        } else {

            return inputFrame.rgba();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // If OpenCV loaded then enable the CameraView
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(CameraFilterActivity.this);

                }
                break;

                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.i(TAG, "onTouch event");
        try {
            mOpenCvCameraView.takePicture();
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this, "촬영 중 오류가 발생했습니다. 다시 시도하세요", Toast.LENGTH_SHORT).show();
        }


        return false;
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean camreaAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= M) {

                        if (!camreaAccepted) {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        } else {
                            //이미 사용자에게 퍼미션 허가를 받음.
                        }
                    }
                }
                break;
        }
    }

    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions) {
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)) {
                //퍼미션 허가 안된 경우
                return false;
            }

        }
        //모든 퍼미션이 허가된 경우
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void showDialogforPermission(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(CameraFilterActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }


    public static native boolean nativeCanny(long iAddr);

    public static native int convertNativeGray(long matAddrRgba, long matAddrGray);
}
