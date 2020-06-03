package com.example.walkwalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.walkwalk.mysqlDB.UserDB;
import com.example.walkwalk.mysqlDB.target;
import com.example.walkwalk.step.UpdateUiCallBack;
import com.example.walkwalk.step.service.StepService;
import com.example.walkwalk.view.StepArcView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , SensorEventListener {
    private DrawerLayout mDrawerLayout;
    private static String TAG="主页面";
    private String imgPath="",newName="";
    private String user_name,user_pwd,user_sex,user_age;
    private int user_id,target_count=3000,target_v,speed,step_count=0;
    private StepArcView cc;
    private Button btn_start,btn_stop;
    private TextView tv_v,tv_target_count,tv_target_v;
    private boolean isBind = false;
    private Chronometer chronometer;
    private NavigationView navigationView;
    private ImageView head_iv;
    private TextView headName;
    /**
     * 通知构建者
     */
    private NotificationCompat.Builder mBuilder;

    // 定位相关
    LocationClient mLocClient;
    private LocationClientOption locationOption;
    public MyLocationListenner myListener = new MyLocationListenner();
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    MapView mMapView;
    BaiduMap mBaiduMap;

    private TextView info;
    private RelativeLayout progressBarRl;

    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    float mCurrentZoom = 19f;//默认地图缩放比例值

    private SensorManager mSensorManager;

    //起点图标
    BitmapDescriptor startBD ;
    //终点图标
    BitmapDescriptor finishBD ;

    List<LatLng> points = new ArrayList<LatLng>();//位置点集合
    Polyline mPolyline;//运动轨迹图层
    LatLng last = new LatLng(0, 0);//上一个定位点
    MapStatus.Builder builder;


    /**
     * 注册组件
     */
    private void signView(){
        cc = (StepArcView)findViewById(R.id.cc);
        btn_start = (Button)findViewById(R.id.bt_start_sport);
        btn_stop = (Button)findViewById(R.id.bt_stop_sport);
        tv_v = (TextView)findViewById(R.id.tv_data);

//        mapImg = (ImageView)findViewById(R.id.img_map);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
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
//        mapImg.setOnClickListener(this);
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
        headName=(TextView)headView.findViewById(R.id.nac_username);
        head_iv=(ImageView) headView.findViewById(R.id.main_icon_image);
        if(imgPath.isEmpty()){
            head_iv.setImageResource(R.mipmap.head);
        }else{
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
            head_iv.setImageBitmap(bitmap);
        }
        if(newName.isEmpty()) {
            headName.setText(user_name);
        }else{
            headName.setText(newName);
        }
        head_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(MainActivity.this,MyInfoActivity.class);
                intent1.putExtra("userId",user_id);
                intent1.putExtra("password",user_pwd);
                intent1.putExtra("name",user_name);
                startActivityForResult(intent1,1);
//                startActivity(intent1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: 执行");
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    newName=data.getStringExtra("newName");
                    imgPath=data.getStringExtra("ImgPath");
                    Log.d(TAG, "getNewData: "+imgPath+" "+newName);
                    Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                    head_iv.setImageBitmap(bitmap);
                    headName.setText(newName);

                }
                break;
            default:
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        String t_count = (int)(ct.getT_stepCount_min())+"—"+(int)(ct.getT_stepCount_max());
        String t_speed = ct.getT_walkV_min()+"—"+ct.getT_walkV_max();
        tv_target_count.setText(t_count);
        tv_target_v.setText(t_speed);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(getApplicationContext());
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        super.onCreate(savedInstanceState);
        getData();
        setContentView(R.layout.activity_main);
        signView();
        loadToolbar();
        initNavigation();
        initData();
        initTarget();
        initMap();
        navigationView =(NavigationView)findViewById(R.id.nav_ceview);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.nav_myinfo:
                                break;
                            case R.id.nav_analyze:
//                                Intent intent = new Intent(MainActivity.this, AnalyseActivity.class);
//                                startActivity(intent);
                                startActivity(new Intent(MainActivity.this,AnalyseActivity.class));
                                break;
                        }
                        return true;
                    }
                });

    }

    private void initMap(){
        startBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_startpoint);
        finishBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_finishpoint);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 获取传感器管理服务
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.baidumapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
        /**
         * 添加地图缩放状态变化监听，当手动放大或缩小地图时，拿到缩放后的比例，然后获取到下次定位，
         *  给地图重新设置缩放比例，否则地图会重新回到默认的mCurrentZoom缩放比例
         */
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                mCurrentZoom = arg0.zoom;
            }
            @Override
            public void onMapStatusChange(MapStatus arg0) {
                // TODO Auto-generated method stub
            }
        });

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        locationOption = new LocationClientOption();
        initLocationOption();
    }

    double lastX;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];

        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;

            if (isFirstLoc) {
                lastX = x;
                return;
            }

            locData = new MyLocationData.Builder().accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat).longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
            Log.d("TAG", "onSensorChanged: "+locData);
        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            if (location == null || mMapView == null) {
                return;
            }
            if (isFirstLoc) {//首次定位
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                if(ll == null){
                    return;
                }
                isFirstLoc = false;
                points.add(ll);//加入集合
                last = ll;
                Log.d(TAG, "onReceiveLocation: ");
                //显示当前定位点，缩放地图
                locateAndZoom(location, ll);
                //标记起点图层位置
                MarkerOptions oStart = new MarkerOptions();// 地图标记覆盖物参数配置类
                oStart.position(points.get(0));// 覆盖物位置点，第一个点为起点
                oStart.icon(startBD);// 设置覆盖物图片
                mBaiduMap.addOverlay(oStart); // 在地图上添加此图层
//                    progressBarRl.setVisibility(View.GONE);
                return;//画轨迹最少得2个点，首地定位到这里就可以返回了
            }
            //从第二个点开始
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            //sdk回调gps位置的频率是1秒1个，位置点太近动态画在图上不是很明显，可以设置点之间距离大于为5米才添加到集合中
            if (DistanceUtil.getDistance(last, ll) < 10) {
                return;
            }
            points.add(ll);//如果要运动完成后画整个轨迹，位置点都在这个集合中
            last = ll;
            //显示当前定位点，缩放地图
            locateAndZoom(location, ll);
            //清除上一次轨迹，避免重叠绘画
            mMapView.getMap().clear();
            //起始点图层也会被清除，重新绘画
            MarkerOptions oStart = new MarkerOptions();
            oStart.position(points.get(0));
            oStart.icon(startBD);
            mBaiduMap.addOverlay(oStart);
            //将points集合中的点绘制轨迹线条图层，显示在地图上
            OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(points);
            mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
        }
