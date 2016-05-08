package jp.ac.dendai.im.cps.citywalkersmeter.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import jp.ac.dendai.im.cps.citywalkersmeter.R;

public class QuestionActivity1 extends ActionBarActivity {

    private static String POST_URL = "http://citylog.cps.im.dendai.ac.jp/api/questionnaires/update";
    private static String PARAM_USER_ID = "userId";
    /*
    private static String PARAM_Q1 = "q1";
    private static String PARAM_Q2 = "q2";
    private static String PARAM_Q3 = "sex";
    private static String PARAM_Q4 = "age";
    private static String PARAM_Q5 = "address";
    */
    private static String PARAM_Q1 = "sex";
    private static String PARAM_Q2 = "age";
    private static String PARAM_Q3 = "q1";
    private static String PARAM_Q4 = "q2";
    private static String PARAM_Q5 = "address";
       /*
       * q1 : 性別
       * p2 : 年齢
       * q3 : 目的
       * q4 : 同伴者
       * q5 : 住所
       */

    private SharedPreferences prefs;
    private int age = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question1);

        prefs = getSharedPreferences("city_walker_id", Context.MODE_PRIVATE);
    }

    /*
     * q1 : 性別
     * p2 : 年齢
     * q3 : 目的
     * q4 : 同伴者
     * q5 : 住所
     */
    public void onSendClick(View v) {
        String result = "";
        int q1 = -1;
        int q2 = -1;
        int q3 = 0;
        int q4 = 0;
        String address = "";

        //問題1
        RadioGroup gender = (RadioGroup) findViewById(R.id.gender);
        q1 = gender.getCheckedRadioButtonId();
        switch (q1) {
            case R.id.otoko:
                q1 = 0;
                break;
            case R.id.onna:
                q1 = 1;
                break;
            default:
                q1 = -1;
                break;
        }
        if (q1 < 0) {
            Toast.makeText(this, "性別が選択されていません", Toast.LENGTH_SHORT).show();
            return;
        }
        result += "問題1 -> " + q3 + "\n";

        //問題2
        RadioGroup age = (RadioGroup) findViewById(R.id.age_group);
        q2 = age.getCheckedRadioButtonId();
        switch (q2) {
            case R.id.ten:
                q2 = 0;
                break;
            case R.id.twenty:
                q2 = 1;
                break;
            case R.id.thirty:
                q2 = 2;
                break;
            case R.id.forty:
                q2 = 3;
                break;
            case R.id.fifty:
                q2 = 4;
                break;
            case R.id.sixty:
                q2 = 5;
                break;
            default:
                q2 = -1;
                break;
        }
        if (q2 < 0) {
            Toast.makeText(this, "年齢を選択してください", Toast.LENGTH_SHORT).show();
            return;
        }
        result += "問題2 -> " + q2 + "\n";

        //問題3
        int[] ids = new int[]{ R.id.q3_check1, R.id.q3_check2, R.id.q3_check3, R.id.q3_check4, R.id.q3_check5, R.id.q3_check6 };
        CheckBox[] boxs = new CheckBox[6];
        for (int i = 0; i < 6; i++) {
            boxs[i] = (CheckBox) findViewById(ids[i]);

            q3 <<= 1;
            if (boxs[i].isChecked()) {
                q3 += 1;
            }
        }

        if (q3 == 0) {
            Toast.makeText(this, "問題3が選択されていません", Toast.LENGTH_SHORT).show();
            return;
        }
        result += Integer.toBinaryString(q3) + "\n";

        //問題4
        ids = new int[]{ R.id.q4_check1, R.id.q4_check2, R.id.q4_check3, R.id.q4_check4, R.id.q4_check5, R.id.q4_check6 };
        boxs = new CheckBox[6];
        for (int i = 0; i < 6; i++) {
            boxs[i] = (CheckBox) findViewById(ids[i]);

            q4 <<= 1;
            if (boxs[i].isChecked()) {
                q4 += 1;
            }
        }

        if (q4 == 0) {
            Toast.makeText(this, "問題4が選択されていません", Toast.LENGTH_SHORT).show();
            return;
        }
        result += Integer.toBinaryString(q4) + "\n";


        //問題５
        //住所は未記入でも可
        EditText editText = (EditText)findViewById(R.id.address);
        address = editText.getText().toString();

        result += "問題５ -> " + address;

//        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

//        HttpPostHandler postHandler = new HttpPostHandler() {
//            @Override
//            public void onPostCompleted(String response) {
//                String str = "送信成功 : " + response;
////                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
//                Log.d("posttest", response);
//
//            }
//
//            @Override
//            public void onPostFailed(String response) {
//                String str = "送信失敗 : " + response;
////                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
//                Log.d("posttest", response);
//            }
//        };

       /*
       * q1 : 性別
       * p2 : 年齢
       * q3 : 目的
       * q4 : 同伴者
       * q5 : 住所
       */
//        HttpPostTask task = new HttpPostTask(POST_URL, postHandler);
//        task.addPostParam(PARAM_USER_ID, String.valueOf(prefs.getInt(PARAM_USER_ID, -1)));
//        task.addPostParam(PARAM_Q1, String.valueOf(q1));
//        task.addPostParam(PARAM_Q2, String.valueOf(q2));
//        task.addPostParam(PARAM_Q3, String.valueOf(q3));
//        task.addPostParam(PARAM_Q4, String.valueOf(q4));
//        task.addPostParam(PARAM_Q5, address);
//        task.execute();

        Toast.makeText(this, "アンケートにご協力頂きありがとう御座いました。", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_question_activity1, menu);
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
