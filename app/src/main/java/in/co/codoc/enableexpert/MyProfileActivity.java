package in.co.codoc.enableexpert;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class MyProfileActivity extends AppCompatActivity implements OnTaskCompleted{
    String user_id ;
    String key;
    String name;
    String loc;
    String qual;
    String note_str;
    String image_url;
    ImageView profile;
    IconTextView phyname;
    IconTextView location;
    IconTextView qualification;
    IconTextView note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        profile = (ImageView)findViewById(R.id.profile);
        phyname = (IconTextView) findViewById(R.id.name);
        location = (IconTextView) findViewById(R.id.location);
        qualification = (IconTextView) findViewById(R.id.qualification);
        note = (IconTextView) findViewById(R.id.about_me);
        user_id = (PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", null));
        key = (PreferenceManager.getDefaultSharedPreferences(this).getString("key", null));
    }
    @Override
    protected void onResume() {
        super.onResume();

        getprofileApiCall();

    }
    void getprofileApiCall() {
        try {
            JSONObject prof = new JSONObject();
            prof.put("user_id",user_id);
            URL url = new URL(Constants.BASE_URL+"/users/UpdatedProfile");
            JsonAsyncTask task = new JsonAsyncTask(this,url,key,true);
            task.execute(prof.toString());
        }catch (Exception e){
            System.out.println("cant send");
        }
    }
    @Override
    public void onTaskCompleted(String response) {
        // Just showing the response in a Toast message
        if(response != null) {
            System.out.println("task completed");
            try {
                JSONObject resultJson = new JSONObject(response);
                //System.out.println("result"+resultJson.get("success"));
                System.out.println("response" + response);
                boolean success = resultJson.getBoolean("success");
                if (success) {
                    JSONObject result = resultJson.getJSONObject("result");
                    name = result.getString("name");
                    qual = result.getString("medical_education");
                    note_str = result.getString("about_me");
                    loc = result.getString("location");
                    image_url = result.getString("image_url");
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString("prof", name).apply();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString("qualification", qual).apply();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString("note", note_str).apply();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString("location", loc).apply();
                    setProfileData();
                }

            } catch (JSONException e) {

            }
        }else {
        }
    }
    void setProfileData() {
        try {
            phyname.setText(name);
            if(loc.matches("null")){
                location.setText("Location not updated");
            }else location.setText(loc);
            qualification.setText(qual);
            Picasso.with(this)
                    .load(image_url)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(profile);
            if (image_url!=null){
                //savePreviousProfile();
               /* Picasso.with(this)
                        .load(image_url)
                        .into(target);*/
            }
            note.setText(note_str);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void back(View v){
        Intent in1 = new Intent(this, DetailsActivity.class);
        startActivity(in1);
    }
    public void edit(View v){
        Intent i = new Intent(getApplicationContext(), UpdateProfileActivity.class);
        i.putExtra("image_url",image_url);
        i.putExtra("name",name);
        i.putExtra("qual",qual);
        i.putExtra("note",note_str);
        i.putExtra("location",loc);
        startActivity(i);
    }
    @Override
    public void onBackPressed() {
        Intent in1 = new Intent(this, DetailsActivity.class);
        startActivity(in1);
    }
}
