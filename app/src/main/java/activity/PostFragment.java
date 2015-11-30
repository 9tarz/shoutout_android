package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RecoverySystem;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.FragmentTransaction;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.nullnil.shoutout.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import app.AppConfig;
import app.AppController;
import helper.Post;
import helper.SessionManager;
import android.text.TextWatcher;
import android.text.Editable;



public class PostFragment extends Fragment {
    private EditText text ;
    private SessionManager session;
    private ProgressDialog dialog , pDialog;
    private double latitude , longitude ;
    private TextView countWords;
    private String count ;
    private int checkCounts ;
    private ImageView imgPreview ;
    private Button btnCapturePicture, btnRecordVideo;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final String IMAGE_DIRECTORY_NAME = "/res";
    private String filePath = null;
    private TextView txtPercentage;
    private Button buttonShout ;
    private static final String TAG = MainActivity.class.getSimpleName();
    long totalSize = 0;
    private Button buttonChoose;
    private Bitmap bitmap;
    private int PICK_IMAGE_REQUEST = 1;
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    private EditText editTextName;




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


    // ===== select photo =====

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    // ====== /select photo ===========
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_post, container, false);
        text = (EditText) rootView.findViewById(R.id.editText_postText);
        buttonShout = (Button) rootView.findViewById(R.id.button_shout);
        buttonShout.setEnabled(false);
        countWords = (TextView) rootView.findViewById(R.id.count);
        // =================== upload ===================== //
        buttonChoose = (Button) rootView.findViewById(R.id.buttonChoose);

        editTextName = (EditText) rootView.findViewById(R.id.editText);

        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    showFileChooser();

            }
        });




        // =================== /upload ==================== //

        // ==================== camera =====================
        imgPreview = (ImageView) rootView.findViewById(R.id.imgPreview);
        btnCapturePicture = (Button) rootView.findViewById(R.id.btnCapturePicture);
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();
            }
        });
        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            getActivity().finish();
        }

        // ==================== /camera =====================

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
                                Fragment backFragment = new TimeLineFragment();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                transaction.replace(R.id.container_body, backFragment);
                                transaction.addToBackStack(null);
                                Bundle bundle = new Bundle();
                                double[] LatLong = {latitude, longitude};
                                bundle.putDoubleArray("pickLatLng", LatLong);
                                backFragment.setArguments(bundle);
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
                        String image = getStringImage(bitmap);

                       String name = editTextName.getText().toString().trim();

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
                        params.put("is_anonymous", "1");
                        params.put(KEY_IMAGE, image);
                        params.put(KEY_NAME, name);


                        return params;
                    }

                };

                AppController.getInstance().addToRequestQueue(strReqPost, tag);
            }

        });
        return rootView ;
    }

    // ======================== camera =====================
    private boolean isDeviceSupportCamera() {
        if (getActivity().getApplicationContext().getPackageManager().hasSystemFeature(

                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Capturing Camera Image will lauch camera app request image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    /*@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }
    */
    private void previewCapturedImage() {
        try {
            // hide video preview
            //videoPreview.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getActivity().getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getActivity().getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imgPreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                // video successfully recorded
                // preview the recorded video
                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        */
    }
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }
        /* else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        }
         */
        else {
            return null;
        }

        return mediaFile;
    }

    // ======================== /camera ====================


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) this.getActivity()).getSupportActionBar().setTitle("Shout");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}