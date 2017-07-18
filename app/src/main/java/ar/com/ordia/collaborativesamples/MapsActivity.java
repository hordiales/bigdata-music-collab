package ar.com.ordia.collaborativesamples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private LatLng position;
    private String title;

    private static final String LOGTAG = "maps";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);

        Log.d(LOGTAG, "Lat: "+latitude+" , Long: "+longitude);
        position = new LatLng(longitude, latitude);
        this.title = intent.getStringExtra("title");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(this.position);
        markerOptions.title(this.title);
        //markerOptions.snippet("Ejemplo notas");

        Marker marker = mMap.addMarker(markerOptions);
        //marker.setTag("123456");
        //marker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(this.position));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, "Id: " + marker.getTag().toString(), Toast.LENGTH_LONG).show();
        marker.showInfoWindow();
        return true;
    }

}
