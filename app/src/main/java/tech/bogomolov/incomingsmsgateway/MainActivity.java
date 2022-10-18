package tech.bogomolov.incomingsmsgateway;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private ListAdapter listAdapter;
    private CredListAdapter credentialsListAdapter;

    public void onDeleteClick(View view) {
        final int position = (int) view.getTag(R.id.delete_button);
        final ForwardingConfig config = listAdapter.getItem(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_record);
        String asterisk = context.getString(R.string.asterisk);
        String any = context.getString(R.string.any);
        String message = context.getString(R.string.confirm_delete);
        message = String.format(message, (config.getSender().equals(asterisk) ? any : config.getSender()));
        builder.setMessage(message);

        builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                listAdapter.remove(config);
                config.remove();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.show();
    }
    public void onDeleteCredentialsClick(View view) {
        final int position = (int) view.getTag(R.id.delete_button);
        final CredentialsConfig config = credentialsListAdapter.getItem(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_record);
        String asterisk = context.getString(R.string.asterisk);
        String any = context.getString(R.string.any);
        String message = context.getString(R.string.confirm_delete);
        message = String.format(message, (config.getSender().equals(asterisk) ? any : config.getSender()));
        builder.setMessage(message);

        builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                credentialsListAdapter.remove(config);
                config.remove();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        context = this;
        ListView listview = findViewById(R.id.listView);
        ListView credentialsListview = findViewById(R.id.settingsView);

        ArrayList<ForwardingConfig> configs = ForwardingConfig.getAll(context);
        ArrayList<CredentialsConfig> credConfig = CredentialsConfig.getAll(context);
        listAdapter = new ListAdapter(configs, context);
        credentialsListAdapter = new CredListAdapter(credConfig, context);

        listview.setAdapter(listAdapter);
        credentialsListview.setAdapter(credentialsListAdapter);

        FloatingActionButton fab = findViewById(R.id.btn_add);
        FloatingActionButton settings = findViewById(R.id.btn_settings);
        fab.setOnClickListener(this.showAddDialog());
        settings.setOnClickListener(this.showCredentials());

        // get the sharedpreferences then start the service
        SharedPreferences sharedpreferences = getSharedPreferences("server", Context.MODE_PRIVATE);
        final String serverUrl = sharedpreferences.getString("url", null);
        final String password = sharedpreferences.getString("password", null);
        final String username = sharedpreferences.getString("username", null);
        Thread serviceThread = new Thread(){
            public void run(){
//                RunningService rs = new RunningService(serverUrl,password,username);
                Intent runningService = new Intent(getBaseContext(), RunningService.class);
                runningService.putExtra("serverUrl",serverUrl);
                runningService.putExtra("password",password);
                runningService.putExtra("username",username);

                startService(runningService);
            }
        };
        serviceThread.start();
    }

    private View.OnClickListener showAddDialog() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_add, null);
                final EditText senderInput = view.findViewById(R.id.input_phone);
                final EditText urlInput = view.findViewById(R.id.input_url);

                builder.setView(view);
                builder.setPositiveButton(R.string.btn_add, null);
                builder.setNegativeButton(R.string.btn_cancel, null);
                final AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String sender = senderInput.getText().toString();
                        if (TextUtils.isEmpty(sender)) {
                            senderInput.setError(getString(R.string.error_empty_sender));
                            return;
                        }

                        String url = urlInput.getText().toString();
                        if (TextUtils.isEmpty(url)) {
                            urlInput.setError(getString(R.string.error_empty_url));
                            return;
                        }

                        try {
                            new URL(url);
                        } catch (MalformedURLException e) {
                            urlInput.setError(getString(R.string.error_wrong_url));
                            return;
                        }

                        ForwardingConfig config = new ForwardingConfig(context);
                        config.setSender(sender);
                        config.setUrl(url);
                        config.save();

                        listAdapter.add(config);

                        dialog.dismiss();
                    }
                });
            }
        };
    }
    private View.OnClickListener showCredentials() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_settings, null);
                final EditText urlInput = view.findViewById(R.id.input_phone);
                final EditText usernameInput = view.findViewById(R.id.input_username);
                final EditText passwordInput = view.findViewById(R.id.input_password);

                builder.setView(view);
                builder.setPositiveButton(R.string.btn_add, null);
                builder.setNegativeButton(R.string.btn_cancel, null);
                final AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String sender = usernameInput.getText().toString();
                        if (TextUtils.isEmpty(sender)) {
                            usernameInput.setError(getString(R.string.error_empty_sender));
                            return;
                        }

                        String url = urlInput.getText().toString();
                        if (TextUtils.isEmpty(url)) {
                            urlInput.setError(getString(R.string.error_empty_url));
                            return;
                        }

                        try {
                            new URL(url);
                        } catch (MalformedURLException e) {
                            urlInput.setError(getString(R.string.error_wrong_url));
                            return;
                        }
                        String password = passwordInput.getText().toString();
                        if (TextUtils.isEmpty(url)) {
                            passwordInput.setError(getString(R.string.error_empty_sender));
                            return;
                        }

                        CredentialsConfig config = new CredentialsConfig(context);
                        config.setSender(sender);
                        config.setUrl(url);
                        config.setPassword(password);
                        config.save();

                        credentialsListAdapter.add(config);

                        dialog.dismiss();
                    }
                });
            }
        };
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);
        }
    }
}
