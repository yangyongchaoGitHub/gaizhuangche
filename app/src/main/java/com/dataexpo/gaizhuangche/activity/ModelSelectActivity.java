package com.dataexpo.gaizhuangche.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.dataexpo.gaizhuangche.BascActivity;
import com.dataexpo.gaizhuangche.R;
import com.dataexpo.gaizhuangche.comm.DBUtils;


public class ModelSelectActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = ModelSelectActivity.class.getSimpleName();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_model_select);
        DBUtils.getInstance().create(mContext);
        initView();

        //测试上传离线数据
//        long dateTime = new Date().getTime();
//        String date = Utils.formatTime(dateTime, "yyyy-MM-dd HH:mm:ss");
//        for (int i = 0; i < 1000; i++) {
//            DBUtils.getInstance().insertData(i+"", date, i+"");
//        }
//        DBUtils.getInstance().delDataAll();
    }

    private void initView() {
        findViewById(R.id.tv_model_selecct_offline).setOnClickListener(this);
        findViewById(R.id.tv_model_selecct_show_data).setOnClickListener(this);
        //显示版本号
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),0);
            ((TextView)findViewById(R.id.tv_version_show)).setText(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_model_selecct_offline:
                startActivity(new Intent(mContext, OfflineModelActivity.class));
                break;

            case R.id.tv_model_selecct_show_data:
                startActivity(new Intent(mContext, ScanRecordActivity.class));
                break;

            default:
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "keyCode: " + keyCode + " event:" + event.toString());
        if (event.getKeyCode() == KeyEvent.KEYCODE_1) {
            startActivity(new Intent(mContext, OfflineModelActivity.class));
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_2) {
            startActivity(new Intent(mContext, ScanRecordActivity.class));
        }
        return super.onKeyDown(keyCode, event);
    }
}
