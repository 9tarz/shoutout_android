package activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


import com.example.nullnil.shoutout.R;

public class PostFragment extends Fragment {

    double[] latlong;
    double lat;
    double longi;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        latlong = bundle.getDoubleArray("picklatlong");
        // in case you want not array value
        lat = latlong[0];
        longi = latlong[1];
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View myFragmentView = inflater.inflate(R.layout.fragment_post, container, false);

        TextView latitude = (TextView)myFragmentView.findViewById(R.id.latitude);
        TextView longitude = (TextView)myFragmentView.findViewById(R.id.longtidude);
        latitude.setText("latitude"+Double.toString(lat));
        longitude.setText("longitude"+Double.toString(longi));

        return myFragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
