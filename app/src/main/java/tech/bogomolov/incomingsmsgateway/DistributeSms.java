package tech.bogomolov.incomingsmsgateway;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DistributeSms extends AsyncTask<String, String, String> {
    private String myAuth ="";
    @Override
    protected String doInBackground(String... strings) {
        String result = "";
        URL url;
        HttpURLConnection urlConnections = null;
        try {
            System.out.println("starting works");
            System.out.println(myAuth);
            String myUrl = "https://zedsms-ecdabsvihq-uc.a.run.app/outboxes/distributor";
//            String credentials="?_start=0";
            url = new URL(myUrl);
            //open a URL coonnection

            urlConnections = (HttpURLConnection) url.openConnection();
            urlConnections.setRequestMethod("GET");
            urlConnections.setRequestProperty("Accept", "application/json");
            urlConnections.setRequestProperty("Authorization", myAuth);
            urlConnections.setReadTimeout(10000);
            urlConnections.setConnectTimeout(15000);
            urlConnections.connect();
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
    public DistributeSms(String auth){
        try{
            JSONObject myJs = new JSONObject(auth);
            myAuth = "Bearer "+ myJs.get("jwt").toString();
        }catch (JSONException js){
            System.out.println(js.getMessage());
        }

    }
    @Override
    protected void onPostExecute(String s) {
        // read through the returned array and send the SMS's
        System.out.println(s);
        try {
            JSONObject sendMessage = new JSONObject(s);
                for (int x=0;x<=sendMessage.getJSONArray ("customers").length();x++){
                    JSONArray customers = sendMessage.getJSONArray("customers");
                    String message = sendMessage.getString("message");
                    JSONObject customerObject = customers.getJSONObject(x);
                    String name = customerObject.getString("name");
                    String phone = customerObject.getString("phone");
                    String id = sendMessage.getString("id");
                    message = "Hi "+name+", "+message;
                    SendSMS sms = new SendSMS(phone,message);
                    sms.sendTheSms();
                    //update sms has been sent
                    UpdateSendSms uss = new UpdateSendSms(id,this.myAuth);
                    uss.execute();

                }
        }catch (JSONException e){
            System.out.println(e.getMessage());
        }
    }
}
