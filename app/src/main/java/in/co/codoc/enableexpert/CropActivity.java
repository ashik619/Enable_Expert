package in.co.codoc.enableexpert;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CropActivity extends AppCompatActivity {
    CropImageView cropImageView;
    String filename;
    File myDir;
    String fname = null;
    Bitmap bp;
    Bitmap cropped;
    Bitmap resized;
    Bitmap rotatedBitmap;
    String root = Environment.getExternalStorageDirectory().toString();
    Button crop;
    Button next;
    ImageView imageView;
    Boolean cropFlag = false;
    Boolean updateFlag;
    String nameStr = null;
    String qualStr = null;
    String noteStr = null;
    String locationStr = null;
    boolean flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        imageView = (ImageView) findViewById(R.id.imageView);
        cropImageView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        crop = (Button) findViewById(R.id.crop);
        next = (Button) findViewById(R.id.next);
        getintent();
        imageView.setImageBitmap(bp);
        crop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                crop();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                next();

            }
        });
    }
    void getintent(){
        Intent i = getIntent();
        filename = i.getStringExtra("file");
        nameStr = i.getStringExtra("name");
        qualStr = i.getStringExtra("qual");
        noteStr = i.getStringExtra("note");
        locationStr =i.getStringExtra("location");
        flag = i.getBooleanExtra("flag",false);
        updateFlag = i.getBooleanExtra("update",false);
        try {
            bp = decodeSampledBitmapFromUri(this,filename,flag);
        }catch (Exception e){
            e.printStackTrace();
            finish();
        }
    }
    public  Bitmap decodeSampledBitmapFromUri(Context context, String filename, boolean flag) throws FileNotFoundException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        String filePath;
        if(!flag) {
            File image = new File(root + "/Rhythmia", filename);
            filePath =  image.getAbsolutePath();
        }else {
            Uri imageUri = Uri.parse(filename);
            filePath = ImageFilePath.getPath(context,imageUri);
        }
        //File image = new File(ImageFilePath.getPath(context,imageUri));
        System.out.println(filePath);
        BitmapFactory.decodeFile(filePath, options);
        options.inJustDecodeBounds = true;
        final int height = options.outHeight;
        final int width = options.outWidth;
        int reqWidth;
        int reqHeight;
        int reqSize;
        if(updateFlag){
            reqSize = 250;
        }else {
            reqSize = 512;
        }
        if(width>height){
            reqWidth = reqSize;
            float hw = (float)height/width;
            float newH = hw*reqSize;
            reqHeight = Math.round(newH);
        }else {
            reqHeight = reqSize;
            float wh = (float)width/height;
            float newW = wh*reqSize;
            reqWidth = Math.round(newW);
        }
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        return inSampleSize;
    }
    void crop(){
        if(cropFlag){
            cropImageView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            crop.setText("Crop");
            cropFlag = false;
        }else {
            imageView.setVisibility(View.GONE);
            cropImageView.setVisibility(View.VISIBLE);
            cropImageView.setImageBitmap(bp);
            crop.setText("Cancel");
            cropFlag = true;
        }
        // cropImageView.setImageBitmap(cropped);
    }
    void next(){
        if(updateFlag){
            if (cropFlag) {
                cropped = cropImageView.getCroppedImage();
                saveForProfile(cropped,false);
            } else {
                saveForProfile(bp,false);
            }
            if (fname != null) {
                startUpdateProfileActivity();
            }

        }
    }
    void resize(Bitmap bp){
        int h = bp.getHeight();
        int w = bp.getWidth();
        Matrix matrix = new Matrix();
        if((w>512)&&(w>h)) {
            float hw = (float)h/w;
            float newH = hw*512;
            h = Math.round(newH);
            w = 512;
        }else if(h>512){
            float wh = (float)w/h;
            float newW = wh*512;
            w = Math.round(newW);
            h=512;
        }
        resized = Bitmap.createScaledBitmap(bp, w, h, true);
        if(h>w){
            matrix.postRotate(90);
            rotatedBitmap = Bitmap.createBitmap(resized , 0, 0, w,h, matrix, true);
            saveImgFile(rotatedBitmap);
        } else saveImgFile(resized);
    }
    void saveImgFile(Bitmap bm){
        myDir = new File(root + "/Rhythmia");
        myDir.mkdirs();
        fname = "ECG_image"+createRandomString()+".png";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            if(bm!=null)
            {
                bm.recycle();
            }
            System.out.println(file.toString());
            //startReportActivity();
        } catch (Exception e) {

        }
    }
    String createRandomString(){
        Long time= System.currentTimeMillis();
        String timehex = Long.toHexString(time);
        return timehex;
    }
    void startUpdateProfileActivity(){
        Intent i = new Intent(getApplicationContext(), UpdateProfileActivity.class);
        i.putExtra("cropped",true);
        i.putExtra("name",nameStr);
        i.putExtra("qual",qualStr);
        i.putExtra("note",noteStr);
        i.putExtra("location",locationStr);
        i.putExtra("file",fname);
        startActivity(i);

    }
    void resizeForProfile(Bitmap bp){
        int h = bp.getHeight();
        int w = bp.getWidth();
        if((w>250)&&(w>h)) {
            float hw = (float)h/w;
            float newH = hw*250;
            h = Math.round(newH);
            w = 250;
        }else if(h>250){
            float wh = (float)w/h;
            float newW = wh*250;
            w = Math.round(newW);
            h=250;
        }
        resized = Bitmap.createScaledBitmap(bp, w, h, true);
        saveForProfile(resized,false);

    }
    void saveForProfile(Bitmap bm,Boolean flag) {
        myDir = new File(root + "/Rhythmia");
        myDir.mkdirs();
        fname = "Profile"+createRandomString() + ".png";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {

        }
        if(bm!=null)
        {
            bm.recycle();
        }
    }
}


