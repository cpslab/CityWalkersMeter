package jp.ac.dendai.im.cps.citywalkersmeter;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static String REGIST_URL = "http://citylog.cps.im.dendai.ac.jp/api/users/regist";
    private static String PARAM_USER_ID = "userId";
    static final String PARAM_PROJECT_ID = "projectId";
    private static String PARAM_IS_SERVICE = "isService";
    private static String PARAM_FINISH_TIME = "finishTime";
    private static String PARAM_INTERVAL_TIME = "intervalTime";

    /*
     * 0 : latitude
     * 1 : longitude
     * 2 : accuracy
     * 3 : speed
     * 4 : bearing
     * 5 : altitude
     * 6 : timestamp
     */
    private TextView[] sensor_strs = new TextView[7];
//    private int[] sensor_strs_id = new int[]{
//            R.id.lat_str, R.id.lng_str, R.id.acc_str, R.id.speed_str, R.id.bearing, R.id.alti_str, R.id.time_str };
    private TextView isMesureStr;
    private boolean isBind = false;
    private boolean isTimer = false;
    private GpsService mService;
    private SharedPreferences prefs;
    private Timer timer;
    private Handler handler = new Handler();

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GpsService.GpsServiceBinder binder = (GpsService.GpsServiceBinder) service;
            mService = binder.getService();
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prefs = getSharedPreferences("city_walker_id", Context.MODE_PRIVATE);

        //Top画面TextViewの初期化
