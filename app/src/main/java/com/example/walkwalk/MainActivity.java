package com.example.walkwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.walkwalk.mysqlDB.UserDB;
import com.example.walkwalk.mysqlDB.target;
import com.example.walkwalk.step.UpdateUiCallBack;
import com.example.walkwalk.step.service.StepService;
import com.example.walkwalk.view.StepArcView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DrawerLayout mDrawerLayout;
    private static String TAG="主页面";

    private String user_name,user_pwd,user_sex,user_age;
    private int user_id,target_count=3000,target_v,speed,step_count=0;
    private StepArcView cc;
    private Button btn_start,btn_stop;
    private TextView tv_v,tv_target_count,tv_target_v;
    private ImageView mapImg;
    private boolean isBind = false;
    private Chronometer chronometer;
    /**
     * 注册组件
     */
    private void signView(){
        cc = (StepArcView)findViewById(R.id.cc);
        btn_start = (Button)findViewById(R.id.bt_start_sport);
        btn_stop = (Button)findViewById(R.id.bt_stop_sport);
        tv_v = (TextView)findViewById(R.id.tv_data);
        mapImg = (ImageView)findViewById(R.id.img_map);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        //计时器的回调监听函数，实时更新步频
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                speed = getSpeed(step_count);
                Log.d(TAG, "updateUi: "+step_count+" "+"speed:"+speed);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_v.setText(speed+"");
                    }
                });
            }
        });
        tv_target_count = (TextView)findViewById(R.id.tv_target_step);
        tv_target_v = (TextView)findViewById(R.id.tv_target_v);
        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        mapImg.setOnClickListener(this);
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
    /**
     * 初始化用户目标，显示
     */
    private void initTarget(){
        GetTarget getTarget = new GetTarget(user_age,user_sex);
        getTarget.start();
        try{
            getTarget.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        target.Target ct = getTarget.target();
        Log.d(TAG, "initTarget: "+ct.getT_walkV_min()+" "+ct.getT_stepCount_min());
        String t_count = ""+ct.getT_stepCount_min();
        String t_speed = ""+ct.getT_walkV_min();
        tv_target_count.setText(t_count);
        tv_target_v.setText(t_speed);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
        setContentView(R.layout.activity_main);
        signView();
        loadToolbar();
        initNavigation();
        initData();
        initTarget();
    }

    private void initData(){
        cc.setCurrentCount(target_count,0);
    }

    /**
     * 开启计步服务
     */
    private void setupService(){
        Intent startIntent = new Intent(this, StepService.class);
        isBind = bindService(startIntent,conn, Context.BIND_AUTO_CREATE);
        startService(startIntent);
    }
    /**
     * 停止计步服务
     */
    private void stopService(){
        Intent stopIntent = new Intent(this,StepService.class);
        //解绑服务
        unbindService(conn);
        //停止服务
        stopService(stopIntent);

    }
    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    ServiceConnection conn = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService stepService = ((StepService.StepBinder) service).getService();
            //设置初始化数据
            cc.setCurrentCount(target_count, stepService.getStepCount());

            //设置步数监听回调
            stepService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int stepCount) {
                    cc.setCurrentCount(target_count, stepCount);
                    step_count = stepCount;



                }
            });
        }
        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };

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
        if (isBind) {
            this.unbindService(conn);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_start_sport:
                setupService();
                chronometer.start();
                Log.d(TAG, "onClick: 开始计步");
                break;
            case R.id.bt_stop_sport:
                stopService();
                chronometer.stop();
                Log.d(TAG, "onClick: 停止计步");
                break;
            case R.id.img_map:
                startActivity(new Intent(MainActivity.this,MapTrackActivity.class));
                break;
            default:
        }
    }

    /**
     * 获取计时时间
     * @return
     */
    private float getTime(){
        float minute = Integer.parseInt(chronometer.getText().toString().split(":")[0]);
        float second = Integer.parseInt(chronometer.getText().toString().split(":")[1]);
        float time = minute + second/60;
        return time;
    }

    /**
     * 传入步数，获取步频
     * @param count
     * @return
     */
    private int getSpeed(int count){
        int speed = 0;
        float time = getTime();
        Log.d(TAG, "getSpeed:time "+time);
        speed = (int) (count/time);
        Log.d(TAG, "getSpeed: speed"+speed);
        return speed;
    }



    /**
     * 获取用户目标子线程
     */
    class GetTarget extends Thread{

        private String age,sex;
        public GetTarget()
        {
            age = sex = "";
        }
        public GetTarget(String age, String sex) {
            this.age = age;
            this.sex = sex;
        }
        @Override
        public void run() {
            target.myTarget= UserDB.GetTarget(age,sex);
                Log.d(TAG, "当前=用户信息："+target.myTarget.getTargetID()+" "+target.myTarget.getAgeID()+
                        " "+target.myTarget.getSex()+" "+target.myTarget.getT_stepCount_min()+" "+target.myTarget.getT_walkV_min());
        }
        public target.Target target(){
            return target.myTarget;
        }
    }
}
