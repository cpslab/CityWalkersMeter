package jp.ac.dendai.im.cps.citywalkersmeter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class QuestionActivity1 extends ActionBarActivity {

    private static String POST_URL = "http://citylog.cps.im.dendai.ac.jp/api/questionnaires/update";
    private static String PARAM_USER_ID = "userId";
    private static String PARAM_Q1 = "q1";
    private static String PARAM_Q2 = "q2";
    private static String PARAM_Q3 = "sex";
    private static String PARAM_Q4 = "age";
    private static String PARAM_Q5 = "address";

    private SharedPreferences prefs;
    private int age = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question1);

        prefs = getSharedPreferences("city_walker_id", Context.MODE_PRIVATE);

        //年齢ドロップダウンメニュー
        ArrayList<Integer> ages = new ArrayList<>();
        for (int i = 10; i <= 100; i++) {
            ages.add(i);
        }

        final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, ages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner = (Spinner) findViewById(R.id.age_list);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                age = (Integer) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setAdapter(adapter);
    }

    public void onSendClick(View v) {
        String result = "";
        int q1 = -1;
        int q2 = 0;
        int q3 = -1;
        int q4 = -1;
        String address = "";

        //問題１
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        q1 = radioGroup.getCheckedRadioButtonId() -1;
        if (q1 < 0) {
            Toast.makeText(this, "問題１が選択されていません", Toast.LENGTH_SHORT).show();
            return;
        }
        result += "問題１ -> " + q1 + "\n";

        //問題2
        int[] ids = new int[]{ R.id.q2_check1, R.id.q2_check2, R.id.q2_check3, R.id.q2_check4, R.id.q2_check5, R.id.q2_check6 };
        CheckBox[] boxs = new CheckBox[6];
        for (int i = 0; i < 6; i++) {
            boxs[i] = (CheckBox) findViewById(ids[i]);

            q2 <<= 1;
            if (boxs[i].isChecked()) {
                q2 += 1;
            }
        }

        if (q2 == 0) {
            Toast.makeText(this, "問題２が選択されていません", Toast.LENGTH_SHORT).show();
            return;
        }
        result += Integer.toBinaryString(q2) + "\n";


        //問題3
        RadioGroup gender = (RadioGroup) findViewById(R.id.gender);
        q3 = gender.getCheckedRadioButtonId();
        if (q3 < 0) {
            Toast.makeText(this, "問題３が選択されていません", Toast.LENGTH_SHORT).show();
            return;
        }
        result += "問題３ -> " + q3 + "\n";

        //問題4
        q4 = age;
        if (q4 < 0) {
            Toast.makeText(this, "年齢を選択してください", Toast.LENGTH_SHORT).show();
            return;
        }
        result += "問題４ -> " + q4 + "\n";

        //問題５
        EditText editText = (EditText)findViewById(R.id.address);
        address = editText.getText().toString();
        if (address.equals("")) {
            Toast.makeText(this, "住所が入力されていません", Toast.LENGTH_SHORT).show();
        }

        result += "問題５ -> " + address;

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

        HttpPostHandler postHandler = new HttpPostHandler() {
            @Override
            public void onPostCompleted(String response) {
                String str = "送信成功 : " + response;
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                Log.d("posttest", response);

            }

            @Override
            public void onPostFailed(String response) {
                String str = "送信失敗 : " + response;
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                Log.d("posttest", response);
            }
        };

        HttpPostTask task = new HttpPostTask(POST_URL, postHandler);
        task.addPostParam(PARAM_USER_ID, String.valueOf(prefs.getInt(PARAM_USER_ID, -1)));
        task.addPostParam(PARAM_Q1, String.valueOf(q1));
        task.addPostParam(PARAM_Q2, String.valueOf(q2));
        task.addPostParam(PARAM_Q3, String.valueOf(q3));
        task.addPostParam(PARAM_Q4, String.valueOf(q4));
        task.addPostParam(PARAM_Q5, address);
        task.execute();

        Toast.makeText(this, "アンケートにご協力いただきありがとうございました。", Toast.LENGTH_LONG).show();
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
