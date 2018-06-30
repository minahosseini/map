package ir.sharif.rahpaapp.MapMVP;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.ViewById;
import org.w3c.dom.Document;

import ir.sharif.rahpaapp.MapMVP.Detail.GMapV2Direction;
import ir.sharif.rahpaapp.MapMVP.Detail.GMapV2DirectionAsyncTask;
import ir.sharif.rahpaapp.MapMVP.Detail.LocationService_;
import ir.sharif.rahpaapp.R;

@EActivity
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener,
        MapsContract.View {
    public static Resources resources;
    double lat, lng, latitude, longitude;
    private GoogleMap mMap;
    private LatLng  origin, destination;
    boolean setOrigin, setDestination, locationPermission = false;
    private static final int REQUEST_PERMISSIONS = 100;
    Geocoder geocoder;

    @ViewById
    ImageView imgLocation;
    @FragmentById
    SupportMapFragment mapFragment;
    private MapsPresenter presenter;
    private GoogleMap googleMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        init();

        presenter = new MapsPresenter();
        presenter.attachView(this);
        presenter.requestPermissions();
    }

    private void init() {
        Toast.makeText(this, "Please connect to the internet and enable GPS", Toast.LENGTH_SHORT).show();
        mapFragment.getMapAsync(this);
        resources = this.getResources();
        geocoder = new Geocoder(this, Locale.getDefault());
    }

    @Override
    public void checkPermission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == false)) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS);
        } else {
            locationPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.onPermissionsResult(true);
                } else {
                    presenter.onPermissionsResult(false);
                }
            }
        }
    }

    @Override
    public void permissionGranted() {
        locationPermission = true;
    }

    @Override
    public void permissionNotGranted() {
        Toast.makeText(getApplicationContext(), "Please Clear data and cache and restart the app and allow the permission", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCameraIdle() {
        lat = mMap.getCameraPosition().target.latitude;
        lng = mMap.getCameraPosition().target.longitude;

        presenter.getCompleteAddress(this,lat,lng);

        //ToDo
        if (setOrigin & setDestination) {
            route(origin, destination, GMapV2DirectionAsyncTask.MODE_WALKING);
        }
    }


    @Click
    void imgLocation() {
        presenter.setMarker(setOrigin,setDestination);
    }

    @Override
    public void setOriginMarker() {
        origin = new LatLng(lat, lng);
        LatLng defaultDestination = new LatLng(lat + 0.001, lng + 0.001);
        mMap.addMarker(new MarkerOptions().position(origin).title("origin")
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_origin_marker))));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultDestination));
        imgLocation.setImageDrawable(resources.getDrawable(R.drawable.ic_destination_marker));
        setOrigin = true;
    }

    @Override
    public void setDestinationMarker() {
        destination = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(destination).title("destination")
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_destination_marker))));
        imgLocation.setVisibility(View.GONE);
        setDestination = true;
        route(origin, destination, GMapV2DirectionAsyncTask.MODE_WALKING);
        startLocationService();
    }

    @Override
    public void showCompleteAddress(String address) {
        Toast.makeText(MapsActivity.this, address, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        presenter.getDefaultLocation();
        mMap.setOnCameraIdleListener(this);
    }
    @Override
    public void setDefaultLocation(double lat,double lng) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        imgLocation.setImageDrawable(resources.getDrawable(R.drawable.ic_origin_marker));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng), 12));
    }


    public void route(LatLng sourcePosition, LatLng destPosition, String mode) {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    Document doc = (Document) msg.obj;
                    GMapV2Direction md = new GMapV2Direction();
                    ArrayList<LatLng> directionPoint = md.getDirection(doc);
                    PolylineOptions rectLine = new PolylineOptions().width(15).color(getApplicationContext().getResources().getColor(R.color.colorAccent));

                    for (int i = 0; i < directionPoint.size(); i++) {
                        rectLine.add(directionPoint.get(i));
                    }
                    mMap.addPolyline(rectLine);
                    md.getDurationText(doc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };

        new GMapV2DirectionAsyncTask(handler, sourcePosition, destPosition, mode).execute();
    }

    private Bitmap getMarkerBitmapFromView(int resId) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = customMarkerView.findViewById(R.id.imgLocation);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private void startLocationService() {
        if (locationPermission) {
            LocationService_.intent(this).start();
            Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            latitude = Double.valueOf(intent.getStringExtra("latutide"));
            longitude = Double.valueOf(intent.getStringExtra("longitude"));

            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                Log.e("MapsActivity", "onReceive: cityName " + addresses.get(0).getAddressLine(0)
                        + " stateName " + addresses.get(0).getAddressLine(1)
                        + " countryName " + addresses.get(0).getAddressLine(2));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(LocationService_.str_receiver));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getApplicationContext(), LocationService_.class));
    }

    @Override
    public void setLanguage() {
        String languageToLoad = "fa_";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

}
