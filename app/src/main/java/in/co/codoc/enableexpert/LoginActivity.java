package in.co.codoc.enableexpert;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class LoginActivity extends AppCompatActivity implements OnTaskCompleted,GetOnTaskCompleted{
    Button normalLogin;
    EditText email_idView;
    EditText passwordView;
    String email_id;
    String password;
    ProgressDialog pd;
    String key;
    int msg;
    boolean expertRegFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        normalLogin = (Button) findViewById(R.id.normal_login_button);
        email_idView = (EditText) findViewById(R.id.email_id);
        passwordView = (EditText) findViewById(R.id.password);
        normalLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                normalLogin();
            }
        });

    }
    void normalLogin(){
        email_id = email_idView.getText().toString();
        password = passwordView.getText().toString();
        if (email_id.matches("")){
            Toast.makeText(getApplicationContext(), "Email cant be empty", Toast.LENGTH_LONG).show();
        } else if(password.matches("")){
            Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_LONG).show();
        } else {
            signInApiCAll();
        }

    }
    void signInApiCAll() {
        try {
            URL url = new URL(Constants.BASE_URL+"/users/login");
            JsonAsyncTask task = new JsonAsyncTask(this,url,"",false);
            pd = ProgressDialog.show(LoginActivity.this, "", "Signing In");
            task.execute(createNormalLoginJson());
        }catch (Exception e){}
    }
    String createNormalLoginJson(){
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("email", email_id);
            jsonObject.put("passwd", password);
            jsonObject.put("is_expert", true);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    @Override
    public void onTaskCompleted(String response) {
        try {
            pd.dismiss();
            System.out.println("response"+response);
            if(response != null) {
                JSONObject resultJson = new JSONObject(response);
                //System.out.println("result"+resultJson.get("success"));
                if (resultJson.getBoolean("success")) {
                    if (expertRegFlag){
                        Toast.makeText(getApplicationContext(), "We will contact you soon", Toast.LENGTH_LONG).show();
                    }else {
                        key = resultJson.getString("key");
                        String user_id = resultJson.getString("user_id");
                        PreferenceManager.getDefaultSharedPreferences(this).edit()
                                .putString("user_id", user_id).apply();
                        PreferenceManager.getDefaultSharedPreferences(this).edit()
                                .putString("password", password).apply();
                        PreferenceManager.getDefaultSharedPreferences(this).edit()
                                .putString("key", key).apply();
                        PreferenceManager.getDefaultSharedPreferences(this).edit()
                                .putString("orginalemail", email_id).apply();
                        if (resultJson.has("msg")) {
                            msg = resultJson.getInt("msg");
                            if (msg == 2) {
                                Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_LONG).show();
                                start_details_activity();
                            }
                        }
                    }

                } else {
                    if (resultJson.has("msg")) {
                        msg = resultJson.getInt("msg");
                        if (msg == 3) {
                            Toast.makeText(getApplicationContext(), "Please Register with us", Toast.LENGTH_LONG).show();
                        } else if (msg == 6) {
                            Toast.makeText(getApplicationContext(), "Please check the password", Toast.LENGTH_LONG).show();
                        }
                    } else if (resultJson.has("res")) {
                        msg = resultJson.getInt("res");
                        if (msg == 3) {
                            Toast.makeText(getApplicationContext(), "Please Register with us", Toast.LENGTH_LONG).show();
                        }
                    }
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
        }catch (JSONException e)
        {
            pd.dismiss();
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_LONG).show();
        }
    }
    void start_details_activity(){
        Intent in1 = new Intent(this, DetailsActivity.class);
        startActivity(in1);
    }
    public void newExpert(View v){
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.register_popup);
        dialog.show();
        Button okbutton = (Button) dialog.findViewById(R.id.okbutton);
        final EditText email_forgot = (EditText) dialog.findViewById(R.id.email);
        final EditText name = (EditText) dialog.findViewById(R.id.name);
        final EditText mciReg = (EditText) dialog.findViewById(R.id.mci_reg);
        final EditText contactNum = (EditText) dialog.findViewById(R.id.c_num);
        final EditText messageView = (EditText) dialog.findViewById(R.id.message);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_str = email_forgot.getText().toString();
                String name_str = name.getText().toString();
                String mciReg_str = mciReg.getText().toString();
                String contactNumStr = contactNum.getText().toString();
                String messageStr = messageView.getText().toString();
                if(messageStr.matches("")){
                    messageStr = "Nil";
                }
                if(email_str.equals("")){
                    Toast.makeText(getApplicationContext(), "Enter your Email id", Toast.LENGTH_LONG).show();
                }else {
                    newExpertApiCall(email_str,name_str,mciReg_str,contactNumStr,messageStr);

                }
            }
        });

    }
    void newExpertApiCall(String email,String name,String mci,String contact,String msg){
        try {
            URL url = new URL(Constants.BASE_URL + "/users/ExpertRegister");
            JsonAsyncTask task = new JsonAsyncTask(this, url, "", false);
            pd = ProgressDialog.show(LoginActivity.this, "", "Sending Request");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email",email);
            jsonObject.put("name",name);
            jsonObject.put("contact",contact);
            jsonObject.put("mcino",mci);
            jsonObject.put("msg",msg);
            expertRegFlag = true;
            task.execute(jsonObject.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void forgetpassword(View v){
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.forgotpassword_dialog);
        dialog.show();
        Button okbutton = (Button) dialog.findViewById(R.id.okbutton);
        final EditText email_forgot = (EditText) dialog.findViewById(R.id.email_forgot);
        email_id = email_idView.getText().toString();
        if(!(email_id.matches(""))){
            email_forgot.setText(email_id, TextView.BufferType.EDITABLE);
        }
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_forgot_str = email_forgot.getText().toString();
                if(email_forgot_str.equals("")){
                    Toast.makeText(getApplicationContext(), "Enter your Email id", Toast.LENGTH_LONG).show();
                }else {
                    forgotPasswordApiCall(email_forgot_str);

                }
            }
        });
    }
    void forgotPasswordApiCall(String email){
        try {
            String baseUrl1 = Constants.BASE_URL+"/users/forgotpassword?email="+email;
            URL url = new URL(baseUrl1);
            GetJsonAsyncTask task2 = new GetJsonAsyncTask(this,url);
            pd = ProgressDialog.show(LoginActivity.this, "", "Please wait");
            task2.execute("");
        }catch (Exception e){
        }
    }
    @Override
    public void GetOnTaskCompleted(String response) {
        pd.dismiss();
        System.out.println(response);
        if(response != null) {
            try {
                System.out.println(response);
                final JSONObject responseJson = new JSONObject(response);
                Boolean success = responseJson.getBoolean("success");
                if (success) {
                    Toast.makeText(LoginActivity.this,"Please check your inbox", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            this.finishAffinity();
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}
