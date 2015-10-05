package activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.nullnil.shoutout.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CircleOptions;

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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements OnMapClickListener,
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
        {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 30; // 30 sec
    private static final long FASTEST_INTERVAL = 1000 * 5; // 5 sec
    private static final long ONE_MIN = 1000 * 60;
    private static final long REFRESH_TIME = ONE_MIN * 5;
    private static final float MINIMUM_ACCURACY = 50.0f;

    MapView mapView;
    GoogleMap map;
    LatLng position;
    Location location;

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
                .setInterval(INTERVAL)        // 10 seconds, in milliseconds
                .setFastestInterval(FASTEST_INTERVAL); // 1 second, in milliseconds

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

    /*@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }*/

    @Override
    public void onMapClick(LatLng point) {
        float[] distance = new float[2];
        //map.animateCamera(CameraUpdateFactory.newLatLng(point));
        location.distanceBetween(point.latitude, point.longitude, location.getLatitude(), location.getLongitude(), distance);
        if( distance[0] > 1000 ){
            Toast.makeText(this.getContext(), "Outside, distance from center: " + distance[0] + " radius: " + 1000, Toast.LENGTH_LONG).show();
        } else {
            map.addMarker(new MarkerOptions().position(point).title("It's Me!"));
            Toast.makeText(this.getContext(), "Inside, distance from center: " + distance[0] + " radius: " + 1000 , Toast.LENGTH_LONG).show();
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
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,  this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
        super.onResume();
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
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(this);

        /*map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                position = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
                map.animateCamera(cameraUpdate);
                CircleOptions circleOptions = new CircleOptions().center(position).radius(1000).fillColor(Color.argb(4, 0, 255, 0)); // In meters
                map.addCircle(circleOptions);
            Log.d(TAG, "Location: " + "Lat" + location.getLatitude() + "Long" +location.getLongitude() );
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("It's Me!"));
        }
    });*/
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (currentLocation != null && currentLocation.getTime() > REFRESH_TIME) {
            this.location = currentLocation;
        }else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            // Schedule a Thread to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                @Override
                public void run() {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, HomeFragment.this);
                }
            }, ONE_MIN, TimeUnit.MILLISECONDS);
        }
            handleNewLocation(this.location);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        Toast.makeText(this.getContext(),"Location Changed!", Toast.LENGTH_LONG).show();
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        position = new LatLng(currentLatitude, currentLongitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
        map.animateCamera(cameraUpdate);
        CircleOptions circleOptions = new CircleOptions().center(position).radius(1000).fillColor(Color.argb(50, 0, 255, 0)); // In meters
        map.addCircle(circleOptions);
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
        if (null == this.location || location.getAccuracy() < this.location.getAccuracy()) {
            this.location = location;
            if (location.getAccuracy() < MINIMUM_ACCURACY) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        }
        handleNewLocation(this.location);
    }
}
