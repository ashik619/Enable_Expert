package in.co.codoc.enableexpert;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TransactionHistory extends AppCompatActivity implements GetOnTaskCompleted {
    String user_id;
    ListView listView;
    TextView noTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        listView = (ListView) findViewById(R.id.transaction_list);
        noTransaction = (TextView) findViewById(R.id.no_transaction_text);
        user_id = (PreferenceManager.getDefaultSharedPreferences(this).getString("user_id",null));
        getTransaction();
    }
    void getTransaction(){
        try {
            String baseUrl1 = Constants.BASE_URL+"/users/gettransactions?user_id="+user_id;
            URL url = new URL(baseUrl1);
            GetJsonAsyncTask task2 = new GetJsonAsyncTask(this,url);
            task2.execute("");
        }catch (Exception e){
        }
    }
    @Override
    public void GetOnTaskCompleted(String response) {
        if(response != null) {
            try {
                System.out.println(response);
                final JSONObject responseJson = new JSONObject(response);
                Boolean success = responseJson.getBoolean("success");
                if (success) {
                    listView.setVisibility(View.VISIBLE);
                    noTransaction.setVisibility(View.GONE);
                    final JSONArray packagesJsonArray = responseJson.getJSONArray("result");
                    ArrayList<Transaction> transactions = Transaction.fromJson(packagesJsonArray);
                    UsersAdapter adapter = new UsersAdapter(this, transactions);
                    listView.setAdapter(adapter);
                }
                else {
                    listView.setVisibility(View.GONE);
                    noTransaction.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void back(View v){ finish();}

    public class UsersAdapter extends ArrayAdapter<Transaction> {
        public UsersAdapter(Context context, ArrayList<Transaction> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final Transaction transaction = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.transaction_list_item, parent, false);
            }
            // Lookup view for data population
            final RelativeLayout btn = (RelativeLayout) convertView.findViewById(R.id.btn);
            TextView tvAmount = (TextView) convertView.findViewById(R.id.amount);
            TextView tvNoOfReports = (TextView) convertView.findViewById(R.id.no_reports);
            TextView tvTimeStamp = (TextView) convertView.findViewById(R.id.time_stamp);
            TextView tvOrderId = (TextView) convertView.findViewById(R.id.order_id);
            final String amount = transaction.amount;
            final String timeStamp = getTimeStamp(transaction.timeStamp);
            final String no_reports = String.valueOf(transaction.no_reports);
            final String order_id = transaction.description;
            btn.setTag(transaction);
            System.out.println();
            tvAmount.setText(amount);
            tvNoOfReports.setText(no_reports);
            tvOrderId.setText(order_id);
            tvTimeStamp.setText("On "+timeStamp);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Transaction transaction = (Transaction) view.getTag();
                    final String amount = transaction.amount;
                    final String description = transaction.description;
                    final String expertName = transaction.expert_name;
                    final String invoice_id = transaction.invoice_id;
                    final String timeStamp = getTimeStamp(transaction.timeStamp);
                    final String no_reports = String.valueOf(transaction.no_reports);
                    startInvoiceActivity(amount,description,expertName,invoice_id,no_reports,timeStamp);

                }
            });
            return convertView;
        }
        public String getTimeStamp(long ms){
            String timeStamp;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(ms);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);
            month+=1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
            String formattedDate = df.format(c.getTime());
            timeStamp = ""+day+"/"+month+"/"+year + " at "+formattedDate ;
            return timeStamp;
        }

    }
    void startInvoiceActivity(String amount,String description,String expertName,String invoice_id,String no_reports,String timeStamp){
        Intent i = new Intent(this, InvoiceActivity.class);
        i.putExtra("amount",amount);
        i.putExtra("description",description);
        i.putExtra("expert_name",expertName);
        i.putExtra("invoice_id",invoice_id);
        i.putExtra("time_stamp",timeStamp);
        i.putExtra("no_reports",no_reports);
        startActivity(i);
    }
}
