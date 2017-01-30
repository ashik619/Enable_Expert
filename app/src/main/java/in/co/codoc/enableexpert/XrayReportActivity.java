package in.co.codoc.enableexpert;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class XrayReportActivity extends AppCompatActivity implements OnTaskCompleted {
    String report_id;
    String user_id;
    String key;
    LinearLayout diagnosisLayout;
    Button submitButton;
    AutoCompleteTextView autoReply;
    String image_url;
    boolean isAccepeted;
    String expertReply;
    ImageView imageView;
    ProgressDialog pd;
    String note;
    Realm myRealm;
    IconTextView diagnosis;
    IconTextView clinicalInfo;
    String temp;
    String reply_from_expert;
    String to_charge = "yes";
    IconTextView heading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xray_report);
        user_id = (PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", null));
        key = (PreferenceManager.getDefaultSharedPreferences(this).getString("key", null));
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("referred.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        myRealm = Realm.getInstance(config);
        autoReply = (AutoCompleteTextView) findViewById(R.id.reply);
        diagnosis = (IconTextView) findViewById(R.id.response);
        clinicalInfo = (IconTextView) findViewById(R.id.clinical_info);
        heading = (IconTextView) findViewById(R.id.heading);
        diagnosisLayout = (LinearLayout) findViewById(R.id.diagnosis_layout);
        submitButton = (Button) findViewById(R.id.update_button);
        imageView =  (ImageView) findViewById(R.id.xrayImageView);
    }
    public void onResume(){
        super.onResume();
        diagnosisLayout.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        autoReply.setVisibility(View.INVISIBLE);
        getIntentData();
        getReportDetailsApicall();
    }

    void getReportDetailsApicall(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("user_id",user_id);
            obj.put("report_id", report_id);
            sendGetPendingReportsJson(obj);
        }catch (JSONException e){

        }
    }
    void sendGetPendingReportsJson(JSONObject json) {
        try {
            URL url = new URL(Constants.BASE_URL+"/reports/getReportOnId");
            JsonAsyncTask task = new JsonAsyncTask(this,url,key,true);
            pd = ProgressDialog.show(XrayReportActivity.this, "", "Loading");
            task.execute(json.toString());
        }catch (Exception e){
            System.out.println("cant send");
        }
    }
    void getDiagnosisRepliesApi(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("user_id",user_id);
            try {
                URL url = new URL(Constants.BASE_URL+"/reports/getResults");
                JsonAsyncTask task = new JsonAsyncTask(this,url,key,true);
                pd = ProgressDialog.show(XrayReportActivity.this, "", "loading");
                task.execute(obj.toString());
            }catch (Exception e){
            }

        }catch (JSONException e){

        }
    }
    void getIntentData(){
        Intent i = getIntent();
        report_id = i.getStringExtra("report_id");
    }
    @Override
    public void onTaskCompleted(String response) {
        // Just showing the response in a Toast message
        System.out.println("task completed");
        pd.dismiss();
        if (response != null) {
            try {
                JSONObject resultJson = new JSONObject(response);
                System.out.println("response" + response);
                boolean success = resultJson.getBoolean("success");
                if (success) {
                    if (resultJson.has("result")) {
                        JSONArray resultarray = resultJson.getJSONArray("result");
                        JSONObject result = resultarray.getJSONObject(0);
                        getDiagnosisRepliesApi();
                        note = result.getString("note_to_expert");
                        image_url = result.getString("image_url");
                        isAccepeted = result.getBoolean("is_accepted");
                        expertReply = result.getString("result");
                        setData();
                    }else if(resultJson.has("results")){
                        saveReplies(resultJson.getJSONArray("results"));
                    } else {
                        Toast.makeText(getApplicationContext(),"You have sent Reply", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), DetailsActivity.class);
                        startActivity(i);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            final Dialog dialog = new Dialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.no_internet_dialog);
            dialog.show();
            Button databutton = (Button) dialog.findViewById(R.id.data);
            Button wifibutton = (Button) dialog.findViewById(R.id.wifi);
            databutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity"));
                    dialog.dismiss();
                    startActivity(intent);
                }
            });
            wifibutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                    dialog.dismiss();
                    startActivity(intent);

                }
            });
        }
    }
    void setData(){
        Picasso.with(this)
                .load(image_url)
                .placeholder(R.drawable.loading)
                .into(imageView);
        clinicalInfo.setText(note);
        heading.setText("xray#"+report_id);
        if(isAccepeted) {
            diagnosisLayout.setVisibility(View.VISIBLE);
            diagnosis.setText(expertReply);
        }else {
            submitButton.setVisibility(View.VISIBLE);
            autoReply.setVisibility(View.VISIBLE);
        }
    }
    void saveReplies(final JSONArray result){
        //delete all reports from db to override
        try {
            System.out.println("saving replies to local db");
            myRealm.beginTransaction();
            RealmResults<Diagnosis> results = myRealm.where(Diagnosis.class).findAll();
            results.deleteAllFromRealm();
            myRealm.commitTransaction();
            for (int i = result.length() - 1; i >= 0; i--) {
                temp = result.getString(i);
                myRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Diagnosis diagnosis = realm.createObject(Diagnosis.class);
                        try {
                            diagnosis.setDiagnosisText(temp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            populateAutoComplete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void populateAutoComplete(){
        RealmResults<Diagnosis> diagnosises = myRealm.where(Diagnosis.class).findAll();
        List<String> diagnosisList = new ArrayList<String>();
        for (int i = 0;i<diagnosises.size();i++){
            System.out.println("saving " + diagnosises.get(i).getDiagnosisText() );
            diagnosisList.add(diagnosises.get(i).getDiagnosisText());
        }
        String diagnosisArray[] = diagnosisList.toArray(new String[diagnosisList.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, diagnosisArray);
        autoReply.setAdapter(adapter);
        autoReply.setThreshold(1);
    }
    public void back(View v) {
        finish();

    }
    public void submit(View v){
        reply_from_expert = autoReply.getText().toString();
        System.out.println("fucked"+reply_from_expert);
        if (reply_from_expert.matches("")){
            Toast.makeText(getApplicationContext(), "Please type a reply", Toast.LENGTH_LONG).show();
        }else {

            callBackReportApicall();
        }
    }
    void callBackReportApicall(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("user_id",user_id);
            obj.put("report_id", report_id);
            obj.put("result",reply_from_expert);
            obj.put("to_charge",to_charge);
            try {
                URL url = new URL(Constants.BASE_URL+"/reports/callbackReport");
                JsonAsyncTask task = new JsonAsyncTask(this,url,key,true);
                pd = ProgressDialog.show(XrayReportActivity.this, "", "Sending");
                task.execute(obj.toString());
            }catch (Exception e){
                System.out.println("cant send");
            }
        }catch (JSONException e){
        }
    }




}
