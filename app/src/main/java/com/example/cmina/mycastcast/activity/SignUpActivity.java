package com.example.cmina.mycastcast.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.util.HttpUtil;
import com.example.cmina.mycastcast.util.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.regex.Pattern;

/**
 * Created by cmina on 2017-02-09.
 */

public class SignUpActivity extends AppCompatActivity {

    private EditText user_email, user_name, user_pass, user_passCh;
    private Button signupBtn, valChBtn;
    private boolean validate = false;

    AlertDialog alertDialog;

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
        setContentView(R.layout.activity_signup);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NetworkUtil.setNetworkPolicy();

        user_email = (EditText) findViewById(R.id.edit_email);
        user_name = (EditText) findViewById(R.id.edit_name);
        user_pass = (EditText) findViewById(R.id.edit_pass);
        user_passCh = (EditText) findViewById(R.id.edit_passch);

        signupBtn = (Button) findViewById(R.id.signupBtn);
        valChBtn = (Button) findViewById(R.id.emailChk);


        //비밀번호가 동일하면 배경색 변경
        user_passCh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = user_pass.getText().toString();
                String passwordCh = user_passCh.getText().toString();

                if (password.equals(passwordCh)) {
                    user_pass.setBackgroundColor(Color.BLUE);
                    user_passCh.setBackgroundColor(Color.BLUE);
                }
                else {
                    user_pass.setBackgroundColor(Color.RED);
                    user_passCh.setBackgroundColor(Color.RED);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                if (spaceCheck(user_name.getText().toString())) {
                    Toast.makeText(SignUpActivity.this, "이름에 공백을 없애주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (spaceCheck(user_pass.getText().toString())) {
                    Toast.makeText(SignUpActivity.this, "비밀번호에 공백을 없애주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (user_email.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    alertDialog = builder.setMessage("이메일을 입력하세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    user_email.requestFocus();
                    return;
                }

                if (user_name.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    alertDialog = builder.setMessage("이름을 입력하세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    user_name.requestFocus();
                    return;
                }

                if (user_pass.getText().toString().equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    alertDialog = builder.setMessage("비밀번호를 입력하세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    user_pass.requestFocus();
                    return;
                }

                if (!(user_passCh.getText().toString().equals(user_pass.getText().toString()))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    alertDialog = builder.setMessage("비밀번호가 일치하지 않습니다")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    user_passCh.requestFocus();
                    return;
                }

                if (!validate) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    alertDialog = builder.setMessage("이메일 중복체크를 하세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    return;
                }

                if (!Pattern.matches("^[a-z0-9_+.-]+@([a-z0-9-]+\\.)+[a-z0-9]{2,4}$", user_email.getText().toString())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    alertDialog = builder.setMessage("이메일 형식을 지키세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    return;
                }

//^.*(?=.{6,12})(?=.*[0-9])(?=.*[a-z]).*$
                    //비밀번호 형식체크
                if (!Pattern.matches("^.*(?=.{6,12})(?=.*[0-9])(?=.*[a-z]).*$", user_pass.getText().toString())  ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    alertDialog = builder.setMessage("비밀번호 형식을 지키세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    return;
                }


                try {
                    HttpUtil request = new HttpUtil("http://222.122.202.126/signup_user_information.php");
                    JSONObject object = new JSONObject();
                    object.put("user_email", user_email.getText().toString());
                    object.put("user_name", user_name.getText().toString());
                    object.put("user_password", user_pass.getText().toString());
                    object.put("login_case", 1);

                    String result = request.PhPtest(object.toString());
                    if (result.equals("1")) {
                        Toast.makeText(SignUpActivity.this, "회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignUpActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(SignUpActivity.this, "인터넷을 연결하세요", Toast.LENGTH_SHORT).show();
                }

            }
        });

        valChBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = user_email.getText().toString();

                if (useremail.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    alertDialog = builder.setMessage("이메일을 입력하세요")
                            .setPositiveButton("확인", null)
                            .create();
                    alertDialog.show();
                    return;
                }

/*

                if (validate) { //이미 중복체크를 했으면 토스트메시지 안 뜨게. 확인작업 안하게.
                    return;
                }
*/



               // if (Patterns.EMAIL_ADDRESS.matcher(useremail).matches()) { //이메일형식이 맞으면 확인작업
                if (Pattern.matches("^[a-z0-9_+.-]+@([a-z0-9-]+\\.)+[a-z0-9]{2,4}$", user_email.getText().toString())) {
                    try {
                        HttpUtil validateRequest = new HttpUtil("http://cmina21.cafe24.com/validateEmail.php");
                        JSONObject object = new JSONObject();
                        object.put("user_email", user_email.getText().toString());
                        String result = validateRequest.PhPtest(object.toString());
                        if (result.equals("1")) {
                            Toast.makeText(SignUpActivity.this, "사용가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                            validate = true;
                        } else {
                            Toast.makeText(SignUpActivity.this, "중복된 이메일입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(SignUpActivity.this, "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

    }

    public boolean spaceCheck(String spaceCheck)
    {
        for(int i = 0 ; i < spaceCheck.length() ; i++)
        {
            if(spaceCheck.charAt(i) == ' ')
                return true;
        }
        return false;
    }

}
