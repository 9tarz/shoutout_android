package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.FragmentTransaction;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.nullnil.shoutout.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.AppConfig;
import app.AppController;
import helper.SessionManager;
import android.text.TextWatcher;
import android.text.Editable;


public class PostFragment extends Fragment {

    private static final String TAG = PostFragment.class.getSimpleName();
    private Button buttonShout ;
    private EditText text ;
    private SessionManager session;
    private ProgressDialog dialog , pDialog;
    private double latitude , longitude ;
    private TextView countWords;
    private String count ;
    private int checkCounts ;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        latitude = bundle.getDoubleArray("pickLatLng")[0];
        longitude = bundle.getDoubleArray("pickLatLng")[1];


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_post, container, false);
        text = (EditText) rootView.findViewById(R.id.editText_postText);
        countWords = (TextView) rootView.findViewById(R.id.count);
        // ================ defect character ================//
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkCounts = 140 - text.getText().length();
                if (checkCounts == 140) {
                    buttonShout.setEnabled(false);
                }
                else{
                    buttonShout.setEnabled(true);
                }
                count = String.valueOf(checkCounts);
                countWords.setText(count);
                // TODO Auto-generated method stub
            }
        });
        Log.d(TAG, "count : "+"");
        // ================================================== //
        buttonShout = (Button) rootView.findViewById(R.id.button_shout);
        buttonShout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                session = new SessionManager(PostFragment.this.getContext());
                final String token = session.getToken();
                String tag = "req_post";

                StringRequest strReqPost = new StringRequest(Request.Method.POST,
                        AppConfig.URL_POST, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jObj = new JSONObject(response);
                            int intError = jObj.getInt("error");
                            boolean error = (intError > 0) ? true : false;

                            if (!error) {
                                Toast.makeText(PostFragment.this.getContext(),
                                        "Complete SHOUT !", Toast.LENGTH_SHORT).show();
                                Fragment backFragment = new HomeFragment();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                transaction.replace(R.id.container_body, backFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();

                            } else {

                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("error_msg");
                                Toast.makeText(PostFragment.this.getContext(),
                                        errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(PostFragment.this.getContext(), "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e(TAG, "Login Error: " + error.getMessage());
                        Toast.makeText(PostFragment.this.getContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        // test
                        Log.d(TAG, "token : " + token);
                        Log.d(TAG, "text : " + text.getText().toString());
                        Log.d(TAG, "latitude : " + Double.toString(latitude));
                        Log.d(TAG, "longitude : " + Double.toString(longitude));
                        //

                        // Posting parameters to login url
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("token", token); // token
                        params.put("text", text.getText().toString()); // text
                        params.put("longitude", String.format("%.6f", longitude)); // logitude
                        params.put("latitude", String.format("%.6f", latitude)); // latitude

                        return params;
                    }

                };

                AppController.getInstance().addToRequestQueue(strReqPost, tag);
            }

        });
        return rootView ;
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}