//        }
    }

    private void locateAndZoom(final BDLocation location, LatLng ll) {
        mCurrentLat = location.getLatitude();
        mCurrentLon = location.getLongitude();
        locData = new MyLocationData.Builder().accuracy(0)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(mCurrentDirection).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);
        MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.animateMapStatus(update);
        update=MapStatusUpdateFactory.zoomTo(19f);
        mBaiduMap.animateMapStatus(update);
    }

    public void startMap(){
        mLocClient.setLocOption(locationOption);
        mLocClient.start();
        Log.d(TAG, "onClick:  mLocClient.start()");
    }

    public void stopMap(){
        if (mLocClient != null && mLocClient.isStarted()) {
            mLocClient.stop();
            if (isFirstLoc) {
                points.clear();
                last = new LatLng(0, 0);
                return;
            }
            MarkerOptions oFinish = new MarkerOptions();// 地图标记覆盖物参数配置类
            oFinish.position(points.get(points.size() - 1));
            oFinish.icon(finishBD);// 设置覆盖物图片
            mBaiduMap.addOverlay(oFinish); // 在地图上添加此图层
            //复位
            points.clear();
            last = new LatLng(0, 0);
            isFirstLoc = true;
        }
    }
    /**
     * 初始化定位参数配置
     */
    private void initLocationOption() {
//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationOption.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("bd09ll");
//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(5000);
//可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
//可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(true);
//可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
//可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
//可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
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

    //toolbar点击事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //HomeAsUp按钮默认值都是android.R.id.home
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);

                break;

        }
        return true;
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (isBind) {
            this.unbindService(conn);
        }
        // 退出时销毁定位
        mLocClient.unRegisterLocationListener(myListener);
        if (mLocClient != null && mLocClient.isStarted()) {
            mLocClient.stop();
        }
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.getMap().clear();
        mMapView.onDestroy();
        mMapView = null;
        startBD.recycle();
        finishBD.recycle();
        super.onDestroy();
    }
    //开始和停止运动
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_start_sport:
                setupService();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                startMap();
                Log.d(TAG, "onClick: 开始计步");
                break;
            case R.id.bt_stop_sport:
                stopService();
                chronometer.stop();
                stopMap();
                Log.d(TAG, "onClick: 停止计步");
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
