package jp.co.miosys.aiworldview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jp.co.miosys.aiworldview.Utils.OkHttpService;
import jp.co.miosys.aiworldview.Utils.SharePreference;
import jp.co.miosys.aiworldview.connect_api.ApiService;
import jp.co.miosys.aiworldview.connect_api.RetrofitClient;
import jp.co.miosys.aiworldview.static_value.ValueStatic;

public class CompanyCodeActivity extends AppCompatActivity {

    public static String URL;
    private EditText editCompanyCode;
    private Button imbNextFirst;
    ProgressBar progressBar;
    ApiService apiService;
    private SharePreference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companycode);
        initComponent();

    }

    private void initComponent(){
        URL = "https://luck-manager.aimap.jp/";
        editCompanyCode = findViewById(R.id.edit_company_code);
        progressBar = findViewById(R.id.progress);
        imbNextFirst = findViewById(R.id.btnNextFirst);
        RetrofitClient.setRetrofit(null);
        apiService = RetrofitClient.getClient(URL).create(ApiService.class);
        preference = new SharePreference(this);
        editCompanyCode.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

        editCompanyCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if  ((actionId == EditorInfo.IME_ACTION_DONE)) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editCompanyCode.getWindowToken(), 0);
                    findViewById(R.id.btnNextFirst).performClick();
                    return true;

                }
                return false;
            }
        });
    }

    public void onSelectCompany(final View view) {
        view.setClickable(false);
        view.setEnabled(false);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editCompanyCode.getWindowToken(), 0);
        JSONObject json = new JSONObject();
        try {
            json.put("app_token", "6c17d2af3d615c155d90408a8d281fe0");
            json.put("company_code", editCompanyCode.getText().toString());
        }catch (JSONException ex) {

        }
        showProgress(true);
        new OkHttpService(OkHttpService.Method.POST, false, this, ValueStatic.CHOOSE_COMPANY,json,false) {

            @Override
            public void onFailureApi(okhttp3.Call call, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setClickable(true);
                        view.setEnabled(true);
                        Toast.makeText(CompanyCodeActivity.this, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponseApi(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String result = response.body().string();
                preference.saveCompany(editCompanyCode.getText().toString());
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");
                    preference.saveCompanyName(data.getString("name"));
                    ValueStatic.MIO_HOST = data.getString("domain");
                    ValueStatic.URL_LIST_MEMO = ValueStatic.MIO_HOST + "/api/v1/memo/list";
                    ValueStatic.URL_LOGIN = ValueStatic.MIO_HOST + "/api/v1/user/login";
                    ValueStatic.URL_CATEGORY =  ValueStatic.MIO_HOST + "/api/v1/category/list";

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            view.setClickable(true);
                            view.setEnabled(true);
                            onLogin();
                            finish();
                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            view.setClickable(true);
                            view.setEnabled(true);
                            Toast.makeText(CompanyCodeActivity.this, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        };
    }

    private void onLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            progressBar.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }
            });
        } else {
            progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }


//    private class ReadJSON extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... strings) {
//            try {
//
//            } catch ()
//            URL url = new URL(strings[0]);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//        }
//    }

}
