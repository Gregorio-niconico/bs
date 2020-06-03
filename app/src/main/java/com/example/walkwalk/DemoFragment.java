package com.example.walkwalk;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

public class DemoFragment extends Fragment implements SensorEventListener {
    private final String TAG = "Map";
    // 定位相关
    LocationClient mLocClient;
    public LocationClientOption locationOption;
    public MyLocationListenner myListener = new MyLocationListenner();
    public int mCurrentDirection = 0;
    public double mCurrentLat = 0.0;
    public double mCurrentLon = 0.0;

    public MapView mMapView;
    public BaiduMap mBaiduMap;

    public TextView info;
    private RelativeLayout progressBarRl;

    boolean isFirstLoc = true; // 是否首次定位
    public MyLocationData locData;
    float mCurrentZoom = 19f;//默认地图缩放比例值

    public SensorManager mSensorManager;

    //起点图标
    BitmapDescriptor startBD ;
    //终点图标
    BitmapDescriptor finishBD ;

    List<LatLng> points = new ArrayList<LatLng>();//位置点集合
    Polyline mPolyline;//运动轨迹图层
    LatLng last = new LatLng(0, 0);//上一个定位点
    MapStatus.Builder builder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(getActivity().getApplicationContext());
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        View view = inflater.inflate(R.layout.map_fragmentdemo,null);
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);// 获取传感器管理服务

        // 地图初始化
        mMapView = (MapView)view.findViewById(R.id.baidumapView_f);
        mBaiduMap = mMapView.getMap();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_startpoint);
        finishBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_finishpoint);

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
        mLocClient = new LocationClient(getActivity());
        mLocClient.registerLocationListener(myListener);
        locationOption = new LocationClientOption();
//        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//只用gps定位，需要在室外定位。
//        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度定位
//        option.setOpenGps(true); // 打开gps
//        option.setCoorType("bd09ll"); // 设置坐标类型
//        option.setScanSpan(1000);
        initLocationOption();
//        mLocClient.setLocOption(option);
        initView();
    }
    private void initView() {
            startMap();
//        Button start = (Button) findViewById(R.id.buttonStart);
//        Button finish = (Button) findViewById(R.id.buttonFinish);
//        info = (TextView) findViewById(R.id.info);
//        progressBarRl = (RelativeLayout) findViewById(R.id.progressBarRl);

//        start.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (mLocClient != null && !mLocClient.isStarted()) {
//        mLocClient.setLocOption(locationOption);
//        mLocClient.start();
//        Log.d(TAG, "onClick:  mLocClient.start()");
//                    progressBarRl.setVisibility(View.VISIBLE);
//                    info.setText("GPS信号搜索中，请稍后...");
//                    mBaiduMap.clear();
//                }
//            }
//        });

//        finish.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                if (mLocClient != null && mLocClient.isStarted()) {
//                    mLocClient.stop();
//
////                    progressBarRl.setVisibility(View.GONE);
//
//                    if (isFirstLoc) {
//                        points.clear();
//                        last = new LatLng(0, 0);
//                        return;
//                    }
//
//                    MarkerOptions oFinish = new MarkerOptions();// 地图标记覆盖物参数配置类
//                    oFinish.position(points.get(points.size() - 1));
//                    oFinish.icon(finishBD);// 设置覆盖物图片
//                    mBaiduMap.addOverlay(oFinish); // 在地图上添加此图层
//
//                    //复位
//                    points.clear();
//                    last = new LatLng(0, 0);
//                    isFirstLoc = true;
//
//                }
//            }
//        });

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
            Log.d(TAG, "onReceiveLocation: 1");
            //注意这里只接受gps点，需要在室外定位。
//            if (location.getLocType() == BDLocation.TypeGpsLocation) {
            Log.d(TAG, "onReceiveLocation: 2");
//                info.setText("GPS信号弱，请稍后...");
            if (isFirstLoc) {//首次定位
                //第一个点很重要，决定了轨迹的效果，gps刚开始返回的一些点精度不高，尽量选一个精度相对较高的起始点
//                    LatLng ll = null;
//                    ll = getMostAccuracyLocation(location);
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
//        builder = new MapStatus.Builder();
//        builder.target(ll).zoom(mCurrentZoom);
//        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.animateMapStatus(update);
        update=MapStatusUpdateFactory.zoomTo(19f);
        mBaiduMap.animateMapStatus(update);
    }

    /**
     * 首次定位很重要，选一个精度相对较高的起始点
     * 注意：如果一直显示gps信号弱，说明过滤的标准过高了，
     你可以将location.getRadius()>25中的过滤半径调大，比如>40，
     并且将连续5个点之间的距离DistanceUtil.getDistance(last, ll ) > 5也调大一点，比如>10，
     这里不是固定死的，你可以根据你的需求调整，如果你的轨迹刚开始效果不是很好，你可以将半径调小，两点之间距离也调小，
     gps的精度半径一般是10-50米
     */
    private LatLng getMostAccuracyLocation(BDLocation location){
        if (location.getRadius()>100) {//gps位置精度大于40米的点直接弃用
            return null;
        }
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        if (DistanceUtil.getDistance(last, ll ) > 10) {
            last = ll;
            points.clear();//有任意连续两点位置大于10，重新取点
            return null;
        }
        points.add(ll);
        last = ll;
        //有5个连续的点之间的距离小于10，认为gps已稳定，以最新的点为起始点
        if(points.size() >= 5){
            points.clear();
            return ll;
        }
        return null;
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

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onStop() {
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
//        // 退出时销毁定位
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
}
