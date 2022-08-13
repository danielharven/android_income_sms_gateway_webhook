package tech.bogomolov.incomingsmsgateway;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateSendSms extends AsyncTask<String, String, String> {
    private String myAuth ="";
    private String id ="";
    public UpdateSendSms(String id, String auth){
        this.id =id;
        this.myAuth =auth;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = "";
        URL url;
        HttpURLConnection urlConnections = null;
        try {
            System.out.println("updating works");
            System.out.println(myAuth);
            String myUrl = "https://zedsms-ecdabsvihq-uc.a.run.app/outboxes/"+id;
            String credentials="status=Completely%20Delivered&sent=true";
            url = new URL(myUrl);
            //open a URL coonnection

            urlConnections = (HttpURLConnection) url.openConnection();
            urlConnections.setRequestMethod("PUT");
            urlConnections.setRequestProperty("Accept", "application/json");
            urlConnections.setRequestProperty("Authorization", myAuth);
            urlConnections.setReadTimeout(10000);
            urlConnections.setConnectTimeout(15000);
            urlConnections.connect();


            DataOutputStream wr = new DataOutputStream(urlConnections.getOutputStream());
            wr.writeBytes(credentials);
            wr.flush();
            wr.close();

            InputStream in = new BufferedInputStream(urlConnections.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder results = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                results.append(line);
            }
            int responseCode=  urlConnections.getResponseCode();
            if(responseCode==200){
                return results.toString();
            }
            return "[]";

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnections != null) {
                urlConnections.disconnect();
            }
        }

        return result.toString();
    }
    @Override
    protected void onPostExecute(String s) {
        // read through the returned array and send the SMS's
        System.out.println(s);
    }
}
