package com.example.walkwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.walkwalk.view.StepArcView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DrawerLayout mDrawerLayout;
    private static String TAG="主页面";
    private String user_name,user_pwd,user_sex,user_age;
    private int user_id;
    private StepArcView cc;
    private Button btn_start,btn_stop;
    private TextView tv_v,tv_time,tv_target_count,tv_target_v;
    /**
     * 注册组件
     */
    private void signView(){
        cc = (StepArcView)findViewById(R.id.cc);
        btn_start = (Button)findViewById(R.id.bt_start_sport);
        btn_stop = (Button)findViewById(R.id.bt_stop_sport);
        tv_v = (TextView)findViewById(R.id.tv_data);
        tv_time = (TextView)findViewById(R.id.tv_time);
        tv_target_count = (TextView)findViewById(R.id.tv_target_step);
        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
    }
    /**
     * 获取用户数据
     */
    private void getData(){
        Intent intent=getIntent();
        user_name=intent.getStringExtra("UserName");
        user_age=intent.getStringExtra("UserAge");
        user_pwd=intent.getStringExtra("UserPwd");
        user_sex=intent.getStringExtra("UserSex");
    }
    /**
     *加载Toolbar
     */
    private void loadToolbar(){
        //加载toolbar
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBar actionBar=getSupportActionBar();
        //设置标题栏的按钮效果
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.menu);
        }
    }

    /**
     * 初始化Navigation
     */
    private void initNavigation(){
        //获取侧边栏实体
        NavigationView cenavView=(NavigationView)findViewById(R.id.nav_ceview);
        cenavView.setCheckedItem(R.id.nav_myinfo);
        cenavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        View headView=cenavView.inflateHeaderView(R.layout.nav_header);
        TextView headName=(TextView)headView.findViewById(R.id.nac_username);
        ImageView head_iv=(ImageView) headView.findViewById(R.id.icon_image);
        headName.setText(user_name);
        head_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(MainActivity.this,MyInfoActivity.class);
                intent1.putExtra("userId",user_id);
                intent1.putExtra("password",user_pwd);
                intent1.putExtra("name",user_name);
                startActivity(intent1);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
        setContentView(R.layout.activity_main);
        signView();
        loadToolbar();
        initNavigation();

    }
    //侧边栏菜单的点击事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //HomeAsUp按钮默认值都是android.R.id.home
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.nav_myinfo:
                break;
            case R.id.nav_analyze:
                break;
            default:
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_start_sport:
                break;
            case R.id.bt_stop_sport:
                break;
            default:
        }
    }
}
