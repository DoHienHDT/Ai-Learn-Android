package jp.co.miosys.aiworldview;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jp.co.miosys.aiworldview.Utils.OkHttpService;
import jp.co.miosys.aiworldview.Utils.SharePreference;
import jp.co.miosys.aiworldview.static_value.ValueStatic;

public class SplashDHActivity extends AppCompatActivity {

    private Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_dh);

        mContext = this;
        SharePreference preference = new SharePreference(this);
        if (!preference.getCompany().equals("")) {
            onSelectCompany(preference);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new SharePreference(mContext).saveLogIn("","");
                    Intent intent = new Intent(SplashDHActivity.this, CompanyCodeActivity.class);
                    startActivity(intent);
                }
            }, 1000);
        }
    }

    public  void onSelectCompany(final SharePreference preference) {
        JSONObject json = new JSONObject();
        try {
            json.put("app_token", "6c17d2af3d615c155d90408a8d281fe0");
            json.put("company_code", preference.getCompany());
        }catch (JSONException ex) {

        }

        new OkHttpService(OkHttpService.Method.POST, false, this, ValueStatic.CHOOSE_COMPANY,json,false) {

            @Override
            public void onFailureApi(okhttp3.Call call, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponseApi(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");

                    ValueStatic.MIO_HOST = data.getString("domain");
                    ValueStatic.URL_LIST_MEMO = ValueStatic.MIO_HOST + "/api/v1/memo/list";
                    ValueStatic.URL_LOGIN = ValueStatic.MIO_HOST + "/api/v1/user/login";
                    ValueStatic.URL_CATEGORY =  ValueStatic.MIO_HOST + "/api/v1/category/list";

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(SplashDHActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        };
    }
}