//        for (int i = 0; i < sensor_strs_id.length; i++) {
//            sensor_strs[i] = (TextView)findViewById(sensor_strs_id[i]);
//            if (i == 1) {
//                sensor_strs[i].setText("計測中...");
//            }
//        }
        isMesureStr = (TextView) findViewById(R.id.isMeasure);

        //UserIdの初期化
        int myId =  prefs.getInt(PARAM_USER_ID, -1);
        TextView idStr = (TextView)findViewById(R.id.id);

        if (myId < 0) {
            HttpPostHandler postHandler = new HttpPostHandler() {
                TextView handlerIdStr = (TextView)findViewById(R.id.id);
                TextView idErrorStr = (TextView)findViewById(R.id.idErrorStr);

                @Override
                public void onPostCompleted(String response) {
                    String str = "登録成功 : " + response;
                    Log.d("testpost", response);

                    try {
                        JSONObject object = new JSONObject(response);
                        Log.d("testpost", object.getString("id"));

                        int id = Integer.valueOf(object.getString("id"));

                        //アプリにidを登録
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(PARAM_USER_ID, id);
                        editor.commit();

                        handlerIdStr.setText("端末ID : " + String.valueOf(id));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handlerIdStr.setText("端末ID : ERROR");
                        idErrorStr.setVisibility(View.VISIBLE);
                    }

//                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onPostFailed(String response) {
                    String str = "登録失敗 : " + response;
//                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity", str);
                    handlerIdStr.setText("端末ID : ERROR");
                    idErrorStr.setVisibility(View.VISIBLE);
                }
            };

            HttpPostTask task = new HttpPostTask(REGIST_URL, postHandler);
            task.addPostParam(PARAM_PROJECT_ID, String.valueOf(114));
            task.execute();
        }
        else {
            idStr.setText("端末ID : " + String.valueOf(myId));
        }

        //強制計測終了時間を設定
        int finishTime = prefs.getInt(PARAM_FINISH_TIME, -1);
        if (finishTime < 0) { finishTime = 6; }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PARAM_FINISH_TIME, finishTime);
        editor.commit();

        //finishTimeの表示
        TextView finishTextView = (TextView) findViewById(R.id.finishTimeStr);
        finishTextView.setText("強制終了時間 : " + String.valueOf(finishTime) + " 時間");

        final TextView progressTextView = finishTextView;
        //finishSeekbarの初期化
        SeekBar seekBar = (SeekBar) findViewById(R.id.finishSeekBar);
        seekBar.setProgress(finishTime);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int changedNumber;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //つまみを動かした際に呼ばれる
                changedNumber = seekBar.getProgress();
                progressTextView.setText("強制終了時間 : " + String.valueOf(changedNumber) + " 時間");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //つまみに触れた時に呼ばれる
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //つまみを離した時に呼ばれる
                changedNumber = seekBar.getProgress();

                if (changedNumber < 1) { changedNumber = 1; }

                progressTextView.setText("強制終了時間 : " + String.valueOf(changedNumber) + " 時間");

                //時間の保存
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(PARAM_FINISH_TIME, changedNumber);
                editor.commit();

                //serviceに設定
                setFinishTime2Service(changedNumber);
            }
        });

        //取得間隔を設定
        int intervalTime = prefs.getInt(PARAM_INTERVAL_TIME, -1);
        if (intervalTime < 0) {
            intervalTime = 2;
            SharedPreferences.Editor intervalEditor = prefs.edit();
            intervalEditor.putInt(PARAM_INTERVAL_TIME, intervalTime);
            intervalEditor.commit();
        }

        //intervalTimeの表示
        TextView intervalTextView = (TextView) findViewById(R.id.intervalTimeStr);
        intervalTextView.setText("データ取得間隔 : " + String.valueOf(intervalTime) + " 秒");

        final TextView staticIntervalTextView = intervalTextView;
        //intervalSeekbarの初期化
        SeekBar intervalTimeSeekBar = (SeekBar) findViewById(R.id.intervalTimeSeekBar);
        intervalTimeSeekBar.setProgress(intervalTime);
        intervalTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int changedNumber;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //つまみを動かした際に呼ばれる
                changedNumber = seekBar.getProgress();
                staticIntervalTextView.setText("データ取得間隔 : " + String.valueOf(changedNumber) + " 秒");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //つまみに触れた時に呼ばれる
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //つまみを離した時に呼ばれる
                changedNumber = seekBar.getProgress();

                if (changedNumber < 1) { changedNumber = 1; }

                staticIntervalTextView.setText("データ取得間隔 : " + String.valueOf(changedNumber) + " 秒");

                //時間の保存
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(PARAM_INTERVAL_TIME, changedNumber);
                editor.commit();

                //serviceに設定
                setIntervalTime2Service(changedNumber);
            }
        });
    }

    public void setFinishTime2Service(final int time) {
        if (mService == null) {
            return;
        }
        mService.setFinishTime(time);
    }

    public void setIntervalTime2Service(final int time) {
        if (mService == null) {
            return;
        }
    }

    public void startStrTimer() {
        if (!isTimer) {
            final GpsService service = mService;
//            final TextView[] textViews = sensor_strs;
            final TextView textView = isMesureStr;
            final TextView geoStr = (TextView) findViewById(R.id.reverse_geo);

            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isBind) {
                                textView.setText("計測中です。\nデータをサーバーにアップロードしています。");
                                textView.setTextColor(getResources().getColor(R.color.orangered));
                            } else {
                                textView.setText("計測していません。");
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }

                            if (mService == null) {
                                return;
                            }

                            LogData updateData = mService.getData();
                            if (updateData == null) {
                                return;
                            }

                            /*
                            textViews[2].setText("Accuracy : " + updateData.getAccuracy());
                            textViews[3].setText("Speed : " + updateData.getSpeed());
                            textViews[4].setText("Bearing : " + updateData.getBearing());

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T' HH:mm:ss.SSSz");
                            textViews[6].setText("Timestamp : " + sdf.format(updateData.getTime() *1000).toString());

//                            長すぎるので整形
                            BigDecimal bd = new BigDecimal(updateData.getLatitude());
                            BigDecimal bdLatitude = bd.setScale(8, BigDecimal.ROUND_DOWN);
                            textViews[0].setText("Latitude : " + bdLatitude.doubleValue());

                            bd = new BigDecimal(updateData.getLongitude());
                            BigDecimal bdLongitude = bd.setScale(8, BigDecimal.ROUND_DOWN);
                            textViews[1].setText("Longitude : " + bdLongitude.doubleValue());

                            bd = new BigDecimal(updateData.getAltitude());
                            BigDecimal bdAltitude = bd.setScale(8, BigDecimal.ROUND_DOWN);
                            textViews[5].setText("Altitude : " + bdAltitude.doubleValue());
//                            ここまで

                            geoStr.setText(reverseGeocode(updateData.getLatitude(), updateData.getLongitude()));

                            */
                        }
                    });
                }
            }, 0, 1000);

            isTimer = true;
        }
    }

    public void stopStrTimer() {
        if (isTimer) {
            timer.cancel();
            timer = null;
            isMesureStr.setText("計測していません。");
            isTimer = false;
        }
    }

    public void onStartService(View v) {
        Intent intent = new Intent(this, GpsService.class);
        startService(intent);

        //アプリにServiceの状態を登録
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PARAM_IS_SERVICE, true);
        editor.commit();

        if (!isBind) {
            bindService(new Intent(this, GpsService.class), mConnection, Context.BIND_AUTO_CREATE);
            isBind = true;
        }
    }

    public void onStopService(View v) {
        Intent intent = new Intent(this, GpsService.class);
        stopService(intent);

        if (isBind) {
            unbindService(mConnection);
            isBind = false;

            //アプリにServiceの状態を登録
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PARAM_IS_SERVICE, false);
            editor.commit();
        }
    }

    public void onQuesionClick(View v){
//        startActivity(new Intent(this, QuestionActivity1.class));

        // GoogleForm ブラウザ起動
        Uri uri = Uri.parse("http://goo.gl/forms/fgVARBcgyO");
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }

    public void onTestSend(View v) {
        //exec_post(LogData.newInstance(35.689689, 139.692692, 10.53f, System.currentTimeMillis() / 1000L, 10));
    }

    private boolean isServiceWorking() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GpsService.class.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private String reverseGeocode(double latitude, double longitude) {
        String str = "現在地を特定できません。";
        Geocoder geocoder = new Geocoder(this, Locale.JAPAN);
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addressList != null) {
            if (!addressList.isEmpty()) {
                str = addressList.get(1).getAddressLine(1) + " 付近です。";
            }
        }

        return str;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //アプリにServiceの状態を登録
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PARAM_IS_SERVICE, true);
        editor.commit();

        if (!isBind && isServiceWorking()) {
            bindService(new Intent(this, GpsService.class), mConnection, Context.BIND_AUTO_CREATE);
            isBind = true;
        }

        startStrTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isBind) {
            unbindService(mConnection);
            isBind = false;
        }

        stopStrTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}