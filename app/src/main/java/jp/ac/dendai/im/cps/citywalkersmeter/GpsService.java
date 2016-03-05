package jp.ac.dendai.im.cps.citywalkersmeter;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.ac.dendai.im.cps.citywalkersmeter.networks.ApiClient;
import okhttp3.Request;
import okhttp3.Response;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class GpsService extends Service implements LocationListener, SensorEventListener {
    private static final String TAG = GpsService.class.getSimpleName();

    private NotificationManager mNotificationManager;
    private LocationManager locationManager;
    private SharedPreferences prefs;
    private Timer timer = new Timer();
    private int nowTime = 0;
    private LogData sensingData = new LogData();
    private SensorManager mSensorManager;
    private Integer[] array_types = new Integer[] {Sensor.TYPE_LIGHT, Sensor.TYPE_PRESSURE, Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_RELATIVE_HUMIDITY, Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_STEP_COUNTER};

    private static String PARAM_USER_ID = "userId";
    private static String PARAM_INTERVAL_TIME = "intervalTime";

    private final IBinder mBinder = new GpsServiceBinder();

    public class GpsServiceBinder extends Binder {
        GpsService getService() {
            return GpsService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getSharedPreferences("city_walker_id", Context.MODE_PRIVATE);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //GPSが無効になっている場合は有効にするよう促す
        final boolean gpsEnabled = locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER));
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(settingsIntent);
        }

        int intervalTime = prefs.getInt(PARAM_INTERVAL_TIME, -1);
        if (intervalTime < 0) { intervalTime = 2; }
        SharedPreferences.Editor intervalEditor = prefs.edit();
        intervalEditor.putInt(PARAM_INTERVAL_TIME, intervalTime);
        intervalEditor.commit();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, intervalTime * 1000, 5, this);

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        List<Integer> types = Arrays.asList(array_types);
        for (Sensor s : sensors) {
            if (types.contains(s.getType())) {
                mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
            }
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.icon);
        mBuilder.setContentTitle("CityWalkerMeterKai");
        mBuilder.setContentText("位置情報計測中...");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        /*
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
        */

        //サービス永続化
        startForeground(R.string.app_name, mBuilder.build());

        setFinishTime(6);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                exec_post();
            }
        }, 0, 5000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        locationManager.removeUpdates(this);
        mSensorManager.unregisterListener(this);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void exec_post() {
        ApiClient client = new ApiClient() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("onFailure", "onFailure: ", e.fillInStackTrace());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String str = "送信成功 : " + response.body().string();
                Log.d("onResponse", str);
                Log.d("onResponse", "onResponse: " + response.toString());
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(sensingData);
            client.updateLog(String.valueOf(prefs.getInt(PARAM_USER_ID, -1)), json);

            Log.d("posttest", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public LogData getData() {
        if (sensingData == null) {
            return null;
        }
        return sensingData;
    }

    public void setFinishTime(int hour) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        nowTime = 0;
        final int sec = hour *60 *60;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                nowTime++;
                if (nowTime > sec) {
                    stopSelf();
                }
            }
        }, 0, 1000);
    }

    public void setIntervalTime(int second) {
        locationManager.removeUpdates(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, second * 1000, 5, this);
    }

    //---------GPS location-----------

    @Override
    public void onLocationChanged(Location location) {
        sensingData.setLat(location.getLatitude());
        sensingData.setLng(location.getLongitude());
        sensingData.setAccuracy(location.getAccuracy());
        sensingData.setTimestamp((int) (location.getTime() / 1000));
        sensingData.setSpeed(location.getSpeed());
        sensingData.setAltitude(location.getAltitude());
        sensingData.setBearing(location.getBearing());

        exec_post();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        /*
        switch (status) {
            case LocationProvider.AVAILABLE: {
                text += "LocatoinProvider.AVAILABLE\n";
                textView.setText(text);
                break;
            }
            case LocationProvider.OUT_OF_SERVICE: {
                text += "LocationProvider.OUT_OF_SERVICE\n";
                textView.setText(text);
                break;
            }
            case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                text += "LocationProvider.TEMPORARILY_UNAVAILABLE";
                textView.setText(text);
                break;
            }
        }
        */
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LIGHT:
                sensingData.setLight(event.values[0]);
                break;
            case Sensor.TYPE_PRESSURE:
                sensingData.setPressure(event.values[0]);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                sensingData.setTemprature(event.values[0]);
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                sensingData.setHumidity(event.values[0]);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                sensingData.setAccelerometers(new float[]{event.values[0], event.values[1], event.values[2]});
                break;
            case Sensor.TYPE_GYROSCOPE:
                sensingData.setGyroscope(new float[]{event.values[0], event.values[1], event.values[2]});
                break;
            case Sensor.TYPE_STEP_COUNTER:
                sensingData.setStep(event.values[0]);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
