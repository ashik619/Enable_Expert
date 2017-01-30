package in.co.codoc.enableexpert;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ashik619 on 23-11-2016.
 */
//Custom Async task for API Requests
public class JsonAsyncTask extends AsyncTask<String,String,String> {
    OnTaskCompleted taskCompleted;
    URL url;
    String key;
    boolean isAuth;
    public JsonAsyncTask(OnTaskCompleted activityContext, URL url,String key,boolean isAuth){
        this.taskCompleted = activityContext;
        this.url = url;
        this.key = key;
        this.isAuth = isAuth;
    }
    @Override
    protected String doInBackground(String... JsonDATA) {
        String JsonResponse;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            //URL url = new URL("http://ec2-user@52.55.139.178:8080/api/v1/register");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            // is output buffer writter
            urlConnection.setRequestMethod("POST");
            if(isAuth) {
                urlConnection.setRequestProperty("Authorization", key);
            }
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            //set headers and method
            Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
            writer.write(JsonDATA[0]);
            // json data
            writer.close();
            InputStream inputStream = urlConnection.getInputStream();
            //input stream
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ((inputLine = reader.readLine()) != null)
                buffer.append(inputLine + "\n");
            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            JsonResponse = buffer.toString();
            System.out.println(JsonResponse);
            try {
                //Returnig the result to post execute
                return JsonResponse;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;



        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    System.out.println("Error closing stream"+ e);
                }
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(String s) {
        taskCompleted.onTaskCompleted(s);

    }
}
