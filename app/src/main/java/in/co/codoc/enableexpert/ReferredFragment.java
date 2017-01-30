package in.co.codoc.enableexpert;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReferredFragment extends Fragment implements OnTaskCompleted {
    ListView listView;
    LinearLayout noReport;
    Realm myRealm;
    String user_id ;
    String key ;
    String baseUrl = Constants.BASE_URL;
    SwipeRefreshLayout mSwipeRefreshLayout;
    int msg;
    String report_id;
    public ReferredFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.referred_fragment_layout, container, false);
        noReport = (LinearLayout) rootView.findViewById(R.id.no_report);
        listView = (ListView) rootView.findViewById(R.id.list_view);
        user_id = (PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("user_id", null));
        key = (PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("key", null));
        noReport.setVisibility(View.GONE);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        Realm.init(getActivity().getApplicationContext());
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .name("referred.realm")
                .build();
        myRealm = Realm.getInstance(config);
        getPendingReportsApiCall();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPendingReportsApiCall();

            }
        });
        return rootView;
    }
    void getPendingReportsApiCall(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("user_id", user_id);
            URL url = new URL(baseUrl+"/reports/getAcceptedReportList");
            JsonAsyncTask task = new JsonAsyncTask(this,url,key,true);
            System.out.println(obj.toString());
            task.execute(obj.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onTaskCompleted(String response) {
        // Just showing the response in a Toast message
        mSwipeRefreshLayout.setRefreshing(false);
        if (response != null) {
            try {
                JSONObject resultJson = new JSONObject(response);
                System.out.println("response" + response);
                boolean success = resultJson.getBoolean("success");
                if (success) {
                    JSONArray result = resultJson.getJSONArray("result");
                    saveResponseToLocalDb(result);
                }else {
                    msg = resultJson.getInt("msg");
                    if(msg == 5) {
                        listView.setVisibility(View.GONE);
                        noReport.setVisibility(View.VISIBLE);
                        RealmResults<ReferredReport> results = myRealm.where(ReferredReport.class).findAll();
                        myRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        myRealm.commitTransaction();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            populateListView();
        }
    }
    void populateListView(){
        RealmResults<ReferredReport> referredReports = myRealm.where(ReferredReport.class).findAll();
        if(referredReports.size()!=0) {
            LocalAdapter adapter = new LocalAdapter(getActivity().getApplicationContext(), referredReports, true);
            listView.setAdapter(adapter);
        }
    }

    void saveResponseToLocalDb(JSONArray result){
        //delete all reports from db to override
        try {

            RealmResults<ReferredReport> results = myRealm.where(ReferredReport.class).findAll();
            myRealm.beginTransaction();
            results.deleteAllFromRealm();
            myRealm.commitTransaction();
            for (int i = 0; i < result.length(); i++) {
                final JSONObject report = result.getJSONObject(i);
                final String report_id = report.getString("report_id");
                myRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ReferredReport referredReport = realm.createObject(ReferredReport.class,report_id);
                        try {

                            referredReport.setExpert_id(report.getString("doctor_id"));
                            referredReport.setExpertName(report.getString("DocName"));
                            referredReport.setImageUrl(report.getString("imgUrl"));
                            referredReport.setThubUrl(report.getString("thumbUrl"));
                            referredReport.setAbout_me(report.getString("aboutme"));
                            referredReport.setLocation(report.getString("location"));
                            referredReport.setQualification(report.getString("qualification"));
                            populateListView();
                        }catch (JSONException e){e.printStackTrace(); }
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public class LocalAdapter extends RealmBaseAdapter<ReferredReport> implements ListAdapter {
        RelativeLayout btn;
        RealmResults<ReferredReport> realmResults;
        public LocalAdapter(Context context, RealmResults<ReferredReport> realmResults, boolean automaticUpdate) {
            super(context, realmResults);
            this.realmResults = realmResults;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent ) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.pending_list_item, parent, false);

            } else {}
            btn = (RelativeLayout) convertView.findViewById(R.id.btn);
            TextView tvReportId = (TextView) convertView.findViewById(R.id.report_id);
            TextView tvDoc = (TextView) convertView.findViewById(R.id.docname);
            ImageView profilePicture = (ImageView) convertView.findViewById(R.id.profilepicView);
            final ReferredReport item = realmResults.get(position);
            btn.setTag(position);
            profilePicture.setTag(position);
            tvReportId.setText("XRAY#"+item.getReport_id().toString());
            report_id = item.getReport_id().toString();
            tvDoc.setText("Referred by Dr. "+item.getExpertName());
            String image_url = item.getThubUrl();
            Picasso.with(getActivity().getApplicationContext())
                    .load(image_url)
                    .error(R.drawable.thumb_image_frame)
                    .placeholder(R.drawable.thumb_image_frame)
                    .into(profilePicture);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    ReferredReport item = realmResults.get(position);
                    startReportActivity(item.getReport_id());
                }
            });
            profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    ReferredReport item = realmResults.get(position);
                    // startProfileActivity(item.getExpertName(),item.getLocation(),item.getQualification(),item.getAbout_me(),item.getImageUrl());
                }
            });
            return convertView;
        }
        void startReportActivity(String report_id){
            Intent i = new Intent(getActivity().getApplicationContext(), XrayReportActivity.class);
            i.putExtra("report_id",report_id);
            startActivity(i);
        }
    }
}

