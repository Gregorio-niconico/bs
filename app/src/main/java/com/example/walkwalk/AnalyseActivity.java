package com.example.walkwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.walkwalk.view.HistoryChartView;

public class AnalyseActivity extends AppCompatActivity implements HistoryChartView.OnViewLayoutListener {
    private final static int WEEK_MODE = 1;
    private HistoryChartView mHistoryChartView;
    private int mode = WEEK_MODE;
    private String mStrWeekRoomData = "2445,3009,2980,2677,2409,2845,3102";
    private String mStrWeekSettingData = "2800,2800,2800,2800,2800,2800,2800";
//    private String mStrWeekPowerTimeData = "60,70,80,80,90,95,100";
//private String mStrWeekPowerTimeData = "93,100,99,89,80,96,103";
private String mStrWeekPowerTimeData = "57,65,64,54,51,62,67";
    /**
     * 加载toolbar
     */
    private void loadToolbar(){
        androidx.appcompat.widget.Toolbar toolbar=(Toolbar)findViewById(R.id.analyse_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        //设置标题栏的按钮效果
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.back);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse);
        loadToolbar();
        initView();
    }
    public void initView() {
        mHistoryChartView = (HistoryChartView) findViewById(R.id.mutiHistoryChartView);
        mHistoryChartView.setOnViewLayoutListener(this);
    }
    /**
     * 更新界面
     */
    protected void updateView() {
        mHistoryChartView.setData(getAllHistoryViewData(), mode);
    }

    /**
     * 获取要绘制的历史数据全状态
     *
     * @return 全状态数据
     */
    private String getAllHistoryViewData() {
        String allHistoryData = "";
        switch (mode) {
            case WEEK_MODE:
                allHistoryData = mStrWeekRoomData + "-" + mStrWeekSettingData + "-"
                        + mStrWeekPowerTimeData;
                break;
            default:
                break;
        }
        return allHistoryData;
    }
    @Override
    public void onLayoutSuccess() {
        //布局onlayout成功后，更新View数据
        updateView();
    }
}
