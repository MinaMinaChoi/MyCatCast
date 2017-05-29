package com.example.cmina.mycastcast.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.example.cmina.mycastcast.activity.MainActivity;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.cmina.mycastcast.activity.CameraFilterActivity.anInt;
import static com.example.cmina.mycastcast.activity.CameraFilterActivity.convertNativeGray;
import static com.example.cmina.mycastcast.activity.CameraFilterActivity.mGauss;
import static com.example.cmina.mycastcast.activity.CameraFilterActivity.mGray;
import static com.example.cmina.mycastcast.activity.CameraFilterActivity.nativeCanny;
import static com.example.cmina.mycastcast.activity.MainActivity.editor;

/**
 * Created by cmina on 2017-03-13.
 */

public class ImageTask extends AsyncTask<Bitmap, Void, Boolean> {

    Context context;
    String fileName;
    private ProgressDialog dialog;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("이미지 저장중");
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Bitmap... params) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        try {
            mGray = new Mat();//Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.IMREAD_UNCHANGED);
            mGauss = new Mat();

            if (anInt == 0) {
                Utils.bitmapToMat(params[0], mGray);
                convertNativeGray(mGray.getNativeObjAddr(), mGauss.getNativeObjAddr());
                Utils.matToBitmap(mGauss, params[0]);

            } else if (anInt == 1) {
                Utils.bitmapToMat(params[0], mGray);
                convertNativeGray(mGray.getNativeObjAddr(), mGauss.getNativeObjAddr());
                nativeCanny(mGauss.getNativeObjAddr());
                Utils.matToBitmap(mGauss, params[0]);

            } else if (anInt ==2) {
                Utils.bitmapToMat(params[0], mGray);
                Imgproc.medianBlur(mGray, mGauss, 11);
                Utils.matToBitmap(mGauss, params[0]);
            } else if (anInt == 3) {
                Utils.bitmapToMat(params[0], mGray);
                Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_RGB2RGBA);
                Utils.matToBitmap(mGray, params[0]);
            }

            //화면 회전을 위한 matrix객체 생성
            Matrix matrix = new Matrix();
            matrix.setRotate(90, (float)params[0].getWidth(), (float)params[0].getHeight());
            Bitmap rotateBitmap = Bitmap.createBitmap(params[0], 0, 0, params[0].getWidth(), params[0].getHeight(), matrix, false);
            //기존 비트맵 자원해제
            params[0].recycle();

            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CvException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if (aBoolean) {
            dialog.dismiss();
            //success
            Toast.makeText(context, "이미지 저장 완료", Toast.LENGTH_SHORT).show();
            editor.putString(SaveSharedPreference.getUserUnique(context), fileName);
            editor.commit();
        } else {
            dialog.dismiss();
            Toast.makeText(context, "카메라 촬영에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

}
