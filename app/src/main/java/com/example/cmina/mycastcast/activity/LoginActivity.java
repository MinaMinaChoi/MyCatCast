package com.example.cmina.mycastcast.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.util.HttpUtil;
import com.example.cmina.mycastcast.util.NetworkUtil;
import com.example.cmina.mycastcast.util.SaveSharedPreference;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Created by cmina on 2017-02-09.
 */

public class LoginActivity extends AppCompatActivity {

    EditText editEmail, password;
    Button loginBtn, joinBtn;
    AlertDialog alertDialog;
    JSONArray array;

    //페이스북 로그인
    private Button faceLoginBtn;
    private CallbackManager callbackManager;

    //카톡 로그인
    SessionCallback callback;
    TextView kakaoText;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        NetworkUtil.setNetworkPolicy();

        //액션바 백버튼
      //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //카톡로그인
    //    callback = new SessionCallback();
      //  Session.getCurrentSession().addCallback(callback);


        kakaoText = (TextView) findViewById(R.id.kakaoText);

        kakaoText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                callback = new SessionCallback();
                Session.getCurrentSession().addCallback(callback);
                return false;
            }
        });

        //facebook login
        callbackManager = CallbackManager.Factory.create();
        faceLoginBtn = (Button) findViewById(R.id.faceLoginBtn);
        faceLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this,
                        Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {

                                Log.e("onSuccess", "onSuccess");
                                Log.e("토큰", loginResult.getAccessToken().getToken());
                                Log.e("유저아이디", loginResult.getAccessToken().getUserId());
                                Log.e("퍼미션 리스트", loginResult.getAccessToken().getPermissions() + "");

                                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                        new GraphRequest.GraphJSONObjectCallback() {
                                            @Override
                                            public void onCompleted(JSONObject object, GraphResponse response) {
                                                try {
                                                    Log.e("user profile", object.toString());

                                                    Profile profile = Profile.getCurrentProfile();
                                                    final String link = profile.getProfilePictureUri(200, 200).toString();

                                                    SaveSharedPreference.setUserName(LoginActivity.this, object.getString("name"));
                                                    SaveSharedPreference.setUserImage(LoginActivity.this, link);
                                                    SaveSharedPreference.setUserUnique(LoginActivity.this, object.getString("id"), "2");

                                                    //이미 디비에 등록된 경우 판별
                                                    try {
                                                        HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/apiLoginCheck.php");
                                                        JSONObject jsonObject = new JSONObject();
                                                        jsonObject.put("user_id", object.getString("id"));
                                                        String result = request.PhPtest(jsonObject.toString());

                                                        if (result.equals("1")) { //디비 등록해야함
                                                            Log.e("face", object.getString("id"));

                                                            //만약에 페이스북사용자의 정보중에 email이 없을 경우, 이메일 받는 액티비티로.
                                                            if (object.has("email")) { //이메일 제공여부를 판단하는 조건문...

                                                                try {
                                                                    HttpUtil request2 = new HttpUtil("http://222.122.202.126/signup_user_information.php");
                                                                    Log.d("dd", object.getString("email") + object.getString("name") + object.getString("id"));
                                                                    JSONObject object1 = new JSONObject();
                                                                    object1.put("user_email", object.getString("email"));
                                                                    object1.put("user_name", object.getString("name"));
                                                                    object1.put("user_id", object.getString("id"));
                                                                    object1.put("login_case", 2);
                                                                    String result2 = request2.PhPtest(object1.toString());

                                                                    if (result2.equals("1")) {

                                                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                        startActivity(intent);
                                                                        finish();

                                                                    } else { //디비 등록오류.
                                                                        SaveSharedPreference.clearUserInfo(LoginActivity.this);
                                                                        Toast.makeText(LoginActivity.this, "다시 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                } catch (MalformedURLException e) {
                                                                    e.printStackTrace();
                                                                }

                                                            } else { //이메일정보를 제공 안할 경우

                                                                Intent intent = new Intent(LoginActivity.this, ExtraInformActivity.class); //추가정보 받는 액티비티로.
                                                                startActivity(intent);
                                                                finish();
                                                            }

                                                        } else { //이미 존재

                                                            Log.e("face already, 페이스북 로그인 성공", object.getString("id"));

                                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    } catch (MalformedURLException e) {
                                                        e.printStackTrace();
                                                    }


                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "id,name,email");
                                request.setParameters(parameters);
                                request.executeAsync();

                            }

                            @Override
                            public void onCancel() {
                                Log.e("onCancel", "onCancel");
                            }

                            @Override
                            public void onError(FacebookException error) {
                                Log.e("onError", "onError" + error.getLocalizedMessage());
                            }
                        });
            }
        });

       //NetworkUtil.setNetworkPolicy();

        //일반로그인
        editEmail = (EditText) findViewById(R.id.edit_email);
        password = (EditText) findViewById(R.id.edit_pass);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        joinBtn = (Button) findViewById(R.id.joinBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editEmail.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    alertDialog = builder.setMessage("이메일을 입력하세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    return;
                }

                if (password.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    alertDialog = builder.setMessage("비밀번호를 입력하세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    return;
                }

                try {
                   // PHPRequest request = new PHPRequest("http://cmina21.cafe24.com/login.php");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user_email", editEmail.getText().toString());
                    jsonObject.put("user_password", password.getText().toString());

                    HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/login.php");

                    String result = request.execute(jsonObject.toString()).get();

                    Log.d("result", result + "");
                    if (result.equals("-1")) {
                        Toast.makeText(LoginActivity.this, "로그인 실패.", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                        JSONObject object = new JSONObject(result);
                        array = object.getJSONArray("result");
                        Log.e("loginresult", object.toString()+array.toString());

                        SaveSharedPreference.setUserUnique(LoginActivity.this, editEmail.getText().toString(), "1");
                        SaveSharedPreference.setUserName(LoginActivity.this, array.getJSONObject(0).getString("user_name"));
                        Log.e("일반 로그인 성공", editEmail.getText().toString() + array.getJSONObject(0).getString("user_name"));

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.e("interrupted", e+"");
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "인터넷을 연결하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //kakaologin
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);

        //facebooklogin
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }


    //카톡로그인부분
    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {

            UserManagement.requestMe(new MeResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;
                    Logger.d(message);

                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        finish();
                    } else {
                        redirectLoginActivity();//다시 로그인화면
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    redirectLoginActivity();
                }

                @Override
                public void onNotSignedUp() {
                }

                @Override
                public void onSuccess(UserProfile userProfile) {
                    //로그인에 성공하면 로그인한 사용자의 일련번호, 닉네임, 이미지url등을 리턴합니다.
                    //사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공합니다.
                    Log.e("UserProfile", userProfile.toString());
                    String kakaoID = String.valueOf(userProfile.getId());
                    Log.e("userID", kakaoID);


                    Log.e("액세스토큰", Session.getCurrentSession().getAccessToken());

                    SaveSharedPreference.setUserName(LoginActivity.this, userProfile.getNickname());
                    SaveSharedPreference.setUserUnique(LoginActivity.this, kakaoID, "2");
                    SaveSharedPreference.setUserImage(LoginActivity.this, userProfile.getThumbnailImagePath());

                    //db에 등록된 user_id 이면 메인액티비티로, 아니면 이메일추가정보를 받아서 인서트.
                    try {
                        HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/apiLoginCheck.php");
                        JSONObject object = new JSONObject();
                        object.put("user_id", kakaoID);
                        String result = request.PhPtest(object.toString());

                        if (result.equals("1")) { //디비 등록

                            Intent intent = new Intent(LoginActivity.this, ExtraInformActivity.class); //추가정보 받는 액티비티로.
                            startActivity(intent);
                            finish();
                        } else { //이미 존재

                            redirectMainActivity();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            // 세션 연결이 실패했을때
            if (exception != null) {
                Logger.e(exception);
            }

            setContentView(R.layout.activity_login); //세션 연결실패하면 다시 로그인화면으로.

        }

        private void redirectMainActivity() {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        protected void redirectLoginActivity() {
            final Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }

    }

}
