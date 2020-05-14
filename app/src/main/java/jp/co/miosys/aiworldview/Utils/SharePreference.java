package jp.co.miosys.aiworldview.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class SharePreference {

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private Context activity;
    private String USER = "USER";
    private String PASS = "PASS";
    private String COMPANY = "company";
    private String COMPANY_NAME = "company name";

    public SharePreference(Context activity) { this.activity = activity; }

    public void saveLogIn(String user, String pass) {
        sp = PreferenceManager.getDefaultSharedPreferences(activity);
        editor = sp.edit();
        editor.putString(USER, user);
        editor.putString(PASS, pass);
        editor.commit();
    }

    public String[] getLogin() {
        sp = PreferenceManager.getDefaultSharedPreferences(activity);
        String user = sp.getString(USER, "");
        String pass = sp.getString(PASS, "");
        String[] result = {user, pass};
        return result;
    }

    public String getCompany() {
        SharedPreferences sp =  PreferenceManager.getDefaultSharedPreferences(activity);
        String address =  sp.getString(COMPANY, "");
        return address;
    }

    public void saveCompany(String id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(COMPANY, id);
        editor.apply();
    }

    public void saveCompanyName(String id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(COMPANY_NAME, id);
        editor.apply();
    }

    public String getCompanyName() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        String address =  sp.getString(COMPANY_NAME, "");
        return address;
    }

}
