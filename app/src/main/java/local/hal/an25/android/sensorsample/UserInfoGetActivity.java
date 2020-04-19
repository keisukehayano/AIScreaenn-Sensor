package local.hal.an25.android.sensorsample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoGetActivity extends AppCompatActivity {

    private DatabaseHelper _helper;

    /**
     * リストビューに表示させるリストデータ
     */
    private List<Map<String, Object>> _list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_get);

        _helper = new DatabaseHelper(this.getApplicationContext());

        SQLiteDatabase db = _helper.getWritableDatabase();

        final String ACCESS_URL = DataAccess.findByPK(db,2);


        GetAccess access = new GetAccess();
        access.execute(ACCESS_URL);


        ListView userList = findViewById(R.id.lv_userInfo);

        userList.setOnItemClickListener(new ListItemClickListener());

    }


    private class GetAccess extends AsyncTask<String, String, String> {

        private static final String DEBUG_TAG = "GetAccess";

        private boolean _success = false;

        @Override
        public String doInBackground(String... params) {
            String urlStr = params[0];


            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";

            try {
                URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                os.flush();
                os.close();
                int status = con.getResponseCode();

                System.out.println("ステータスコード:" + status);
                //ステータースコード２００以外は失敗
                if (status != 200) {
                    throw new IOException("ステータースコード:" + status);
                }

                is = con.getInputStream();

                result = is2String(is);
                _success = true;
            } catch (SocketTimeoutException ex) {
                Log.e(DEBUG_TAG, "タイムアウト", ex);
            } catch (MalformedURLException ex) {
                Log.e(DEBUG_TAG, "URL変換失敗", ex);
            } catch (IOException ex) {
                Log.e(DEBUG_TAG, "通信失敗", ex);
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    Log.e(DEBUG_TAG, "InputStream開放失敗", ex);
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
            if (_success) {

                try {
                    JSONObject rootJSON = new JSONObject(result);
                    JSONArray jsonList = rootJSON.getJSONArray("list");
                    int listLength = jsonList.length();





                        _list = new ArrayList<>();

                        for (int i = 0; i < listLength; i++) {
                            Map<String, Object> map = new HashMap<>();

                            JSONObject jsonObject = jsonList.getJSONObject(i);

                            String userId = jsonObject.getString("userid");
                            String name = jsonObject.getString("name");
                            String gender = jsonObject.getString("gender");

                            map.put("userid",userId);
                            map.put("name",name);
                            map.put("gender",gender);


                            _list.add(map);

                        }



                } catch (JSONException ex) {
                    Log.e(DEBUG_TAG, "JSON解析失敗", ex);
                }

                String[] from = {"userid","name","gender"};
                int[] to = {R.id.tvId,R.id.tvName,R.id.tvGender};

                SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),_list,R.layout.row,from,to);
                ListView userList = findViewById(R.id.lv_userInfo);

                userList.setAdapter(adapter);

            }
        }
    }

    private String is2String(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] b = new char[1024];
        int line;
        while (0 <= (line = reader.read(b))) {
            sb.append(b, 0, line);
        }
        return sb.toString();
    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Map<String,Object> item = _list.get(position);

            String strId = item.get("userid").toString();
            int intUserId = Integer.parseInt(strId);
            String strName = item.get("name").toString();
            String strGender = item.get("gender").toString();

            System.out.println("strId:" + intUserId);
            System.out.println("name:" + strName);
            System.out.println("gender:" + strGender);

            SQLiteDatabase db = _helper.getWritableDatabase();

            int userId = DataAccess.findByUserPk(db,1);
            System.out.println("ユーザID:" + userId);


           if(userId == 1) {
                System.out.println("アップデート");
                DataAccess.userUpdate(db,1,intUserId,strName,strGender);
           } else {
                System.out.println("インサート");
                DataAccess.userInsert(db,1,intUserId,strName,strGender);
            }



        }
    }




}
