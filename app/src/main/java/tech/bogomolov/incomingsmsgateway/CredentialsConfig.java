package tech.bogomolov.incomingsmsgateway;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Map;

public class CredentialsConfig {
    final private Context context;

    private String sender;
    private String url;
    private String password;

    public CredentialsConfig(Context context) {
        this.context = context;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void save() {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString("url",this.url);
        editor.commit();
        editor.putString("username",this.sender);
        editor.commit();
        editor.putString("password", this.password);
        editor.commit();

    }

    public static ArrayList<CredentialsConfig> getAll(Context context) {
        SharedPreferences sharedPref = getPreference(context);
        Map<String, ?> sharedPrefs = sharedPref.getAll();

        ArrayList<CredentialsConfig> configs = new ArrayList<CredentialsConfig>();

        for (Map.Entry<String, ?> entry : sharedPrefs.entrySet()) {
            CredentialsConfig config = new CredentialsConfig(context);
            config.setSender(entry.getKey());
            config.setUrl((String) entry.getValue());
            config.setPassword((String) entry.getValue());
            configs.add(config);
        }

        return configs;
    }

    public void remove() {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(this.getSender());
        editor.commit();
    }

    private static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.key_server_preference),
                Context.MODE_PRIVATE
        );
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences sharedPref = getPreference(context);
        return sharedPref.edit();
    }
}
