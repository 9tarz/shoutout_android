package activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.nullnil.shoutout.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.AppConfig;
import app.AppController;

import adapter.SwipeListAdapter;
import helper.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeLineFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = TimeLineFragment.class.getSimpleName();
    private double[] LatLong;
    private double latitude;
    private double longitude;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerView;
    //private ListView listView;
    private SwipeListAdapter adapter;
    private List<Post> postList;
    private Button buttonPost ;


    public TimeLineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        LatLong = bundle.getDoubleArray("pickLatLng");
        // in case you want not array value
        latitude = LatLong[0];
        longitude = LatLong[1];
    }

    @Override
    public void onRefresh() {
        fetchPosts(latitude,longitude);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(this);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        postList = new ArrayList<>();
        adapter = new SwipeListAdapter(this.getActivity(), postList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(adapter);

        swipeContainer.setOnRefreshListener(this);
        swipeContainer.post(new Runnable() {
                                @Override
                                public void run() {
                                    swipeContainer.setRefreshing(true);
                                    fetchPosts(latitude, longitude);
                                }
                            }
        );

        /*buttonPost = (Button) rootView.findViewById(R.id.button_Post);
        buttonPost.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Fragment nextFragment = new PostFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container_body, nextFragment);
                transaction.addToBackStack(null);
                // pick location
                Bundle bundle = new Bundle();
                double[] LatLong = {latitude, longitude};
                bundle.putDoubleArray("pickLatLng", LatLong);
                nextFragment.setArguments(bundle);
                // goo
                transaction.commit();
            }

        });*/
        FloatingActionButton btnFab = (FloatingActionButton) rootView.findViewById(R.id.button_post);
        btnFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Fragment nextFragment = new PostFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container_body, nextFragment);
                transaction.addToBackStack(null);
                // pick location
                Bundle bundle = new Bundle();
                double[] LatLong = {latitude, longitude};
                bundle.putDoubleArray("pickLatLng", LatLong);
                nextFragment.setArguments(bundle);
                // goo
                transaction.commit();
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    private void fetchPosts(final double latitude, final double longitude) {

        swipeContainer.setRefreshing(true);
        postList.clear();
        String tag_string_req = "req_fetchPosts";

        String stringLatitude = String.valueOf(latitude);
        String stringLongitude = String.valueOf(longitude);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_FETCHPOSTS + "/" + stringLatitude + "/" + stringLongitude, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "fetchPosts Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    int intError = jObj.getInt("error");
                    boolean error = (intError > 0) ? true : false;
                    // Check for error node in json
                    if (!error) {
                        JSONArray arr_post = jObj.getJSONArray("posts");
                        for (int i = 0; i < arr_post.length(); i++) {
                            String postUsername = arr_post.getJSONObject(i).getString("username");
                            String postText = arr_post.getJSONObject(i).getString("text");
                            String postTimestamp = arr_post.getJSONObject(i).getString("created_at");
                            long timestamp;
                            Date dateTimestamp = null;
                            try {
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                //df.setTimeZone(TimeZone.getTimeZone("GMT-04:00"));
                                dateTimestamp = df.parse(postTimestamp);
                                //df.setTimeZone(TimeZone.getTimeZone("GMT+07:00"));
                                //String strGMT7 = df.format(dateTimestamp);
                                //dateTimestamp = df.parse(strGMT7);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            timestamp = dateTimestamp.getTime();
                            Log.i(TAG, "Username:" +postUsername);
                            Log.i(TAG, "Text:" +postText);
                            Log.i(TAG, "Timestamp:" +postTimestamp);
                            Post p = new Post(postText,postUsername, timestamp);

                            postList.add(0, p);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(TimeLineFragment.this.getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                    swipeContainer.setRefreshing(false);
                    //Toast.makeText(TimeLineFragment.this.getContext(), "Update Completed", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(TimeLineFragment.this.getContext(), "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "fetchPosts Error: " + error.getMessage());
                Toast.makeText(TimeLineFragment.this.getContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                swipeContainer.setRefreshing(false);
            }
        }) {
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}