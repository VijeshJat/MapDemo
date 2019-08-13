package jat.vijesh.mapdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.maps.model.JointType.ROUND;


/**
 * reference -  https://github.com/amalChandran/trail-android
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Bitmap smallMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        smallMarker = Bitmap.createScaledBitmap((((BitmapDrawable) getResources().getDrawable(R.drawable.ic_car)).getBitmap()), 48, 48, false);

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    int currentPt = 0;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        directionPoint.add(new LatLng(22.748566, 75.894722));
        directionPoint.add(new LatLng(22.751749, 75.895564));
        directionPoint.add(new LatLng(22.755202, 75.896697));
        directionPoint.add(new LatLng(22.753590, 75.903603));
        directionPoint.add(new LatLng(22.749502, 75.903500));
        directionPoint.add(new LatLng(22.750967, 75.895898));


        if (marker == null) {
            marker = mMap.addMarker(new MarkerOptions().position(directionPoint.get(currentPt)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(currentPt), 14.0f));
        }

        drawPolyLineAndAnimateCar();

    }


    Handler hasndler = new Handler();
    private Marker marker;
    private List<LatLng> directionPoint = new ArrayList<>();


    //Method for finding bearing between two points
    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    private PolylineOptions polylineOptions;
    List<LatLng> list = new ArrayList<>();
    Polyline polyline;

    public void drawPolyLineOnMap() {

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);
        polylineOptions.addAll(list);
        polyline = mMap.addPolyline(polylineOptions);

    }


    private float v;
    private double lat, lng;
    private LatLng startPosition, endPosition;
    private int index, next;

    private void drawPolyLineAndAnimateCar() {
        //Adjusting bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : directionPoint) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
        mMap.animateCamera(mCameraUpdate);

        drawPolyLineOnMap();

        mMap.addMarker(new MarkerOptions().position(directionPoint.get(directionPoint.size() - 1)));

        marker = mMap.addMarker(new MarkerOptions().position(directionPoint.get(0))
                .flat(true)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        hasndler = new Handler();
        index = -1;
        next = 1;
        hasndler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index < directionPoint.size() - 1) {
                    index++;
                    next = index + 1;
                }
                if (index < directionPoint.size() - 1) {
                    startPosition = directionPoint.get(index);
                    endPosition = directionPoint.get(next);
                }

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(3000);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        v = valueAnimator.getAnimatedFraction();
                        lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                        lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                        LatLng newPos = new LatLng(lat, lng);
                        marker.setPosition(newPos);
                        marker.setAnchor(0.5f, 0.5f);
                        marker.setRotation(getBearing(startPosition, newPos));
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(newPos).zoom(15.5f).build()));

                        List<LatLng> points = polyline.getPoints();
                        points.add(newPos);
                        polyline.setPoints(points);
                    }
                });

                valueAnimator.start();


                if (index != directionPoint.size() - 1) {
                    hasndler.postDelayed(this, 3000);
                }
            }
        }, 3000);
    }
}
