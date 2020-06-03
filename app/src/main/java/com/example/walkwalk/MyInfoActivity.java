package com.example.walkwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkwalk.mysqlDB.UserDB;
import com.example.walkwalk.mysqlDB.user;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyInfoActivity extends AppCompatActivity {
    private static String TAG = "MyInfo";
    String imagePath = null;
    private EditText et_userName,et_userPwd,et_userPwdNew;
    private String userName,userPwd,userPwdNew,password,name;
    private int userId;
    private CircleImageView img;
    private TextView changeImg;
    public static final int CHOOSE_PHOTO=2;
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
        img=(CircleImageView)findViewById(R.id.icon_image);
        changeImg=(TextView)findViewById(R.id.text_changeImage);
        changeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MyInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MyInfoActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
                } else {
                    openAlbum();
                }
            }
        });
    }
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath); // 根据图片路径显示图片
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            img.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
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
                                Intent intent = new Intent(MyInfoActivity.this,MainActivity.class);
                                intent.putExtra("ImgPath",imagePath);
                                intent.putExtra("newName",name);
                                Log.d(TAG, "onOptionsItemSelected: "+imagePath+"  "+name);
                                setResult(RESULT_OK,intent);
                                finish();
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
