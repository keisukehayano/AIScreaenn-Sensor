package local.hal.an25.android.sensorsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;




/**
 * Activity_counter_prototype
 *
 * 加速度センサーによって三軸の動きの強さを感知し一定値以上の値を感知すると、ポイントとして加算される。
 * 設定したタイマー間隔でポイントを記録する。
 *
 * @author keisuke hayano
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {


    //グローバル変数置き場/////////////////////////////////////////////////////////////

    private DatabaseHelper _helper;

    LineChart mChart;
    String[] names = new String[]{"x-value", "y-value", "z-value"};
    int[] colors = new int[]{R.color.blue, R.color.green, R.color.red};

    SensorManager sensorManager;
    Sensor sensor;
    TextView sumTextView;
    TextView stepTextView;

    TextView timeCount1;


    int pointCount = 0;
    int totalPoint = 0;

    //フィルタリング数:ここの変数をいじるとアクティビティのポイントの調整可能
    double sensorFilter = 2;

    //モード変更変数
    int ACTIVITY_MODE = 1;

    final float alpha = 0.8f;

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];

    private int count = 0;
    //タイマー設定
    private int timer = 4000;

    /////////////////////////////////////////////////////////////////////////////////

    //handlerでタイマー設定
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {



        @Override
        public void run() {



            //12回分のポイント取得及び送信
            if (count <= 14 && count != 0) {


                SQLiteDatabase db = _helper.getWritableDatabase();
                final String ACCESS_URL = DataAccess.findByPK(db,1);
                UserInfo userInfo = DataAccess.getUserInfo(db,1);


                setText(timeCount1,pointCount);


                totalPoint = totalPoint + pointCount;

                if(userInfo != null) {
                    long longUserId = userInfo.get_userId();
                    String strUserId = String.valueOf(longUserId);
                    String strPoint = String.valueOf(pointCount);
                    String strCount = String.valueOf(count);




                        System.out.println("カウンター:" + count);
                        setText(timeCount1,pointCount);
                        PostAccess access = new PostAccess();
                        access.execute(ACCESS_URL, strUserId, strCount, strPoint);


                }

                pointCount = 0;
            }

            count++;


            handler.postDelayed(this,timer);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _helper = new DatabaseHelper(getApplicationContext());

        //ラジオボタン取得
        RadioGroup group = findViewById(R.id.rgroup_1);
        group.check(R.id.rButton1);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                System.out.println("チェックID:" + checkedId);
                if(radioButton.isChecked() == true){
                    String text =radioButton.getText().toString();
                    System.out.println("メッセージ:" + text);

                    if (text.equals("小盛")) {
                        System.out.println("強度:小盛が選ばれました。");
                        //センサーの値を入れ替える部分
                        sensorFilter = 2;
                        ACTIVITY_MODE = 1;
                        Toast.makeText(getApplicationContext(),"強度:小盛が選ばれました。",Toast.LENGTH_SHORT).show();
                    }

                    if (text.equals("中盛")) {
                        System.out.println("強度:中盛が選ばれました。");
                        //センサーの値を入れ替える部分
                        sensorFilter = 6;
                        ACTIVITY_MODE = 2;
                        Toast.makeText(getApplicationContext(),"強度:中盛が選ばれました。",Toast.LENGTH_SHORT).show();
                    }

                    if (text.equals("大盛")) {
                        System.out.println("強度:大盛が選ばれました。");
                        //センサーの値を入れ替える部分
                        sensorFilter = 4;
                        ACTIVITY_MODE = 5;
                        Toast.makeText(getApplicationContext(),"強度:大盛が選ばれました。",Toast.LENGTH_SHORT).show();
                    }

                    if (text.equals("特盛")) {
                        System.out.println("強度:特盛が選ばれました。");
                        //センサーの値を入れ替える部分
                        sensorFilter = 1;
                        ACTIVITY_MODE = 4;
                        Toast.makeText(getApplicationContext(),"強度:特盛が選ばれました。",Toast.LENGTH_SHORT).show();
                    }

                    if (text.equals("熱盛")) {
                        System.out.println("強度:敦盛が選ばれました。");
                        //センサーの値を入れ替える部分
                        sensorFilter = 1;
                        ACTIVITY_MODE = 5;
                        Toast.makeText(getApplicationContext(),"強度:熱盛が選ばれました。",Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });



        SQLiteDatabase db = _helper.getWritableDatabase();
        String url = DataAccess.findByPK(db,1);
        EditText etUrl = findViewById(R.id.etUrl);
        etUrl.setText(url);

        String getUrl = DataAccess.findByPK(db,2);
        EditText etGetUrl = findViewById(R.id.etgeturl);
        etGetUrl.setText(getUrl);

        UserInfo userInfo = DataAccess.getUserInfo(db,1);

        if(userInfo != null) {
            long intUserId = userInfo.get_userId();
            String strUserName = userInfo.get_name();
            String strGender = userInfo.get_gender();

            TextView tvUserInfo = findViewById(R.id.tvUser);
            tvUserInfo.setText("id:" + intUserId + " " + strUserName + " " + strGender);
        }




        sumTextView = findViewById(R.id.sum_value);
        stepTextView = findViewById(R.id.counter);

        timeCount1 = findViewById(R.id.timeCount1);


        sumTextView.setText("3軸加速度ベクトルの長さ:");
        stepTextView.setText("0ポイント");

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        mChart = findViewById(R.id.lineChart);

        mChart.setDescription(""); // 表のタイトルを空にする
        mChart.setData(new LineData()); // 空のLineData型インスタンスを追加
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_actionbar_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int itemId = item.getItemId();

        if(itemId == R.id.btnAdd){
            Intent intent = new Intent(getApplicationContext(),UserInfoGetActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //textViewに値をセットするメソッド...ただそれだけ。
    public void setText(TextView view, int point) {
        view.setText("ポイント:" + point);

    }

    public void onSensorChanged(SensorEvent event) {

        //重力相殺のための計算式
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        //重力相殺
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        //xTextView.setText("X軸の加速度:" + linear_acceleration[0]);
        //yTextView.setText("Y軸の加速度:" + linear_acceleration[1]);
        //zTextView.setText("Z軸の加速度:" + linear_acceleration[2]);

        //３軸のベクトルを合算
        float sum = (float) Math.sqrt(Math.pow(linear_acceleration[0],2) + Math.pow(linear_acceleration[1],2) + Math.pow(linear_acceleration[2],2));
        sumTextView.setText("3軸加速度ベクトルの長さ:" + sum);


        //アクティビティモード小盛
        if (ACTIVITY_MODE == 1) {
            System.out.println("今は小盛アクティビティモード");
            //一定の値以下ならcount++
            if (sum < sensorFilter) {
                pointCount++;
            }
        }

        //アクティビティモード中盛
        if (ACTIVITY_MODE == 2) {
            System.out.println("今は中盛アクティビティモード");
            //一定の値以上ならcount++
            if (sum < sensorFilter) {
                pointCount++;
            }
        }

        //アクティビティモード大盛
        if (ACTIVITY_MODE == 3) {
            System.out.println("今は大盛アクティビティモード");
            //一定の値以上ならcount++
            if (sum > sensorFilter) {
                pointCount++;
            }
        }

        //アクティビティモード特盛
        if (ACTIVITY_MODE == 4) {
            System.out.println("今は特盛アクティビティモード");
            if (sum > sensorFilter) {
                pointCount++;
            }
        }

        //アクティビティモード熱盛
        if (ACTIVITY_MODE == 5) {
            System.out.println("今は熱盛アクティビティモード");
            if (sum > sensorFilter) {
                pointCount++;
            }
        }

        //画面にポイントを描画するよ
        stepTextView.setText("合計" + totalPoint + "ポイント");


        //外部ライブラリチャート表示
        LineData data = mChart.getLineData();

        if (data != null) {
            for (int i = 0; i < 3; i++) {
                ILineDataSet set = data.getDataSetByIndex(i);
                if (set == null) {
                    set = createSet(names[i], colors[i]); // ILineDataSetの初期化は別メソッドにまとめました
                    data.addDataSet(set);
                }
                data.addEntry(new Entry(set.getEntryCount(), linear_acceleration[i]), i); // 実際にデータを追加する
                data.notifyDataChanged();
            }
            mChart.notifyDataSetChanged(); // 表示の更新のために変更を通知する
            mChart.setVisibleXRangeMaximum(50); // 表示の幅を決定する
            mChart.moveViewToX(data.getEntryCount()); // 最新のデータまで表示を移動させる

        }

    }



    @Override
    public void onAccuracyChanged(Sensor sensor,int accuracy){
        //Accuracy変更後にここが走る
    }

    @Override
    protected void onResume(){
        super.onResume();
        //ボタンリスナで動かすのであえてコメントアウト
        //sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    protected void onPause(){
        super.onPause();
        //リスナを解除するよ
        sensorManager.unregisterListener(this);
    }

    public void clickStartButton(View view){
        sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);

        handler.post(runnable);

    }

    public void clickResetButton(View view){
        this.onPause();
        stepTextView.setText("0ポイント");
        pointCount = 0;
        handler.removeCallbacks(runnable);
        count = 0;
        totalPoint = 0;
        timeCount1.setText("");


    }

    public void clickStopButton(View view){
        this.onPause();
        //sensorManager.unregisterListener(this);

        handler.removeCallbacks(runnable);
    }

    private class PostAccess extends AsyncTask<String,String,String> {

        private static final String DEBUG_TAG = "PostAccess";





        private boolean _success = false;

        @Override
        public String doInBackground(String... params) {
            String urlStr = params[0];
            String userId = params[1];
            String itemNum = params[2];
            String point = params[3];

            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";
            String json = "";

            try {
                JSONObject jsonPoint = new JSONObject();
                jsonPoint.put("userid",userId);
                jsonPoint.put("itemnum",itemNum);
                jsonPoint.put("point",point);
                json = jsonPoint.toString();
                System.out.println("json文字列生成" + json);


            }
            catch (JSONException ex) {

            }


            try{

                URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setInstanceFollowRedirects(false);
                con.setRequestProperty("Accept-Language", "jp");
                con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                os.write(json.getBytes());
                os.flush();
                os.close();


                int status = con.getResponseCode();

                System.out.println("システムコード:" + status);


                //ステータースコード２００以外は失敗
                if(status != 200){
                    throw new IOException("ステータースコード:" + status);
                }

                is = con.getInputStream();


               result = is2String(is);
                _success = true;

            }
            catch (SocketTimeoutException ex){
                Log.e(DEBUG_TAG,"タイムアウト",ex);
            }
            catch (MalformedURLException ex) {
                Log.e(DEBUG_TAG,"URL変換失敗",ex);
            }
            catch (IOException ex){
                Log.e(DEBUG_TAG,"通信失敗",ex);
            }
            finally {
                if (con != null) {
                    con.disconnect();
                }
                try {
                    if(is != null) {
                        is.close();
                    }
                }
                catch (IOException ex) {
                    Log.e(DEBUG_TAG,"InputStream開放失敗",ex);
                }
            }


            return result;
        }

        @Override
        public void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);


        }

        @Override
        public void onPostExecute(String result) {
            if(_success){
                String userId = "";
                String itemNum = "";
                String point = "";

                try{
                    JSONObject rootJSON = new JSONObject(result);
                    userId = rootJSON.getString("userid");
                    itemNum = rootJSON.getString("itemnum");
                    point = rootJSON.getString("point");
                }
                catch (JSONException ex) {
                    Log.e(DEBUG_TAG,"JSON解析失敗",ex);
                }

                TextView jsonView = findViewById(R.id.tvResJson);
                jsonView.setText("ユーザID:" + userId + " 項番:" + itemNum + " ポイント:" + point);

            }

        }
    }

    private LineDataSet createSet(String label, int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setLineWidth(2.5f); // 線の幅を指定
        set.setColor(color); // 線の色を指定
        set.setDrawCircles(false); // ポイントごとの円を表示しない
        set.setDrawValues(false); // 値を表示しない

        return set;
    }


    public void onRegistGetUrlButtonClick(View view) {
        SQLiteDatabase db = _helper.getWritableDatabase();

        int intId = DataAccess.findByPKPK(db,2);

        if (intId == 0) {
            EditText etInputGetUrl = findViewById(R.id.etgeturl);
            String inputGetUrl = etInputGetUrl.getText().toString();
            DataAccess.insert(db,2,inputGetUrl);
            Toast.makeText(getApplicationContext(),R.string.tv_tostInsert,Toast.LENGTH_SHORT).show();

        } else {
            EditText etInputGetUrl = findViewById(R.id.etgeturl);
            String inputGetUrl = etInputGetUrl.getText().toString();
            DataAccess.update(db,2,inputGetUrl);
            Toast.makeText(getApplicationContext(),R.string.tv_tostUpdate,Toast.LENGTH_SHORT).show();
        }


    }




    public void onRegistButtonClick(View view) {
        SQLiteDatabase db = _helper.getWritableDatabase();

         int intId = DataAccess.findByPKPK(db,1);


        if (intId == 0) {
            EditText etInputUrl = findViewById(R.id.etUrl);
            String inputUrl = etInputUrl.getText().toString();
            DataAccess.insert(db,1,inputUrl);
            Toast.makeText(getApplicationContext(),R.string.tv_tostInsert,Toast.LENGTH_SHORT).show();


        } else {
            EditText etInputUrl = findViewById(R.id.etUrl);
            String inputUrl = etInputUrl.getText().toString();
            DataAccess.update(db,1,inputUrl);
            Toast.makeText(getApplicationContext(),R.string.tv_tostUpdate,Toast.LENGTH_SHORT).show();
        }



    }

    private String is2String(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] b = new char[1024];
        int line;
        while(0 <= (line = reader.read(b))){
            sb.append(b,0,line);
        }
        return sb.toString();
    }

}