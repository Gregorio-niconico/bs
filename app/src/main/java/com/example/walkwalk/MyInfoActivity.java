package com.example.walkwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.walkwalk.mysqlDB.UserDB;
import com.example.walkwalk.mysqlDB.user;

public class MyInfoActivity extends AppCompatActivity {
    private static String TAG = "MayInfo";
    private EditText et_userName,et_userPwd,et_userPwdNew;
    private String userName,userPwd,userPwdNew,password,name;
    private int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        //加载toolbar
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        //设置标题栏的按钮效果
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.cancel);
        }
        et_userName=(EditText)findViewById(R.id.info_et_username);
        et_userPwd=(EditText)findViewById(R.id.info_et_userpwd);
        et_userPwdNew=(EditText)findViewById(R.id.info_et_userpwdnew);
        Intent intent=getIntent();
        password=intent.getStringExtra("password");
        name=intent.getStringExtra("name");
        et_userName.setText(name);

    }

    //加载toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    //toolbar的点击事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //HomeAsUp按钮默认值都是android.R.id.home
            case android.R.id.home:
                this.finish();
                break;
            case R.id.finish:
                getEditString();
                Log.d(TAG, "userPwd:"+userPwd+" "+"password: "+password);
                if(userPwd.equals(password)) {
                    if (!userPwd.equals(userPwdNew)) {
                        UserUpdate userUpdate = new UserUpdate(name,userName, userPwdNew);
                        userUpdate.start();
                        try {
                            userUpdate.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int ans = userUpdate.getAns();
                        Log.d(TAG, "修改ans:" + ans);
                        switch (ans) {
                            case 0:
                                Toast.makeText(this, "修改成功！", Toast.LENGTH_SHORT).show();
                                break;
                            case -1:
                                Toast.makeText(this, "修改失败！", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    } else {
                        Toast.makeText(this, "新密码与旧密码一致！", Toast.LENGTH_SHORT).show(); }
                }else{
                       Toast.makeText(this,"原密码错误，请重新输入",Toast.LENGTH_SHORT).show();
                    }
                    break;
            default:
        }
        return true;
    }

    class UserUpdate extends Thread{
        private int ans;
        private String user_name,user_pwd,user_oldname;
        public UserUpdate(){user_name=user_pwd=user_oldname="";}

        public UserUpdate(String user_oldname,String user_name, String user_pwd) {
            this.user_oldname = user_oldname;
            this.user_name = user_name;
            this.user_pwd = user_pwd;
        }

        @Override
        public void run() {
//            user.User now=new user.User();
//            now.setName(user_name);
//            now.setPassword(user_pwd);
//            now.setAge(age);
            ans= UserDB.userUpdate(user_oldname,user_name,user_pwd);
        }
        public int getAns(){return ans;}
    }

    //获取输入框控件内容
    public void getEditString(){
        userName=et_userName.getText().toString();
        userPwd=et_userPwd.getText().toString();
        userPwdNew=et_userPwdNew.getText().toString();
        Log.d(TAG, "输入"+userName+" "+userPwd+" "+userPwdNew);

    }
}
