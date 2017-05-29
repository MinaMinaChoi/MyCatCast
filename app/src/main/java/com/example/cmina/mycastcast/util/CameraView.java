package com.example.cmina.mycastcast.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;


/**
 * Created by cmina on 2017-03-13.
 */

public class CameraView extends JavaCameraView implements Camera.PictureCallback {

    private static final String TAG = "CameraView";
  //  ProgressDialog dialog;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void takePicture() {
        Log.i(TAG, "Taking picture");
     //   this.mPictureFileName = fileName;
        // Call to garbage collector to avoid bug http://code.opencv.org/issues/2961
        System.gc();
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        mCamera.setPreviewCallback(null);
        mCamera.setOneShotPreviewCallback(null);

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FOCUS_MODE_AUTO);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this);
       // dialog.dismiss();

    }

    /**
     * reduces the size of the image
     * @param image
     * @param maxSize
     * @return
     */
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }



    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
/*

        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("이미지 저장중...");
        dialog.show();
*/

        Bitmap image = null;
        image = BitmapFactory.decodeByteArray(data, 0, data.length);
        image = getResizedBitmap(image, 500);

        ImageTask task = new ImageTask();
        task.context = getContext();
        task.execute(image);

        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

    }


}
