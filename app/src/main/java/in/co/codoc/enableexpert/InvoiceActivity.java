package in.co.codoc.enableexpert;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class InvoiceActivity extends AppCompatActivity {
    RelativeLayout invoiceView;
    String root = Environment.getExternalStorageDirectory().toString();
    File myDir;
    File pdfFile;
    String amount;
    String timeStamp;
    String invoice_id;
    String description;
    String expertName;
    String docName;
    String no_reports;
    File pdfTemplateFile;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);
        invoiceView = (RelativeLayout)findViewById(R.id.invoice_view);
        TextView docNameTv = (TextView)findViewById(R.id.doc_name);
        TextView expNameTv = (TextView)findViewById(R.id.expert_name);
        TextView amountTv = (TextView)findViewById(R.id.amount);
        TextView totalTv = (TextView)findViewById(R.id.total);
        TextView descriptionTv = (TextView)findViewById(R.id.order_id1);
        TextView invoice_idTv = (TextView)findViewById(R.id.invoice_id);
        TextView timeStampTv = (TextView)findViewById(R.id.time_stamp);
        TextView noReportsTv = (TextView)findViewById(R.id.no_reports);
        docName = (PreferenceManager.getDefaultSharedPreferences(this).getString("prof",null));
        email = (PreferenceManager.getDefaultSharedPreferences(this).getString("orginalemail",null));
        getintentdata();
        amountTv.setText(amount);
        expNameTv.setText("Dr."+docName);
        docNameTv.setText("Dr."+expertName);
        totalTv.setText(amount);
        descriptionTv.setText(description);
        invoice_idTv.setText(invoice_id);
        timeStampTv.setText(timeStamp);
        noReportsTv.setText(no_reports+ " Reports");
        myDir = new File(root + "/Codoc");
        myDir.mkdirs();
    }
    void getintentdata(){
        Intent i = getIntent();
        amount = i.getStringExtra("amount");
        timeStamp = i.getStringExtra("time_stamp");
        invoice_id = i.getStringExtra("invoice_id");
        description = i.getStringExtra("description");
        expertName = i.getStringExtra("expert_name");
        no_reports = i.getStringExtra("no_reports");
    }
    /*public void share(View v){
        createPdf();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Rhythmia Invoice");
        Uri uri = Uri.fromFile(pdfFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, "Share Invoice"));
    }
    void createPdf(){
        loadTemplate();
        pdfTemplateFile = new File(myDir, "invoice.pdf");
        pdfFile = new File(myDir, "invoice" + description + ".pdf");
        try {
            PdfReader pdfTemplate = new PdfReader(pdfTemplateFile.toString());
            FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(pdfTemplate, fileOutputStream,'\0', true);
            // stamper.setFormFlattening(true);
            AcroFields acroFields = stamper.getAcroFields();
            // AcroFields fields = pdfTemplate.getAcroFields();
            Set<String> fields = acroFields.getFields().keySet();//
            System.out.println("size of fields"+fields.size());
            for (String s : fields) {
                System.out.println(s);
            }
            acroFields.setField("docname", expertName);
            acroFields.setField("expname", docName);
            acroFields.setField("date", timeStamp);
            acroFields.setField("ord_id", description);
            acroFields.setField("in_id", invoice_id);
            acroFields.setField("no_reports", no_reports + "Reports");
            acroFields.setField("total", amount);
            acroFields.setField("amount", amount);
            //acroFields.setField("email", email);

            stamper.close();
            pdfTemplate.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void loadTemplate(){
        byte[] buffer=null;
        InputStream fIn = getBaseContext().getResources().openRawResource(R.raw.invoice);
        int size=0;
        try {
            size = fIn.available();
            buffer = new byte[size];
            fIn.read(buffer);
            fIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean exists = (new File(root + "/Codoc","invoice.pdf")).exists();
        if(!exists){
            System.out.println("file not found");
            FileOutputStream save;
            try {
                pdfTemplateFile = new File(myDir, "invoice.pdf");
                save = new FileOutputStream(pdfTemplateFile);
                save.write(buffer);
                save.flush();
                save.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }else {
            System.out.println("file found");
        }
    }
    */
    public void back(View v){ finish();}
}