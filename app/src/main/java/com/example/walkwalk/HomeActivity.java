package com.example.walkwalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.walkwalk.ViewPaper.AnalyzeFragment;
import com.example.walkwalk.ViewPaper.BottomAdapter;
import com.example.walkwalk.ViewPaper.MyInfoFragment;
import com.example.walkwalk.ViewPaper.SportsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private BottomNavigationView mBv;
    private ViewPager mVp;
    private static String TAG="主页面";
    private String user_name,user_pwd,user_sex,user_age;
    private int user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        Intent intent=getIntent();
        user_id=intent.getIntExtra("UserId",0);
        user_name=intent.getStringExtra("UserName");
        user_pwd=intent.getStringExtra("UserPwd");
        user_sex=intent.getStringExtra("UserSex");
        user_age=intent.getStringExtra("UserAge");
        Log.d(TAG, "用户："+user_name+" "+user_sex+" ");
        //不同性别显示不同的图标
        if(user_sex.equals("男")){
            setContentView(R.layout.activity_home_m);
        }else{
            setContentView(R.layout.activity_home_w);
        }

        //加载toolbar
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBar actionBar=getSupportActionBar();
        //设置标题栏的按钮效果
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.center);
        }
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
                Intent intent=new Intent(HomeActivity.this,MyInfoActivity.class);
                intent.putExtra("userId",user_id);
                intent.putExtra("password",user_pwd);
                intent.putExtra("name",user_name);
                startActivity(intent);
            }
        });
        initView();


    }

    //初始化导航栏
    private void initView() {
        mBv = (BottomNavigationView) findViewById(R.id.nav_view);
        mVp = (ViewPager) findViewById(R.id.viewpaper);
//        BottomAdapterNavigationViewHelper.disableShiftMode(mBv);

        //这里可true是一个消费过程，同样可以使用break，外部返回true也可以
        mBv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tab_sport:
                        mVp.setCurrentItem(0);
                        return true;

                    case R.id.tab_analyze:
                        mVp.setCurrentItem(1);
                        return true;

                    case R.id.tab_my:
                        mVp.setCurrentItem(2);
                        return true;

                }
                return false;
            }
        });

        //数据填充
        setupViewPager(mVp);
        //ViewPager监听
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mBv.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //禁止ViewPager滑动
        //        mVp.setOnTouchListener(new View.OnTouchListener() {
        //                    @Override
        //                    public boolean onTouch(View v, MotionEvent event) {
        //                        return true;
        //                    }
        //                });
    }

    //初始化ViewPaper
    private void setupViewPager(ViewPager viewPager) {
        BottomAdapter adapter = new BottomAdapter(getSupportFragmentManager());
        adapter.addFragment(new SportsFragment());
        adapter.addFragment(new AnalyzeFragment());
        adapter.addFragment(new MyInfoFragment());
        viewPager.setAdapter(adapter);
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
            case R.id.nav_collection:
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
}
