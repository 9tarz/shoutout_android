package activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.nullnil.shoutout.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Circle;
import android.support.v4.app.FragmentTransaction;

import android.location.Location;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import android.graphics.Color;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import android.content.IntentSender;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.maps.LocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.AppConfig;
import app.AppController;

public class HomeFragment extends Fragment implements OnMapClickListener,
        OnMapReadyCallback,
        LocationListener,
        LocationSource,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener
        {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000;
    private static final long FASTEST_INTERVAL = 1000;
    private static final float MINIMUM_ACCURACY = 50.0f;
    private static final float RADIUS = 1000;

    private MapView mapView;
    private GoogleMap map;
    private LatLng position;
    private Location location;
    private Circle circleMap;
    private OnLocationChangedListener mapLocationListener = null;

            public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) rootView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);

       try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setLocationSource(this);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(this);
        map.moveCamera(CameraUpdateFactory.zoomTo(15f));
        map.setOnMarkerClickListener(this);
    }


    @Override
    public void onMapClick(LatLng point) {
        float[] distance = new float[2];
        //map.animateCamera(CameraUpdateFactory.newLatLng(point));
        location.distanceBetween(point.latitude, point.longitude, location.getLatitude(), location.getLongitude(), distance);
        if( distance[0] > RADIUS ){
            Toast.makeText(this.getContext(), "Outside, distance from center: " + distance[0] + " radius: " + RADIUS, Toast.LENGTH_LONG).show();
        } else {
            map.addMarker(new MarkerOptions().position(point).title("It's Me!"));
            Toast.makeText(this.getContext(), "Inside, distance from center: " + distance[0] + " radius: " + RADIUS , Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        mapView.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            this.location = lastLocation;
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        if(circleMap != null) {
            circleMap.remove();
        }
        handleNewLocation(this.location);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }


    private void handleNewLocation(Location location) {
        if (location != null ) {
            //Toast.makeText(this.getContext(),"Time:"+ location.getTime()+ " Provider:"+ location.getProvider() + " Accuracy:" + location.getAccuracy(), Toast.LENGTH_LONG).show();
            Log.d(TAG, location.toString());
            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();
            Toast.makeText(this.getContext(), "Location Changed! Lat:" + currentLatitude + " Long:" +currentLongitude, Toast.LENGTH_LONG).show();
            //addCircleAndMoveCamera(currentLatitude, currentLongitude);
            pullAroundLocation(currentLatitude, currentLongitude);
        }
    }

    private void addCircleAndMoveCamera(double currentLatitude, double currentLongitude) {
        position = new LatLng(currentLatitude, currentLongitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
        map.animateCamera(cameraUpdate);
        CircleOptions circleOptions = new CircleOptions().center(position).radius(RADIUS).fillColor(Color.argb(50, 0, 255, 0)).strokeWidth(0f); // In meters
        circleMap = map.addCircle(circleOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(position));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this.getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (this.mapLocationListener != null) {
            this.mapLocationListener.onLocationChanged(location);
        }
        this.location = location;
        handleNewLocation(this.location);
        /*if (null == this.location || location.getAccuracy() < this.location.getAccuracy()) {
            this.location = location;
            circleMap.remove();
            Toast.makeText(this.getContext(),":Provider:"+ location.getProvider() + "Accuracy:" + location.getAccuracy(), Toast.LENGTH_LONG).show();
            handleNewLocation(this.location);
            if (location.getAccuracy() < MINIMUM_ACCURACY) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        }*/

        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(this.location.getLatitude(), this.location.getLongitude())));
    }
    @Override
    public void activate(OnLocationChangedListener listener) {
        this.mapLocationListener = listener;
    }

    @Override
    public void deactivate() {
        this.mapLocationListener=null;
    }

    private void pullAroundLocation(final double latitude, final double longitude) {
        map.clear();
        // Tag used to cancel the request
        String tag_string_req = "req_pulllocation";

        String stringLatitude = String.valueOf(latitude);
        String stringLongitude = String.valueOf(longitude);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_GETAROUNDLOCATION + "/" + stringLatitude + "/" + stringLongitude, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "pullLocation Response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    int intError = jObj.getInt("error");
                    boolean error = (intError > 0) ? true : false;
                    // Check for error node in json
                    if (!error) {
                        JSONArray arr_post = jObj.getJSONArray("posts");
                        addCircleAndMoveCamera(latitude, longitude);
                        for (int i = 0; i < arr_post.length(); i++) {
                            String jsonLatitude = arr_post.getJSONObject(i).getString("latitude");
                            String jsonLongitude = arr_post.getJSONObject(i).getString("longitude");
                            map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(jsonLatitude), Double.parseDouble(jsonLongitude)))
                                    .title("LAT: " + jsonLatitude + " LONG:" + jsonLongitude));
                            Log.i(TAG, "Lat:" + jsonLatitude + " Long: " + jsonLongitude);
                        }
                    } else {
                        addCircleAndMoveCamera(latitude, longitude);
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(HomeFragment.this.getContext(), errorMsg , Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(HomeFragment.this.getContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "pullLocation Error: " + error.getMessage());
                Toast.makeText(HomeFragment.this.getContext(),
                       error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Fragment newFragment = new TimeLineFragment();
        Bundle bundle = new Bundle();
        LatLng pickLatLng =  marker.getPosition();
        double[] LatLong = {pickLatLng.latitude,pickLatLng.longitude};
        bundle.putDoubleArray("pickLatLng", LatLong);
        newFragment.setArguments(bundle);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // and add the transaction to the back stack
        transaction.replace(R.id.container_body, newFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();

        //move camera to marker
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pickLatLng, 15);
        map.animateCamera(cameraUpdate);
        return true;
    }
}
