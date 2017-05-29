package com.example.cmina.mycastcast.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.fragment.SettingFragment;
import com.example.cmina.mycastcast.util.SaveSharedPreference;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.cmina.mycastcast.activity.ContentActivity.bitmap;
import static com.example.cmina.mycastcast.activity.MainActivity.editor;

/**
 * Created by cmina on 2017-03-10.
 */

public class ImageFilterActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    ImageView imageView;
    SeekBar bright_seekbar;
    Spinner spinner;
    Button applyBtn;
    Bitmap image;
    Bitmap filterImage;
    Integer integer = 0;

    private static final String TAG = "ImageFilterActivity";

    static {

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.e(TAG, "OpenCV loaded");
        }
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


    public String saveBitmaptoJpeg(Bitmap bitmap) {
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/" + "MyCastCast" + "/";
        String file_name = System.currentTimeMillis() + ".jpg";

        String string_path = ex_storage + foler_name;

        File file_path;
        try {
            file_path = new File(string_path);
            if (!file_path.isDirectory()) {
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path + file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        } catch (FileNotFoundException exception) {
            Log.e("FileNotFoundException", exception.getMessage());
        } catch (IOException exception) {
            Log.e("IOException", exception.getMessage());
        }

        return string_path + file_name;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imgfilter);

        Intent i = getIntent();
        String imageUri = i.getStringExtra("imageUri");

        imageView = (ImageView) findViewById(R.id.image);
        bright_seekbar = (SeekBar) findViewById(R.id.seekBar);
        bright_seekbar.setOnSeekBarChangeListener(this);
        spinner = (Spinner) findViewById(R.id.spinner);
        applyBtn = (Button) findViewById(R.id.applyBtn);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //필터적용한 이미지 저장하기.
                String filePath = saveBitmaptoJpeg(filterImage);
                //쉐어드에 저장하고, settingfragment에 가서, profile에 적용해주기
                editor.putString(SaveSharedPreference.getUserUnique(ImageFilterActivity.this), filePath);
                editor.commit();

                //setting fragment로
                Intent intent = new Intent(ImageFilterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        imageView.setImageURI(Uri.parse(imageUri));

        try {
            image = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageUri));
            image = getResizedBitmap(image, 500);
            filterImage = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageUri));
            filterImage = getResizedBitmap(filterImage, 500);

        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.filter, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {

                    case 0:
                        imageView.setImageBitmap(image);
                        integer=1;
                        break;
                    case 1:
                        Mat tmp = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
                        Utils.bitmapToMat(image, tmp);
                        //가우시안블러, 메디안블러
                        Imgproc.medianBlur(tmp, tmp, 17);
                      // Imgproc.GaussianBlur(tmp, tmp, new Size(), 5);
                        Utils.matToBitmap(tmp, filterImage);
                        imageView.setImageBitmap(filterImage);
                        integer=0;
                        break;
                    case 2: //공포
                        tmp = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
                        Utils.bitmapToMat(image, tmp);
                        Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2RGBA);
                        Utils.matToBitmap(tmp, filterImage);
                        imageView.setImageBitmap(filterImage);
                        integer=0;

                        break;
                    case 3: //gray
                        tmp = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
                        Utils.bitmapToMat(image, tmp);
                        Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BGR2GRAY);
                        Utils.matToBitmap(tmp, filterImage);
                        imageView.setImageBitmap(filterImage);
                        integer=0;

                        break;
                    case 4: //edge
                        Mat imgMat = new Mat();
                        Utils.bitmapToMat(image, imgMat);
                        Mat interMat = new Mat();  // intermediate

                        Imgproc.cvtColor(imgMat, interMat, Imgproc.COLOR_BGR2GRAY);
                        Imgproc.Canny(imgMat, interMat, 10, 100, 3, true);
                        Utils.matToBitmap(interMat, filterImage);
                        imageView.setImageBitmap(filterImage);
                        integer=0;

                        break;
                    default:
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private Bitmap increaseBrightness(Bitmap bitmap, int value) {

        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, src);
        src.convertTo(src, -1, 1, value);
        Bitmap result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, result);
        return result;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Bitmap edited = null;
        if (integer == 0 ) {
           edited = increaseBrightness(filterImage, progress);
        } else {
            edited = increaseBrightness(image, progress);
        }

        imageView.setImageBitmap(edited);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
