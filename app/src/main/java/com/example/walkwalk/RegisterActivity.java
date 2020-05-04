package com.example.walkwalk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.walkwalk.mysqlDB.UserDB;
import com.example.walkwalk.mysqlDB.user;

import java.util.Timer;
import java.util.TimerTask;

public class RegisterActivity extends AppCompatActivity
        implements View.OnClickListener,CompoundButton.OnCheckedChangeListener {
    private EditText  edit_username;
    private EditText  edit_pwd;
    private EditText  edit_checkpwd;
    private EditText  edit_age;
    private RadioButton radio_man;
    private RadioButton radio_woman;
    private String username,pwd,checkpwd,age,sex;
    private static final String TAG="Register";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edit_username=(EditText)findViewById(R.id.edit_username);
        edit_pwd=(EditText)findViewById(R.id.edit_pwd);
        edit_checkpwd=(EditText)findViewById(R.id.edit_checkpwd);
        edit_age=(EditText)findViewById(R.id.edit_age);
        radio_man=(RadioButton)findViewById(R.id.radio_man);
        radio_woman=(RadioButton)findViewById(R.id.radio_women);
        Button confirmButton=(Button)findViewById(R.id.button_confirm);
        Button cancelButton=(Button)findViewById(R.id.button_cancel);

        radio_man.setOnCheckedChangeListener(this);
        radio_woman.setOnCheckedChangeListener(this);
        confirmButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }
    //性别选择
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            disableOthers(buttonView.getId());
            sex=buttonView.getText().toString();
            buttonView.setTextColor(getResources().getColor(R.color.colorPink));
        }else{
            buttonView.setTextColor(getResources().getColor(R.color.colorLightPink));
        }
    }
    //取消其他选择
    private void disableOthers(int viewId) {
        if(R.id.radio_man!=viewId&&radio_man.isChecked()){
            radio_man.setChecked(false);
        }
        if(R.id.radio_women!=viewId&&radio_woman.isChecked()){
            radio_woman.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        getEditString();
        switch (v.getId()){
            case R.id.button_confirm:
                if(!username.isEmpty()&&!pwd.isEmpty()&&!checkpwd.isEmpty()&&
                     !age.isEmpty()&&!sex.isEmpty()){
                    if(!pwd.equals(checkpwd)){
                        initEditString();
                        Toast.makeText(this, "两次密码不一致喔", Toast.LENGTH_SHORT).show();
                    }else{
                        Register_User thread=new Register_User(username,pwd);
                        thread.start();
                        try {
                            thread.join();
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        int ans=thread.GetAns();
                        String now;
                        switch (ans){
                            case 0:
                                now="注册成功！";
                                Toast.makeText(this, now+"即将返回登录界面", Toast.LENGTH_SHORT).show();
                                TimerTask task=new TimerTask() {
                                    @Override
                                    public void run() {
                                        Intent intent=new Intent(RegisterActivity.this,login.class);
                                        startActivity(intent);
                                    }
                                };
                                Timer timer=new Timer();
                                timer.schedule(task,1000);
                                return ;
                            case 1:
                                now="用户已存在";
                                initEditString();
                                Toast.makeText(this, now, Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                now="网络异常";
                                initEditString();
                                Toast.makeText(this, now, Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    Toast.makeText(this, "信息还没有完善噢", Toast.LENGTH_SHORT).show();
                }
            case R.id.button_cancel:
                initEditString();
            case R.id.return_login:
                Intent intent=new Intent(RegisterActivity.this,login.class);
                startActivity(intent);
        }

    }

    //创建用户子线程
    class Register_User extends Thread{
        private int ans;
        private String user_name,user_pwd;
        public Register_User(){user_name=user_pwd="";}

        public Register_User(String user_name, String user_pwd) {
            this.user_name = user_name;
            this.user_pwd = user_pwd;
        }

        @Override
        public void run() {
            user.User now=new user.User();
            now.setName(user_name);
            now.setPassword(user_pwd);
            now.setAge(age);
            now.setSex(sex);
            ans= UserDB.userSignUp(now);
        }
        public int GetAns(){return ans;}
    }

    //获取输入框控件内容
    public void getEditString(){
        username=edit_username.getText().toString();
        pwd=edit_pwd.getText().toString();
        checkpwd=edit_checkpwd.getText().toString();
        age=edit_age.getText().toString();
        Log.d(TAG, "输入"+username+pwd+checkpwd+age+sex
        );

    }
    //清空输入框
    public void initEditString(){
        edit_username.setText("");
        edit_pwd.setText("");
        edit_checkpwd.setText("");
        edit_age.setText("");
    }

}
