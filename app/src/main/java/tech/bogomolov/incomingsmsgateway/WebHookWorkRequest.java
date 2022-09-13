package tech.bogomolov.incomingsmsgateway;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebHookWorkRequest extends Worker {

    public final static String DATA_URL = "URL";
    public  static String AUTH_TOKE= "URL";
    public final static String DATA_TEXT = "TEXT";
    public static final int MAX_ATTEMPT = 10;

    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_ERROR = "error";
    public static final String RESULT_RETRY = "error_retry";

    public WebHookWorkRequest(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (getRunAttemptCount() > MAX_ATTEMPT) {
            return Result.failure();
        }

        String url = getInputData().getString(DATA_URL);
        String text = getInputData().getString(DATA_TEXT);

        String result = this.makeRequest(url, text);

        if (result.equals(RESULT_RETRY)) {
            return Result.retry();
        }

        if (result.equals(RESULT_ERROR)) {
            return Result.failure();
        }

        return Result.success();
    }

    private String makeRequest(String urlString, String text) {
        String result = RESULT_SUCCESS;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            urlConnection.setRequestProperty("User-agent", "ZEDSMS Forwarder App");
            urlConnection.setRequestProperty("Authorization", AUTH_TOKE);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            writer.write(text);
            writer.flush();
            writer.close();
            out.close();

            new BufferedInputStream(urlConnection.getInputStream());

            char code = Integer.toString(urlConnection.getResponseCode()).charAt(0);
            if (!Character.toString(code).equals("2")) {
                result = RESULT_RETRY;
            }
        } catch (MalformedURLException e) {
            result = RESULT_ERROR;
            Log.e("SmsGateway", "MalformedURLException " + e);
        } catch (IOException e) {
            result = RESULT_RETRY;
            Log.e("SmsGateway", "Exception " + e);
        } catch (Exception e) {
            result = RESULT_ERROR;
            Log.e("SmsGateway", "Exception " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result;
    }
}
