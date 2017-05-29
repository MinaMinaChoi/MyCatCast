package com.example.cmina.mycastcast.fragment;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.CameraFilterActivity;
import com.example.cmina.mycastcast.activity.ImageFilterActivity;
import com.example.cmina.mycastcast.activity.MainActivity;
import com.example.cmina.mycastcast.service.MyFirebaseMessagingService;
import com.example.cmina.mycastcast.util.SaveSharedPreference;
import com.facebook.login.LoginManager;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.app.Activity.RESULT_OK;
import static com.example.cmina.mycastcast.activity.MainActivity.editor;
import static com.example.cmina.mycastcast.activity.MainActivity.settingShared;
import static com.example.cmina.mycastcast.activity.MainActivity.userSettingDbOpen;
import static com.example.cmina.mycastcast.util.PlayerConstants.cursor;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by cmina on 2017-02-20.
 */

public class SettingFragment extends Fragment implements View.OnClickListener {

    private ImageView profileImage;
    private TextView userName;
    private Switch pushSwitch, wifiSwitch;
    private Button logoutBtn;

    //userSetting 값 sqlite저장
    // public static UserSettingDbOpen userSettingDbOpenHelper;
    // private Cursor cursor;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    final static int REQ_CODE_SELECT_IMAGE = 3001;
    final static int REQUEST_IMAGE_CAPTURE = 4001;
    File file;
    String imagePath;

    private String mParam1;
    private String mParam2;

    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS = {"android.permission.CAMERA"};


