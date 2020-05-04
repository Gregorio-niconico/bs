package com.example.walkwalk.ViewPaper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.walkwalk.HomeActivity;
import com.example.walkwalk.R;

import java.util.ArrayList;
import java.util.List;



public class SportsFragment extends Fragment {
    private static String TAG = "SportsFragmrnt";
    private Context mContext;
    private static TextView positionText;
    private static MapView mapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private LocationClientOption locationOption;
    private MyLocationListener myListener = new MyLocationListener();
    private boolean isFirstLocate=true;
    private SwipeRefreshLayout swipeRefresh;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sport, null);
        mContext = getActivity().getApplicationContext();
        //声明LocationClient类
        mLocationClient = new LocationClient(mContext);
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        //声明LocationClient类实例并配置定位参数
        locationOption = new LocationClientOption();
        positionText = (TextView) view.findViewById(R.id.position_text_view);
        mapView = (MapView) view.findViewById(R.id.bmapView);
        mBaiduMap=mapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        mBaiduMap.setMyLocationEnabled(true);
        swipeRefresh = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPink);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMap();
            }
        });
        return view;
    }




    @Override
    public void onStart() {
        super.onStart();

        List<String> permissionList = new ArrayList<>();
        //申请危险权限
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            //将权限的申请情况授权与否的List转换为数组
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            //一次性申请三个权限
            SportsFragment.this.requestPermissions(permissions, 1);
        } else {
            requestLocation();
        }
    }

    private void refreshMap(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    requestLocation();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //请求定位
    private void requestLocation() {
        initLocationOption();
        mLocationClient.setLocOption(locationOption);
        //开始定位
        mLocationClient.start();
    }

    //地图视角移动到定位处
    private void nacigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update=MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(16f);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate=false;
        }
    }
    /**
     * 初始化定位参数配置
     */
    private void initLocationOption() {

//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("gcj02");
//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000);
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

    //用于实现定位监听
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            StringBuilder currentPosition=new StringBuilder();
            //获取纬度信息
            double latitude = location.getLatitude();
            currentPosition.append("纬度：").append(latitude).append("\n");
            //获取经度信息
            double longitude = location.getLongitude();
            currentPosition.append("经度：").append(longitude).append("\n");
            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            currentPosition.append("省份：").append(province).append("\n");
            String city = location.getCity();    //获取城市
            currentPosition.append("城市：").append(city).append("\n");
            String district = location.getDistrict();    //获取区县
            currentPosition.append("区县：").append(district).append("\n");
            String street = location.getStreet();    //获取街道信息
            currentPosition.append("街道：").append(street).append("\n");
            String adcode = location.getAdCode();    //获取adcode
            String town = location.getTown();    //获取乡镇信息
            currentPosition.append("乡镇：").append(town).append("\n");
            //获取定位精度，默认值为0.0f
            float radius = location.getRadius();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            String coorType = location.getCoorType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            int errorCode = location.getLocType();
            Log.d(TAG, "onReceiveLocation: "+currentPosition);
            setTextView(currentPosition);

            //mapView 销毁后不在处理新接收的位置
            if (location == null || mapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            nacigateTo(location);
        }
    }


    public  void setTextView(StringBuilder str) {
        positionText.setText(str);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView=null;
        super.onDestroy();

    }
}

    //权限请求结果
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode){
//            case 1:
//                if(grantResults.length>0){
//                    for(int result : grantResults){
//                        if(result != PackageManager.PERMISSION_GRANTED){
//                            Toast.makeText(mContext,"11",Toast.LENGTH_SHORT).show();
//                            finish();
//                            return;
//                        }
//                    }
//                    requestLocation();
//                }else{
//                    Toast.makeText(mContext,"发生未知错误",Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//                break;
//            default:
//        }
//    }


