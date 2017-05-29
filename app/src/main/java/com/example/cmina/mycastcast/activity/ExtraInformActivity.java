package com.example.cmina.mycastcast.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.util.HttpUtil;
import com.example.cmina.mycastcast.util.NetworkUtil;
import com.example.cmina.mycastcast.util.SaveSharedPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.regex.Pattern;


/**
 * Created by cmina on 2017-02-16.
 */

public class ExtraInformActivity extends AppCompatActivity {

    private EditText extraEmail;
    private Button kakaoJoinBtn;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);

        NetworkUtil.setNetworkPolicy();

        extraEmail = (EditText) findViewById(R.id.kakadoJoinEmail);
        kakaoJoinBtn = (Button) findViewById(R.id.kakaoJoinBtn);

        //이메일입력후 가입
        kakaoJoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (extraEmail.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExtraInformActivity.this);
                    alertDialog = builder.setMessage("이메일을 입력하세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    extraEmail.requestFocus();
                    return;
                }

                if (!Pattern.matches("^[a-z0-9_+.-]+@([a-z0-9-]+\\.)+[a-z0-9]{2,4}$", extraEmail.getText().toString())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExtraInformActivity.this);
                    alertDialog = builder.setMessage("이메일 형식을 지키세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    return;
                }


                try {
                    String email = extraEmail.getText().toString();
                    String name = SaveSharedPreference.getUserName(ExtraInformActivity.this);
                    String user_id = SaveSharedPreference.getUserUnique(ExtraInformActivity.this);
                    String result = null;

                    JSONObject object = new JSONObject();
                    object.put("user_email", email);
                    object.put("user_name", name);
                    object.put("user_id", user_id);
                    object.put("login_case", 2);

                    HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/signup_user_information.php");
                    result = request.PhPtest(object.toString());

                    if (result.equals("1")) {
                        //db등록완료.

                        SaveSharedPreference.setUserUnique(ExtraInformActivity.this, user_id, "2");
                        Intent intent = new Intent(ExtraInformActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    } else {
                        SaveSharedPreference.clearUserInfo(ExtraInformActivity.this);
                        Toast.makeText(ExtraInformActivity.this, "다시 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ExtraInformActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(ExtraInformActivity.this, "인터넷을 연결하세요", Toast.LENGTH_SHORT).show();
                }
            }


        });


    }
}
