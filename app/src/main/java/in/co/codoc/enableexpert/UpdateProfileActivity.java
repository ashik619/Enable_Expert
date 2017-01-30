package in.co.codoc.enableexpert;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfileActivity extends AppCompatActivity  {
    final CharSequence[] AdbItems = {
            "Gallery", "Camera",
    };
    String user_id_str ;
    String key ;
    ProgressDialog pd;
    private static final int RESULT_LOAD_IMG = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private LocationManager locationManager;
    String locationStr;
    String image_url;
    Boolean profileImageSelected;
    ImageView locationIcon;
    IconTextView locationTextView;
    ImageView profiilepiView;
    Target target;
    String fname;
    String nameStr;
    String qualStr;
    String noteStr;
    EditText name;
    EditText qual;
    EditText note;
    File myDir;
    String capturedImageFileName = null;
    File capturedImageFile = null;
    String selectedImage = null;
    String profilePictureFileName;
    Bitmap bp;
    int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        locationIcon = (ImageView) findViewById(R.id.loc_icon);
        profiilepiView = (ImageView) findViewById(R.id.profileImage);
        locationTextView = (IconTextView) findViewById(R.id.location_text);
        name = (EditText)findViewById(R.id.name);
        qual = (EditText)findViewById(R.id.qualification);
        note = (EditText)findViewById(R.id.about_me);
        user_id_str = (PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", null));
        key = (PreferenceManager.getDefaultSharedPreferences(this).getString("key", null));
        profilePictureFileName = "image_orginal.png";
        getIntentData();

    }

    void getIntentData(){
        Intent i = getIntent();
        image_url = i.getStringExtra("image_url");
        flag = i.getIntExtra("flag",9);
        profileImageSelected = i.getBooleanExtra("cropped",false);
        if (image_url != null) {
            savePreviousProfile();
            Picasso.with(this)
                    .load(image_url)
                    .placeholder(R.drawable.default_profile)   // optional
                    .error(R.drawable.default_profile)      // optional
                    .into(profiilepiView);
            Picasso.with(this)
                    .load(image_url)
                    .into(target);
        }else {
            if(!profileImageSelected) {
                Bitmap bmp = BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.default_profile);
                saveDefaultProfile(bmp);
            }
        }
        if(profileImageSelected){
            profilePictureFileName = i.getStringExtra("file");
            File myDir = new File(Constants.root + "/Rhythmia");
            File file = new File(myDir, profilePictureFileName);
            try {
                Uri imageUri = Uri.fromFile(file);
                bp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profiilepiView.setImageBitmap(bp);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        nameStr = i.getStringExtra("name");
        qualStr = i.getStringExtra("qual");
        noteStr = i.getStringExtra("note");
        locationStr = i.getStringExtra("location");
        if(locationStr == null){
            locationStr = "Location not updated";
        }
        if(nameStr != null){
            name.setText(nameStr, TextView.BufferType.EDITABLE);
        }
        if(qualStr != null){
            qual.setText(qualStr, TextView.BufferType.EDITABLE);
        }
        if(noteStr != null){
            note.setText(noteStr, TextView.BufferType.EDITABLE);
        }
    }
    public void back(View v){
        Intent i = new Intent(getApplicationContext(), DetailsActivity.class);
        startActivity(i);

    }
    public void select(View v) {
        showADBdialog();
    }
    void showADBdialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(AdbItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                if(item==0){
                    activeGallery();
                }
                else if(item == 1)
                {
                    activeTakePhoto();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void activeTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
    private void activeGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }
    public void getlocation(View v){
        if(isLocationEnabled(this)){
            setLocation();
        }else {
            final Dialog dialog = new Dialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.location_dialog);
            dialog.show();
            Button okbutton = (Button) dialog.findViewById(R.id.okbutton);
            okbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    dialog.dismiss();
                    startActivity(intent);
                }
            });        }
    }
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
    void setLocation(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            if (location != null) {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String city = addresses.get(0).getLocality();
                    String country = addresses.get(0).getCountryName();
                    System.out.println(addresses.get(0).getAddressLine(0)+addresses.get(0).getAddressLine(1)+addresses.get(0).getAddressLine(2));
                    locationStr = city + ","  + country;
                    locationIcon.setVisibility(View.VISIBLE);
                    locationTextView.setText(locationStr);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            } else {
                locationTextView.setText("Location not available");

                locationIcon.setVisibility(View.GONE);
            }

        }
    }
    void savePreviousProfile(){
        target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        File myDir = new File(Constants.root + "/Rhythmia");
                        myDir.mkdirs();
                        fname = "image_orginal" + ".png";
                        File file = new File(myDir, fname);
                        if (file.exists())
                            file.delete();
                        try
                        {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, ostream);
                            ostream.close();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {
                }
            }
        };
    }
    void saveDefaultProfile(Bitmap bm){
        File myDir = new File(Constants.root + "/Rhythmia");
        myDir.mkdirs();
        fname = "image_orginal" + ".png";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override protected void onActivityResult(int requestCode, int resultCode,
                                              Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMG:
                if (requestCode == RESULT_LOAD_IMG &&
                        resultCode == RESULT_OK && null != data) {
                    selectedImage = data.getData().toString();
                    startCrop(true);
                }
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == REQUEST_IMAGE_CAPTURE &&
                        resultCode == RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    saveImgFile(photo);
                    startCrop(false);

                }
        }
    }
    void saveImgFile(Bitmap bm){
        myDir = new File(Constants.root + "/Rhythmia");
        myDir.mkdirs();
        capturedImageFileName = "profile_captured"+createRandomString()+".png";
        capturedImageFile = new File(myDir, capturedImageFileName);
        if (capturedImageFile.exists())
            capturedImageFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(capturedImageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            if(bm!=null)
            {
                bm.recycle();
                bm=null;
            }
            System.out.println(capturedImageFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    String createRandomString(){
        Long time= System.currentTimeMillis();
        String timehex = Long.toHexString(time);
        return timehex;
    }
    public void startCrop(boolean captureFlag){
        Intent i = new Intent(getApplicationContext(), CropActivity.class);
        if(captureFlag){
            i.putExtra("file",selectedImage.toString());
        }else {
            i.putExtra("file",fname);
        }
        i.putExtra("flag",captureFlag);
        i.putExtra("update",true);
        i.putExtra("name",nameStr);
        i.putExtra("qual",qualStr);
        i.putExtra("note",noteStr);
        i.putExtra("location",locationStr);
        startActivity(i);
    }
    public void update(View v){
        nameStr =  name.getText().toString();
        qualStr = qual.getText().toString();
        noteStr = note.getText().toString();
        if(nameStr.matches("")){
            Toast.makeText(getApplicationContext(), "Enter Your Name", Toast.LENGTH_LONG).show();
        }else if(qualStr.matches("")){
            Toast.makeText(getApplicationContext(), "Enter Your Qualification", Toast.LENGTH_LONG).show();
        }else if(noteStr.matches("")){
            noteStr = " ";
            System.out.println("qual"+qualStr);
            pd = ProgressDialog.show(UpdateProfileActivity.this, "", "Updating");
            profileUpdateApiCall();
        }else {
            System.out.println("qual"+qualStr);
            pd = ProgressDialog.show(UpdateProfileActivity.this, "", "Updating");
            profileUpdateApiCall();
        }

    }
    void profileUpdateApiCall() {
        FileUploadService service =
                ServiceGenerator.createService(FileUploadService.class);
        MultipartBody.Part body;
        File file;

        file = new File(Constants.root + "/Rhythmia", profilePictureFileName);

        RequestBody user_id =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), user_id_str);
        RequestBody name =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), nameStr);
        RequestBody about_me =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), noteStr);
        RequestBody education =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), qualStr);
        System.out.println("loc"+locationStr);
        RequestBody location =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), locationStr);
        RequestBody is_expert =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), "false");
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);
        body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<ResponseBody> profilePicCall = service.updateProfile(user_id,name,about_me,education,location,is_expert, body);
        profilePicCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                try {
                    System.out.println("respo"+response.body().string());
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), MyProfileActivity.class);
                    startActivity(i);

                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "Profile not Updated", Toast.LENGTH_SHORT).show();
                Log.e("Upload error:", t.getMessage());
            }
        });
    }
    @Override
    public void onBackPressed() {
        if(flag == 0){
            Intent in1 = new Intent(this, DetailsActivity.class);
            startActivity(in1);
        }else {
            Intent i = new Intent(getApplicationContext(), DetailsActivity.class);
            startActivity(i);
        }
    }
}
