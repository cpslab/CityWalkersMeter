package jp.ac.dendai.im.cps.citywalkersmeter.activities;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import jp.ac.dendai.im.cps.citywalkersmeter.GpsService;
import jp.ac.dendai.im.cps.citywalkersmeter.LogData;
import jp.ac.dendai.im.cps.citywalkersmeter.R;
import jp.ac.dendai.im.cps.citywalkersmeter.dialogs.EditTextDialog;
import jp.ac.dendai.im.cps.citywalkersmeter.dialogs.OkCancelDialog;
import jp.ac.dendai.im.cps.citywalkersmeter.networks.ApiClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static String PARAM_USER_ID = "userId";
    private static String PARAM_PROJECT_ID = "projectId";
    private static String PARAM_IS_SERVICE = "isService";
    private static String PARAM_FINISH_TIME = "finishTime";
    private static String PARAM_INTERVAL_TIME = "intervalTime";

    private static final String TAG = MainActivity.class.getSimpleName();

    /*
     * 0 : latitude
     * 1 : longitude
     * 2 : accuracy
     * 3 : speed
     * 4 : bearing
     * 5 : altitude
     * 6 : timestamp
     * 7 : light
     * 8 : pressure
     * 9 : temprature
     * 10 : humidity
     * 11 : bearing
     * 12 : step
     * 13 : accelerometers_x
     * 14 : accelerometers_y
     * 15 : accelerometers_z
     * 16 : gyroscope_x
     * 17 : gyroscope_y
     * 18 : gyroscope_z
     */
    private TextView[] sensor_strs = new TextView[19];
    private int[] sensor_strs_id = new int[]{
            R.id.lat_str, R.id.lng_str, R.id.acc_str, R.id.speed_str, R.id.bearing, R.id.alti_str, R.id.time_str,
            R.id.light_str, R.id.pressure_str, R.id.temprature_str, R.id.humidity_str, R.id.bearing, R.id.step_str,
            R.id.accelex_str, R.id.acceley_str, R.id.accelez_str, R.id.gyrox_str, R.id.gyroy_str, R.id.gyroz_str};
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
        for (int i = 0; i < sensor_strs_id.length; i++) {
            sensor_strs[i] = (TextView)findViewById(sensor_strs_id[i]);
            if (i == 1) {
                sensor_strs[i].setText("計測中...");
            }
        }
        isMesureStr = (TextView) findViewById(R.id.isMeasure);

        // projectIdの初期化
        String projectId = prefs.getString(PARAM_PROJECT_ID, "");

        if (projectId.equals("")) {
            showInputProjectIdDialog();
        }

        TextView proIdStr = (TextView)findViewById(R.id.projectId);
        proIdStr.setText("Project Id : " + projectId);

        //UserIdの初期化
        int myId =  prefs.getInt(PARAM_USER_ID, -1);
        TextView idStr = (TextView)findViewById(R.id.id);

        if (myId < 0) {
            ApiClient client = new ApiClient() {
                TextView handlerIdStr = (TextView)findViewById(R.id.id);
                TextView idErrorStr = (TextView)findViewById(R.id.idErrorStr);

                @Override
                public void onFailure(Request request, IOException e) {
                    String str = "登録失敗";
//                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity", str);
                    handlerIdStr.setText("端末ID : ERROR");
                    idErrorStr.setVisibility(View.VISIBLE);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String str = response.body().string();
                    Log.d("testpost", "登録成功 : " + str);
                    Log.d("onResponse", "onResponse: " + response.message());
                    Log.d("onResponse", "onResponse: " + response.toString());

                    try {
                        JSONObject object = new JSONObject(str);
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
                }
            };
            client.postUserId(prefs.getString(PARAM_PROJECT_ID, ""));
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

        //GPSが無効になっている場合は有効にするよう促す
        gpsCheck("GPSを有効にしてください");
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

    public void gpsCheck(String message) {
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER));
        if (gpsEnabled) {
            return;
        }

        OkCancelDialog dialogFragment = OkCancelDialog.newInstance(R.string.app_name, message);
        dialogFragment.setOnOkClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(settingsIntent);
            }
        });
        dialogFragment.setOnCancelClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogFragment.show(getSupportFragmentManager(), "dialog_fragment");
    }

    public void showInputProjectIdDialog() {
        final TextView proIdStr = (TextView) findViewById(R.id.projectId);
        final EditText editText = new EditText(MainActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setText("");

        EditTextDialog dialogFragment = EditTextDialog
                .newInstance(R.string.app_name, "Project ID を入力してください");
        dialogFragment.setOnOkClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str = editText.getText().toString();
                if (str.equals("")) {
                    Toast.makeText(MainActivity.this, "入力してください", Toast.LENGTH_SHORT).show();
                    return;
                }

                //アプリにidを登録
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PARAM_PROJECT_ID, str);
                editor.commit();
                proIdStr.setText("Project Id : " + editText.getText().toString());
            }
        });
        dialogFragment.setEditText(editText);
        dialogFragment.show(getSupportFragmentManager(), "dialog_fragment");
    }

    public void onProIdClick(View v) {
        showInputProjectIdDialog();
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

                            sensor_strs[2].setText("Accuracy : " + updateData.getAccuracy());
                            sensor_strs[3].setText("Speed : " + updateData.getSpeed());
                            sensor_strs[4].setText("Bearing : " + updateData.getBearing());

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T' HH:mm:ss.SSSz");
                            sensor_strs[6].setText("GPSTime : " + sdf.format(updateData.getTimestamp() * 1000).toString());

//                            長すぎるので整形
                            BigDecimal bd = new BigDecimal(updateData.getLat());
                            BigDecimal bdLatitude = bd.setScale(8, BigDecimal.ROUND_DOWN);
                            sensor_strs[0].setText("Latitude : " + bdLatitude.doubleValue());

                            bd = new BigDecimal(updateData.getLng());
                            BigDecimal bdLongitude = bd.setScale(8, BigDecimal.ROUND_DOWN);
                            sensor_strs[1].setText("Longitude : " + bdLongitude.doubleValue());

                            bd = new BigDecimal(updateData.getAltitude());
                            BigDecimal bdAltitude = bd.setScale(8, BigDecimal.ROUND_DOWN);
                            sensor_strs[5].setText("Altitude : " + bdAltitude.doubleValue());

                            sensor_strs[7].setText("Light : " + updateData.getLight());

                            sensor_strs[8].setText("Pressure : " + updateData.getPressure());

                            sensor_strs[9].setText("Temperature : " + updateData.getTemprature());

                            sensor_strs[10].setText("Humidity : " + updateData.getHumidity());

                            sensor_strs[11].setText("Bearing : " + updateData.getBearing());

                            sensor_strs[12].setText("Step : " + updateData.getStep());

                            double[] accelerometers = updateData.getAccelerometers();
                            if (accelerometers != null) {
                                sensor_strs[13].setText("Accelerometer_x : " + accelerometers[0]);
                                sensor_strs[14].setText("Accelerometer_y : " + accelerometers[1]);
                                sensor_strs[15].setText("Accelerometer_z : " + accelerometers[2]);
                            }

                            double[] gyroscopes = updateData.getGyroscope();
                            if (gyroscopes != null) {
                                sensor_strs[16].setText("Gyroscope_x : " + gyroscopes[0]);
                                sensor_strs[17].setText("Gyroscope_y : " + gyroscopes[1]);
                                sensor_strs[18].setText("Gyroscope_z : " + gyroscopes[2]);
                            }
//                            ここまで

                            geoStr.setText(reverseGeocode(updateData.getLat(), updateData.getLng()));

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
        gpsCheck("GPSを有効にしてください");

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
        if (latitude == 0.0 && longitude == 0.0) {
            return str;
        }
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