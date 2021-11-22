package com.example.maplogin.utils;

import android.app.Activity;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.maplogin.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class RoutingController implements RoutingListener {

    public static final int POLYLINE_WIDTH = 7;

    private final Activity mActivity;
    private final GoogleMap mMap;

    private ArrayList<Polyline> polylines;

    public RoutingController (Activity activity, GoogleMap map) {
        mActivity = activity;
        mMap = map;
    }

    // find routes from startLatLng to endLatLng with specific travelMode.
    public void findRoutes(LatLng startLatLng, LatLng endLatLng, AbstractRouting.TravelMode travelMode)
    {
        if(startLatLng==null || endLatLng==null)
            Toast.makeText(mActivity,"Unable to get location",Toast.LENGTH_LONG).show();

        else {
            Routing routing = new Routing.Builder()
                    .travelMode(travelMode)
                    .withListener(this)
//                    .alternativeRoutes(true)
                    .waypoints(startLatLng, endLatLng)
                    .key("AIzaSyCQjSbW4ANku5u4VMlkIWtpp4m6yTi4EPA")
                    .build();
            routing.execute();
        }
    }

    /***** start of routing call back functions *****/
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        clearRoutesUI();

        polylines = new ArrayList<>();

        // add route(s) to the map using polyline
        int color;
        for (int i = 0; i < route.size(); i++) {
            if (i == shortestRouteIndex)
                color = ContextCompat.getColor(mActivity, R.color.red);
            else
                color = ContextCompat.getColor(mActivity, R.color.red_transparent);
            addRouteToMap(route.get(i).getPoints(), color);
        }
    }

    private void addRouteToMap(List<LatLng> points, int color) {
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(color);
        polyOptions.width(POLYLINE_WIDTH);
        polyOptions.addAll(points);
        Polyline polyline = mMap.addPolyline(polyOptions);
        polylines.add(polyline);
    }

    private void clearRoutesUI() {
        if (polylines != null) {
            for (int i = 0; i < polylines.size(); ++i)
                polylines.get(i).remove();
            polylines.clear();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(mActivity,e.getMessage(),Toast.LENGTH_LONG).show();
    }
    @Override
    public void onRoutingStart() {
    }
    @Override
    public void onRoutingCancelled() {
    }
    /***** end of routing call back functions *****/
}