    private boolean hasPermission(String[] permissions) {
        int res = 0;
        for (String perms : permissions) {
            res = getActivity().checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false; //퍼미션허가 안된 경우
            }
        }
        return true; //허가
    }

    //마시멜로이상 런타임퍼미션요청
    private void requestNecessaryPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean readAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    //   boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!readAccepted) {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다. ");
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg) {
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(getContext());
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }
            }
        });

        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        myDialog.show();
    }

    public SettingFragment() {

    }

    public static PlayListFragment newInstance(String mParam1, String mParam2) {
        PlayListFragment fragment = new PlayListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, mParam1);
        args.putString(ARG_PARAM2, mParam2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("설정");

        Intent intent = getActivity().getIntent();

        if (SaveSharedPreference.getUserUnique(getContext()).length() != 0) {

            View view = inflater.inflate(R.layout.fragment_setting, container, false);

            profileImage = (ImageView) view.findViewById(R.id.profileImage);
            userName = (TextView) view.findViewById(R.id.userName);
            pushSwitch = (Switch) view.findViewById(R.id.pushSwitch);
            wifiSwitch = (Switch) view.findViewById(R.id.wifiSwitch);
            logoutBtn = (Button) view.findViewById(R.id.logoutBtn);

            Log.e("setting frag", intent.toString());
            if (intent.toString().contains("extras")) {
                Glide.with(getContext()).load(SaveSharedPreference.getUserImage(getContext())).thumbnail(0.1f).bitmapTransform(new CropCircleTransformation(getContext())).into(profileImage);
            }

            logoutBtn.setOnClickListener(this);
            //자체로그인일떄만 프로필이미지 변경가능. ==>모든 로그인경우에 대해서.
            //   if (SaveSharedPreference.getLoginCase(getContext()).toString().equals("1")) {
            profileImage.setOnClickListener(this);
            //   }

            try {
                file = createFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            //쉐어드에 저장된 이미지가 있으면 그 이미지를
            userName.setText(SaveSharedPreference.getUserName(getContext()));
            if (SaveSharedPreference.getUserImage(getContext()).toString().length() > 0) {
                Glide.with(getContext()).load(SaveSharedPreference.getUserImage(getContext())).bitmapTransform(new CropCircleTransformation(getContext())).into(profileImage);

            }
            //로그인된 상태라면.
            if (settingShared.getString(SaveSharedPreference.getUserUnique(getContext()), "") != "") {
                Glide.with(getContext()).load(settingShared.getString(SaveSharedPreference.getUserUnique(getContext()), "")).bitmapTransform(new CropCircleTransformation(getContext())).into(profileImage);
            }
            Log.e("ddd", SaveSharedPreference.getUserImage(getContext()));
            Log.e("dddddd", SaveSharedPreference.getUserUnique(getContext()));

            cursor = null;
            cursor = userSettingDbOpen.getPush(SaveSharedPreference.getUserUnique(getContext()));
            cursor.moveToFirst();
            Log.e("push 확인", cursor.getString(2).toString());
            if (cursor.getString(2).toString().equals("true") || cursor.getString(2).toString().equals("1")) {
                pushSwitch.setChecked(true);
            } else {
                pushSwitch.setChecked(false);
            }

            //FCM서비스를 죽일지말지
            pushSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked == true) {
                        userSettingDbOpen.updatePush(SaveSharedPreference.getUserUnique(getContext()), true);
                        PackageManager pm = getApplicationContext().getPackageManager();
                        ComponentName componentName = new ComponentName(getApplicationContext(), MyFirebaseMessagingService.class);
                        pm.setComponentEnabledSetting(componentName,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);

                    } else {
                        userSettingDbOpen.updatePush(SaveSharedPreference.getUserUnique(getContext()), false);
                        PackageManager pm = getApplicationContext().getPackageManager();
                        ComponentName componentName = new ComponentName(getApplicationContext(), MyFirebaseMessagingService.class);
                        pm.setComponentEnabledSetting(componentName,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);
                    }
                }
            });

            cursor = userSettingDbOpen.getWIFI(SaveSharedPreference.getUserUnique(getContext()));
            cursor.moveToFirst();
            Log.e("wifi 확인", cursor.getString(1).toString());
            if (cursor.getString(1).toString().equals("true") || cursor.getString(1).toString().equals("1")) {
                wifiSwitch.setChecked(true);

            } else {
                wifiSwitch.setChecked(false);
            }


            wifiSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked == true) {
                        userSettingDbOpen.updateWifi(SaveSharedPreference.getUserUnique(getContext()), true);
                    } else {
                        userSettingDbOpen.updateWifi(SaveSharedPreference.getUserUnique(getContext()), false);

                    }
                }
            });

            return view;

        } else {
            Toast.makeText(getContext(), "로그인을 하세요", Toast.LENGTH_SHORT).show();
            return null;
        }


    }

    public static File createFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "IMG_" + timestamp + ".jpg";
        // String imageFileName = "test.jpg";

        File storageDir = Environment.getExternalStorageDirectory();
        File curFile = new File(storageDir, imageFileName);

        return curFile;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logoutBtn:

                //저장된 이름
                SaveSharedPreference.clearUserInfo(getContext());

                //카톡로그아웃
                UserManagement.requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {

                    }
                });

                //페북로그아웃
                LoginManager.getInstance().logOut();

                final Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                break;
            case R.id.profileImage:
                //  Toast.makeText(getContext(), "클릭클릭", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("프로필 사진 변경");
                builder.setMessage("프로필 사진을 바꾸겠습니까?")
                        .setCancelable(true)
                        .setPositiveButton("갤러리", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_PICK);

                                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
                            }
                        })
                        .setNeutralButton("카메라", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //필터카메라와 일반 카메라 선택 다이얼로그

                                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                                builder1.setMessage("사용할 카메라 종류를 선택하세요")
                                        .setCancelable(true)
                                        .setPositiveButton("필터카메라", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent1 = new Intent(getContext(), CameraFilterActivity.class);
                                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent1);
                                                return;
                                            }
                                        })
                                        .setNegativeButton("일반카메라", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (!hasPermission(PERMISSIONS)) {
                                                    requestNecessaryPermissions(PERMISSIONS);
                                                } else {
                                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

                                                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                                                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                                                    }
                                                }
                                            }
                                        });

                                AlertDialog dialog1 = builder1.create();
                                dialog1.show();

                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        //갤러리에서 사진 선택
        if (requestCode == REQ_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {

                //다이얼로그 띄우기, 필터를 거칠지 말지.
                AlertDialog.Builder myDialog = new AlertDialog.Builder(getContext());
              //  myDialog.setTitle("알림");
                myDialog.setMessage("이미지에 필터적용하겠습니까?");
                myDialog.setCancelable(false);
                myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //액티비티 이동
                        Intent intent = new Intent(getContext(), ImageFilterActivity.class);
                        intent.putExtra("imageUri", String.valueOf(data.getData()));
                        startActivity(intent);
                    }
                });

                myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = data.getData();
                        Log.e("image gal", uri + "");
                        Glide.with(getContext()).load(uri).bitmapTransform(new CropCircleTransformation(getContext())).into(profileImage);
                        //쉐어드에 저장해야하는뎅.
                        editor.putString(SaveSharedPreference.getUserUnique(getContext()), String.valueOf(uri));
                        editor.commit();
                    }
                });
                myDialog.show();


                /*Uri uri = data.getData();
                Log.e("image gal", uri + "");
                Glide.with(getContext()).load(uri).bitmapTransform(new CropCircleTransformation(getContext())).into(profileImage);
                //쉐어드에 저장해야하는뎅.
                editor.putString(SaveSharedPreference.getUserUnique(getContext()), String.valueOf(uri));
                editor.commit();*/
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) {
                if (file != null) {
                    String imageurl = file.getAbsolutePath();

                    Log.e("image", imageurl);

                    //런타임퍼미션...
                    Glide.with(getContext()).load(imageurl).bitmapTransform(new CropCircleTransformation(getContext())).into(profileImage);
                    editor.putString(SaveSharedPreference.getUserUnique(getContext()), imageurl);
                    editor.commit();

                } else {
                    Toast.makeText(getContext(), "파일이 없습니다", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

/*    private File savePictureFile() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String fileName = "IMG_" + timestamp;

        File pictureStorage = new File(Environment.getExternalStorageDirectory(), "MYAPP/");

        if (!pictureStorage.exists()) {
            pictureStorage.mkdirs();
        }

        try {
            File file = File.createTempFile(fileName, ".jpg", pictureStorage);

            imagePath = file.getAbsolutePath(); //이미지뷰에 보여주기 위해

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(imagePath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            getActivity().sendBroadcast(mediaScanIntent);

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

}
