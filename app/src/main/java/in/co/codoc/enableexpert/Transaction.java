package in.co.codoc.enableexpert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashik619 on 15-12-2016.
 */
public class Transaction {
    public String amount;
    public int no_reports;
    public long timeStamp;
    public String expert_name;
    public String invoice_id;
    public String transaction_id;
    public String description;
    public Transaction(JSONObject object){
        try {
            this.no_reports = object.getInt("no_of_reports");
            this.amount = object.getString("amount");
            this.timeStamp = object.getLong("created_at");
            this.amount = object.getString("amount");
            this.description = object.getString("description");
            this.expert_name= object.getString("DocName");
            this.invoice_id = object.getString("invoice_id");
            this.transaction_id = object.getString("trans_id");
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    public static ArrayList<Transaction> fromJson(JSONArray jsonObjects) {
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                transactions.add(new Transaction(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return transactions;
    }
}
