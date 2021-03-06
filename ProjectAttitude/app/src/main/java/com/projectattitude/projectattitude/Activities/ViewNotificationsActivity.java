package com.projectattitude.projectattitude.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.projectattitude.projectattitude.Adapters.RequestAdapter;
import com.projectattitude.projectattitude.Objects.FollowRequest;
import com.projectattitude.projectattitude.Objects.User;
import com.projectattitude.projectattitude.R;

import java.util.ArrayList;

/**
 * This activity will handle the viewing and handling of notifications
 * such as follow requests
 */
public class ViewNotificationsActivity extends AppCompatActivity {


    private ArrayList<FollowRequest> requests = new ArrayList<FollowRequest>();
    private ListView requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notifications);

        User user = (User)getIntent().getSerializableExtra("user");

        requestList = (ListView)findViewById(R.id.notification_list);
        requests = user.getRequests();
        RequestAdapter adapter = new RequestAdapter(this, requests);
        requestList.setAdapter(adapter);

        if(requests.size() == 0){ //If no requests, show toast message
            Toast.makeText(ViewNotificationsActivity.this, "No pending requests.",
                    Toast.LENGTH_LONG).show();
        }

        setResult(RESULT_OK);
        adapter.notifyDataSetChanged();

    }
}
