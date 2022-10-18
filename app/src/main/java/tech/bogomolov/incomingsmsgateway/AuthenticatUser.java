package tech.bogomolov.incomingsmsgateway;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class AuthenticatUser extends AsyncTask<String, String, String> {
    public String JWT ="";
    String credentials = "identifier=distributor&password=123@Distributor";
    String myUrl = "https://zedsms-ecdabsvihq-uc.a.run.app/auth/local";
    String serverUrl = "https://zedsms-ecdabsvihq-uc.a.run.app/auth/local";

    public AuthenticatUser(String username, String password, String serverUrl) {
        this.credentials = "identifier="+username+"&password="+password;
        this.myUrl = serverUrl+"/auth/local";
        this.serverUrl =serverUrl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // display a progress dialog to show the user what is happening
    }

        @Override
        protected String doInBackground(String... params) {
            // Fetch data from the API in the background.
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                    url = new URL(myUrl);
                    //open a URL coonnection

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setConnectTimeout(15000);
                    urlConnection.connect();


                    DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                    wr.writeBytes(credentials);
                    wr.flush();
                    wr.close();

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder results = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        results.append(line);
                    }
                return results.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

            return result.toString();

        }

        @Override
        protected void onPostExecute(final String s) {
            JWT = s;
            WebHookWorkRequest.AUTH_TOKE = "Bearer "+s;
            // show results
           DistributeSms ds = new DistributeSms(s,this.serverUrl);
           ds.execute();
            new Timer().scheduleAtFixedRate(new TimerTask(){
                @Override
                public void run(){
                    DistributeSms ds = new DistributeSms(s, serverUrl);
                    ds.execute();
                }
            },0,2*60*1000);
        }

        private void taskHandler(String s){
          // start process to read data

        }
}
