package jp.co.miosys.aiworldview;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jp.co.miosys.aiworldview.Utils.OkHttpService;
import jp.co.miosys.aiworldview.Utils.SharePreference;
import jp.co.miosys.aiworldview.static_value.ValueStatic;

public class LoginActivity extends AppCompatActivity {

    private Context mContext;
    private EditText edtUser, edtPass;
    private String mUser, mPass;
    private SharePreference preference = new SharePreference(this);
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;
        initView();
    }

    private void initView() {
        TextView txtCompanyName = (TextView) findViewById(R.id.txt_company_name);
        txtCompanyName.setText(new SharePreference(this).getCompanyName());
        edtUser = (EditText) findViewById(R.id.edt_user);
        edtPass = (EditText) findViewById(R.id.edt_pass);

        mUser = preference.getLogin()[0];
        mPass = preference.getLogin()[1];

        Log.d("login",mUser);
        edtUser.setText(mUser);
        edtPass.setText(mPass);

        TextView tvVerion = (TextView) findViewById(R.id.app_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;
            tvVerion.setText(String.format("Ver %s.%s (2020)", version, String.valueOf(verCode)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButtonTapped();
            }
        });
    }

    private String getUUID() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void loginButtonTapped() {
        JSONObject json = new JSONObject();
        try {
            json.put("username", edtUser.getText().toString());
            json.put("password", edtPass.getText().toString());
            json.put("kind", 5);
            json.put("uuid", getUUID());
            json.put("app_token", "6c17d2af3d615c155d90408a8d281fe0");
        } catch (JSONException ex) {

        }

        if (mProgressDialog == null ) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Login ...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        new OkHttpService(OkHttpService.Method.POST, false, this, ValueStatic.URL_LOGIN,json,false) {

            @Override
            public void onFailureApi(okhttp3.Call call, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponseApi(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.getString("status");
                    if (status.equals("success")){
                        preference.saveLogIn(edtUser.getText().toString(), edtPass.getText().toString());
                        goLocation();
                    } else {
                        Toast.makeText(mContext, "このユーザーは未登録です。", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            Toast.makeText(mContext, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }

            }
        };
    }

//    private boolean textEmpty() {
//        if (edtId.getText().toString().length() == 0) {
//            Toast.makeText(this, "ID not empty", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        if (edtId.getText().toString().length() == 0) {
//            Toast.makeText(this, "Password not empty", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        return true;
//    }

//    private void connectServer() {
//        Call<DataResponseLogin> call;
//        call = apiServices.apiLogin(
//                ValueStatic.APP_TOKEN,
//                edtId.getText().toString(),
//                edtPassword.getText().toString(),
//                getUUID(),
//                "5");
//        call.enqueue(new Callback<DataResponseLogin>() {
//            @Override
//            public void onResponse(Call<DataResponseLogin> call, Response<DataResponseLogin> response) {
//                if (response.isSuccessful()) {
//                    if (response.body() != null) {
//                        if (response.body().getStatus().equals("success")) {
//                            ValueStatic.TOKEN = response.body().getToken();
//                            ValueStatic.userName = edtId.getText().toString();
//                            editor.putString(VALUE_USERNAME, edtId.getText().toString());
//                            editor.putString(VALUE_PASS, edtPassword.getText().toString());
//                            editor.apply();
//                            goLocation();
//                        } else {
//                            Toast.makeText(getBaseContext(), "Connect Fail", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                } else {
//                    Toast.makeText(getBaseContext(), "Connect Fail", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<DataResponseLogin> call, Throwable t) {
//                Toast.makeText(getBaseContext(), "Connect Fail", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void goLocation() {
        Intent intent = new Intent(this, Camera3DActivity.class);
        startActivity(intent);
    }
}
