package activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.FragmentTransaction;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import com.example.nullnil.shoutout.R;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Locale;
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
    private CheckBox isAnonymous;
    private int is_anonymous;

    private ImageView imgPreview ;
    private Button btnCapturePicture, btnRecordVideo;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final String IMAGE_DIRECTORY_NAME = "/Shoutout";
    private String filePath = null;
    private TextView txtPercentage;
    long totalSize = 0;

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;
    private byte[] multipartBody;
    private SimpleDraweeView previewImage;
    private int PICK_IMAGE_REQUEST = 1;
    private Button buttonChoose;
    String pathfile ;
    String realPath;
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
        buttonShout = (Button) rootView.findViewById(R.id.button_shout);
        buttonShout.setEnabled(false);
        countWords = (TextView) rootView.findViewById(R.id.count);
        isAnonymous = (CheckBox) rootView.findViewById(R.id.checkBox);
        buttonChoose = (Button) rootView.findViewById(R.id.buttonChoose);

        pDialog = new ProgressDialog(PostFragment.this.getContext());
        pDialog.setCancelable(false);

        // ==================== camera =====================
        //imgPreview = (ImageView) rootView.findViewById(R.id.imgPreview);
        previewImage = (SimpleDraweeView) rootView.findViewById(R.id.imgPreview);
        btnCapturePicture = (Button) rootView.findViewById(R.id.btnCapturePicture);
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();
            }
        });
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showFileChooser();

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

                is_anonymous = (isAnonymous.isChecked()) ? 1 : 0;

                BitmapFactory.Options options = new BitmapFactory.Options();

                // downsizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 4;

                Boolean has_image = false;
                byte[] fileImage = null;

                if (fileUri != null) {
                    pDialog.setMessage("Uploading image ...");
                    pathfile = fileUri.getPath();
                    if (realPath != null)
                        pathfile = realPath ;
                    Log.d(TAG, "path file photo : "+pathfile);
                    // repair select //

                    // ============= //
                    Bitmap bitmap = BitmapFactory.decodeFile(pathfile, options);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    fileImage = byteArrayOutputStream.toByteArray();
                    has_image = true;
                }
                else {
                    pDialog.setMessage("Shouting out ...");
                }
                showDialog();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                try {
                    // the first file
                    // test
                    Log.d(TAG, "token : " + token);
                    Log.d(TAG, "text : " + text.getText().toString());
                    Log.d(TAG, "latitude : " + Double.toString(latitude));
                    Log.d(TAG, "longitude : " + Double.toString(longitude));
                    Log.d(TAG, "is anonymous : " + Integer.toString(is_anonymous));
                    //
                    if (has_image) {
                        buildPart(dos, fileImage, "upload_image.jpg");
                    }
                    buildTextPart(dos, "token", token);
                    buildTextUTFPart(dos, "text", text.getText().toString());
                    buildTextPart(dos, "longitude",String.format("%.6f", longitude) );
                    buildTextPart(dos, "latitude",String.format("%.6f", latitude) );
                    buildTextPart(dos, "is_anonymous", Integer.toString(is_anonymous) );

                    // send multipart form data necesssary after file data
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    // pass to multipart body
                    multipartBody = bos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MultipartRequest multipartRequest = new MultipartRequest(AppConfig.URL_POST, null, mimeType, multipartBody, new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        hideDialog();
                        try {
                            String jsonString = new String(response.data);

                            JSONObject jObj = new JSONObject(jsonString);
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

                        //Toast.makeText(PostFragment.this.getContext(), "Upload successfully!", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideDialog();
                        Toast.makeText(PostFragment.this.getContext(), "Upload failed!\r\n" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                multipartRequest.setRetryPolicy(new DefaultRetryPolicy(1000 * 60, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                /*StringRequest strReqPost = new StringRequest(Request.Method.POST,
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
                        // test
                        Log.d(TAG, "token : " + token);
                        Log.d(TAG, "text : " + text.getText().toString());
                        Log.d(TAG, "latitude : " + Double.toString(latitude));
                        Log.d(TAG, "longitude : " + Double.toString(longitude));
                        Log.d(TAG, "is anonymous : " + Integer.toString(is_anonymous));
                        //

                        // Posting parameters to post url
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("token", token); // token
                        params.put("text", text.getText().toString()); // text
                        params.put("longitude", String.format("%.6f", longitude)); // logitude
                        params.put("latitude", String.format("%.6f", latitude)); // latitude
                        params.put("is_anonymous", Integer.toString(is_anonymous));

                        return params;
                    }

                }; */

                //AppController.getInstance().addToRequestQueue(strReqPost, tag);
                AppController.getInstance().addToRequestQueue(multipartRequest, tag);

            }

        });
        return rootView ;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);
        //dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(parameterValue + lineEnd);
    }

    private void buildTextUTFPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);
        //dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeUTF(parameterValue + lineEnd);
    }

    private void buildPart(DataOutputStream dataOutputStream, byte[] fileData, String fileName) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\""
                + fileName + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
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

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
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

    /*private void previewCapturedImage() {
        try {
            // hide video preview
            //videoPreview.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = calculateInSampleSize(options, 350, 350);

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }*/

    private void previewCapturedImage() {
        try {
            Log.d(TAG, "fileUri  : " + fileUri);
            Log.d(TAG, "path of fileUri  : " + fileUri.getPath());
            Log.d(TAG, "Real chooser path :"+realPath);
            previewImage.setVisibility(View.VISIBLE);
            previewImage.setImageURI(fileUri);
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
        //
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK ) {

            // SDK < API11
            if (Build.VERSION.SDK_INT < 11)
                realPath = getRealPathFromURI_BelowAPI11(getActivity(), data.getData());

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = getRealPathFromURI_API11to18(getActivity(), data.getData());

                // SDK > 19 (Android 4.4)
            else
                realPath = getRealPathFromURI_API19(getActivity(), data.getData());
            fileUri = data.getData();
            previewCapturedImage();
           /* if (resultCode == getActivity().RESULT_OK) {
                // successfully captured the image
                // display it in image view
                //Getting the Bitmap from Gallery
                fileUri = data.getData();
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
        */

        }
    }
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private static Uri getOutputMediaFileUri(int type){
        File fileToReturn =  getOutputMediaFile(type);
        return  fileToReturn!=null?Uri.fromFile(fileToReturn):
                null;
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


class MultipartRequest extends Request<NetworkResponse> {
    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mHeaders;
    private final String mMimeType;
    private final byte[] mMultipartBody;

    public MultipartRequest(String url, Map<String, String> headers, String mimeType, byte[] multipartBody, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mHeaders = headers;
        this.mMimeType = mimeType;
        this.mMultipartBody = multipartBody;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return mMimeType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mMultipartBody;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
}