package com.dataexpo.gaizhuangche.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.dataexpo.gaizhuangche.BascActivity;
import com.dataexpo.gaizhuangche.R;
import com.dataexpo.gaizhuangche.comm.DBUtils;
import com.dataexpo.gaizhuangche.comm.FileUtils;
import com.dataexpo.gaizhuangche.comm.Utils;
import com.dataexpo.gaizhuangche.model.TestResult;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;

public class OfflineModelActivity extends BascActivity implements View.OnClickListener{
    private static final String TAG = OfflineModelActivity.class.getSimpleName();
    private Context mContext;
    private TextView tv_last;
    private TextView tv_qrcode_warning;
    private TextView tv_time;
    private TextView tv_expoid;
    private TextView tv_name;
    private TextView tv_code;
    private TextView tv_company;
    private TextView tv_role;
    private TextView tv_number;
    private String qrcode = "";
    private String qrcode_last = "";
    private EditText et_code;
    private long perTime_qrcode = 0L;
    HashMap<Integer, Integer> soundMap = new HashMap<>();
    private SoundPool soundPool;

    private String[] values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_offline_model);
        initView();
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(mContext, R.raw.rescan, 1)); //播放的声音文件
    }

    private void initView() {
        findViewById(R.id.btn_offline_model_back).setOnClickListener(this);
        findViewById(R.id.tv_offline_check_in).setOnClickListener(this);
        tv_qrcode_warning = findViewById(R.id.tv_offline_qrcode_scan_warning);
        tv_last = findViewById(R.id.tv_offline_last_scan_value);
        et_code = findViewById(R.id.et_qrcode);
        tv_time = findViewById(R.id.tv_offline_qrcode_scan_time);
        tv_expoid = findViewById(R.id.tv_expo_id_value);
        tv_name = findViewById(R.id.tv_expo_name_value);
        tv_code = findViewById(R.id.tv_expo_code_value);
        tv_company = findViewById(R.id.tv_expo_company_value);
        tv_role = findViewById(R.id.tv_expo_role_value);
        tv_number = findViewById(R.id.tv_expo_number_value);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void qrcodeScanEnd() {
        String scanValue = et_code.getText().toString().trim().replaceAll("\n", "");

        if (TextUtils.isEmpty(scanValue)) {
            //scanError(tv_qrcode_warning, R.string.null_scan);
            et_code.setText("");
            return;
        }

        //设置通码，当扫描结果是通码时，直接通行
        if ("YW000000".equals(scanValue)) {
            et_code.setText("");
            tv_expoid.setText("");
            tv_code.setText("");
            tv_company.setText("");
            tv_role.setText("");
            tv_number.setText("");
            long dateTime = new Date().getTime();// - 60*60*24*1000;
            String date = Utils.formatTime(dateTime, "yyyy-MM-dd_HH:mm:ss");
            tv_time.setText(date);
            DBUtils.getInstance().insertData("通码", date);
            tv_name.setText("通码，请开闸");
            return;
        }

        if (!checkAES128CBC(scanValue)) {
            scanError(tv_qrcode_warning, R.string.scan_value_error);
            final AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(mContext);
            normalDialog.setMessage("扫描内容异常！");
            normalDialog.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            normalDialog.setPositiveButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            //Toast.makeText(this, "扫描内容异常！", Toast.LENGTH_SHORT).show();
            // 显示
            normalDialog.show();
            tv_expoid.setText("");
            tv_name.setText("");
            tv_code.setText("");
            tv_company.setText("");
            tv_role.setText("");
            tv_number.setText("");
            tv_time.setText("");
            qrcode = "";
            playSound();
            et_code.setText("");
            return;
        }

//        if (((new Date().getTime()) - perTime_qrcode < 2000) && qrcode.equals(qrcode_last)) {
//            scanError(tv_qrcode_warning, R.string.repeat_scan);
//            et_code.setText("");
//            return;
//        }

        tv_qrcode_warning.setText("");
        long dateTime = new Date().getTime();
        String date = Utils.formatTime(dateTime, "yyyy-MM-dd_HH:mm:ss");
        tv_time.setText(date);
        tv_expoid.setText(values[0]);
        tv_name.setText(values[1]);
        tv_code.setText(values[4]);
        tv_company.setText(values[2]);
        tv_role.setText(values[3]);
        tv_number.setText(values[5]);

        offlineCheckIn(values[1], values[2], values[3], values[4], values[0]);

        values = null;
        et_code.setText("");
    }

    private boolean checkAES128CBC(String str) {
        //获取第一个 ： 之后的内容进行解码
        String[] sp = str.split(":");
        if (sp.length != 2) {
            return false;
        }
        String decode = Utils.decrypt(sp[1]);
        //Log.i(TAG, "decode is " + decode);
        if (decode == null) {
            return false;
        }
        values = decode.split("\\|");
        //Log.i(TAG, "values.length " + values.length);
        if (values.length == 6 && ("2020A".equals(values[0]) || "2020B".equals(values[0]))) {
            qrcode = values[4];
            return true;
        }
        return false;
    }

    private void playSound() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    soundPool.play(soundMap.get(1), 1, // 左声道音量
                            1, // 右声道音量
                            1, // 优先级，0为最低
                            0, // 循环次数，0无不循环，-1无永远循环
                            1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
                    );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(TAG, " event:" + event.toString());

        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            //if ("".equals(tv_code.getText().toString().trim())) {
                qrcodeScanEnd();
            //}
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_offline_model_back:
                finish();
                break;

            case R.id.tv_offline_check_in:
//                if (!checkInput()) {
//                    break;
//                }
//                toCheckIn();

                break;
            default:
        }
    }

    private void toCheckIn() {
        if (DBUtils.getInstance().count(qrcode) > 1) {
            final AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(mContext);
            normalDialog.setMessage("当日已签到超过2次，是否继续签到！");
            normalDialog.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //offlineCheckIn();
                            //Log.i(TAG, "delete codesize :" + userInfo.data.euPrintCount);
                        }
                    });
            normalDialog.setPositiveButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tv_expoid.setText("");
                            tv_name.setText("");
                            tv_code.setText("");
                            tv_company.setText("");
                            tv_role.setText("");
                            tv_number.setText("");
                            tv_time.setText("");
                            qrcode = "";
                        }
                    });
            // 显示
            normalDialog.show();
        } else {
            //offlineCheckIn();
        }
    }

    private void offlineCheckIn(String name, String company, String role, String code, String expoid) {

//        MyAsyncTask task  = new MyAsyncTask();
//        task.execute(123);

        long dateTime = new Date().getTime();
        String date = Utils.formatTime(dateTime, "yyyy-MM-dd_HH:mm:ss");
        DBUtils.getInstance().insertData(qrcode, date);
        FileUtils.saveRecord(name, date, company, role, code, expoid);
        tv_last.setText(qrcode_last);

        qrcode_last = qrcode;
        //perTime_qrcode = dateTime;
//        String c = "2020A|杨勇勇|数展|监管机构|1234567898765|2020A";
//        String encode = Utils.encrypt(c);
//        Log.i(TAG, "encode: " + encode);
    }

    private boolean checkInput() {
        if (TextUtils.isEmpty(qrcode)) {
            return false;
        }

        //TODO : some code check
        return true;
    }

    private void scanError(TextView v, int msgId) {
        v.setText(msgId);
    }

    public class MyAsyncTask extends AsyncTask<Integer, String, String> {
        @Override
        protected String doInBackground(Integer... integers) {
            RestTemplate template = new RestTemplate();
            //template.getForEntity("http://192.168.1.27:8090/authtool/getBase64?code=123456789&name=45645788956", TestResult.class);
            ResponseEntity<TestResult> result = template.getForEntity("http://192.168.1.27:8090/authtool/getBase64?code=123456789&name=45645788956", TestResult.class);

            return  result.getBody().getDate();
        }
        /**
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPostExecute(String result) {
            //btn.setText("线程结束" + result);
            Log.i(TAG, "exec end");
        }
        //该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
        @Override
        protected void onPreExecute() {
            //btn.setText("开始执行异步线程");
        }
        @Override
        protected void onProgressUpdate(String... values) {

        }
    }
}
