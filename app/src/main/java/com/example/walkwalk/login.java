package com.example.walkwalk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkwalk.mysqlDB.UserDB;
import com.example.walkwalk.mysqlDB.user;

import java.util.Timer;
import java.util.TimerTask;


public class login extends AppCompatActivity implements View.OnClickListener{
    private EditText et_username;
    private EditText et_pwd;
    private String user_name,user_password;
    private static final String TAG="login";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        et_username=(EditText)findViewById(R.id.et_username);
        et_pwd=(EditText)findViewById(R.id.et_pwd);
        Button loginButton=(Button)findViewById(R.id.button_login);
        Button registerButton=(Button)findViewById(R.id.button_register);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        getEditString();
        switch (v.getId()){
            case R.id.button_login:
                if(!user_password.isEmpty()&&!user_name.isEmpty()) {
                    CheckUser checkUser=new CheckUser(user_name,user_password);
                    checkUser.start();
                    try{
                        checkUser.join();
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    int ans=checkUser.getAns();
                    Log.d(TAG, "ans"+ans);
                    String now="";
                    switch (ans){
                        case 0:
                            now = "登录成功！";
                            String userName=user.myself.getName();
                            String userPwd=user.myself.getPassword();
                            String userSex=user.myself.getSex();
                            String userAge=user.myself.getAge();
                            int userId=user.myself.getId();
                            Intent intent1 = new Intent(login.this,MainActivity.class);
                            Log.d(TAG, "用户："+userAge+" "+userName+" ");
                            intent1.putExtra("UserId",userId);
                            intent1.putExtra("UserName",userName);
                            intent1.putExtra("UserPwd",userPwd);
                            intent1.putExtra("UserSex",userSex);
                            intent1.putExtra("UserAge",userAge);
                            startActivity(intent1);
                            break;
                        case 1:
                            now = "密码错误！";
                            break;
                        case 2:
                            now = "用户不存在！";
                            break;
                        default:
                            now ="网络异常...";
                    }
                    initEditString();
                    Toast.makeText(this,now,Toast.LENGTH_SHORT).show();
//                    TimerTask task = new TimerTask() {
//                        @Override
//                        public void run() {
//                            initEditString();
//                        }
//                    };
//                    Timer timer = new Timer();
//                    timer.schedule(task, 1000);//3秒后执行TimeTask的run方法


                }else{
                    Toast.makeText(this,"你没有输入用户名或密码",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_register:
                Intent intent2=new Intent(login.this,RegisterActivity.class);
                startActivity(intent2);
        }
    }

    //获取输入框控件内容
    public void getEditString(){
        user_name=et_username.getText().toString();
        user_password=et_pwd.getText().toString();
    }
    //清空输入框
    public void initEditString(){
        et_username.setText("");
        et_pwd.setText("");
    }

    //用户登录是否匹配子线程
    class CheckUser extends Thread{
        private int ans;
        private String user_name,user_pwd;
        public CheckUser()
        {
            user_name = user_password = "";
        }
        public CheckUser(String user_name, String user_pwd) {
            this.user_name = user_name;
            this.user_pwd = user_pwd;
        }
        @Override
        public void run() {
            ans= UserDB.userSignIn(user_name,user_pwd);
            if(ans==0){
                user.myself=UserDB.Getuser(user_name);
                Log.d(TAG, "当前=用户信息："+user.myself.getId()+" "+
                        user.myself.getName()+" "+user.myself.getSex()+" "+user.myself.getAge());
            }
        }
        public int getAns(){
            return ans;
        }
    }
}
