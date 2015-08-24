package jp.ac.dendai.im.cps.citywalkersmeter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private String text = "start";
    private SharedPreferences prefs;

    private static String REGIST_URL = "http://citylog.cps.im.dendai.ac.jp/api/users/regist";
    private static String PARAM_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prefs = getSharedPreferences("city_walker_id", Context.MODE_PRIVATE);

        int myId =  prefs.getInt(PARAM_USER_ID, -1);
        Log.d("posttest", String.valueOf(myId));
        if (myId < 0) {

            HttpPostHandler postHandler = new HttpPostHandler() {
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

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onPostFailed(String response) {
                    String str = "登録失敗 : " + response;
                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                }
            };

            HttpPostTask task = new HttpPostTask(REGIST_URL, postHandler);
            task.execute();
        }
    }

    public void onStartService(View v) {
            Intent intent = new Intent(this, GpsService.class);
            startService(intent);
    }

    public void onStopService(View v) {
            Intent intent = new Intent(this, GpsService.class);
            stopService(intent);
    }

    public void onQuesionClick(View v){
        startActivity(new Intent(this, QuestionActivity1.class));
    }

    public void onTestSend(View v) {
        //exec_post(LogData.newInstance(35.689689, 139.692692, 10.53f, System.currentTimeMillis() / 1000L, 10));
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
