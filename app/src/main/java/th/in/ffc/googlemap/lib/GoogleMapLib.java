package th.in.ffc.googlemap.lib;

import android.app.Activity;
import android.graphics.Color;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class GoogleMapLib {
    private GoogleMap myMap;
    private HashMap<Marker, Integer> hashMarker;
    private ArrayList<LatLng> pointPolygon;
    private Polygon polygonOnMap;
    private Circle circleMap;
    private Marker edgeMarkerCircle;
    private double radius;
    private boolean drawPolygon;

    public GoogleMapLib(Activity activity, int MapViewId) {
        FragmentManager myFragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
        SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
                .findFragmentById(MapViewId);
        drawPolygon = false;
        pointPolygon = new ArrayList<>();
        mySupportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override public void onMapReady(GoogleMap googleMap) {
                myMap = googleMap;
                LatLng latlng = new LatLng(13.822578, 100.514233);
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 8.0f));
                myMap.setOnMapClickListener(myMapClick);
                myMap.setOnMapLongClickListener(myMapLongClick);
                myMap.setOnMarkerDragListener(markerDrag);
            }
        });
        hashMarker = new HashMap<>();
    }

    private void createPolygon() {
        if (pointPolygon.size() > 1) {
            if (polygonOnMap != null) {
                polygonOnMap.remove();
            }
            LatLng latlng[] = new LatLng[pointPolygon.size()];
            latlng = pointPolygon.toArray(latlng);
            polygonOnMap = myMap.addPolygon(new PolygonOptions().add(latlng)
                    .fillColor(Color.argb(150, 255, 255, 255)));
            polygonOnMap.setStrokeColor(Color.RED);

        }
    }

    private void createCircle() {
        if (pointPolygon.size() > 0) {
            if (circleMap != null) {
                circleMap.remove();
            }
            circleMap = myMap.addCircle(new CircleOptions()
                    .center(pointPolygon.get(0)).radius(radius)
                    .strokeColor(Color.RED).fillColor(Color.argb(150, 255, 255, 255)));
        }
    }

    private double havrsineFormula(LatLng point1, LatLng point2) {
        double lat1 = point1.latitude;
        double lat2 = point2.latitude;
        double lon1 = point1.longitude;
        double lon2 = point2.longitude;
        double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;

        return d * 1000.0;
    }

    private LatLng getCircleEdge(double d, double lat, double lon) {
        d /= 1000.0;
        double brng = 0.0;
        double R = 6371.0;
        double lat1 = Math.toRadians(lat);
        double lon1 = Math.toRadians(lon);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R)
                + Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));
        double lon2 = lon1
                + Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1),
                Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));
        return new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }

    public void clear() {
        myMap.clear();
        pointPolygon.clear();
        hashMarker.clear();
    }

    public void drawPolygon() {
        drawPolygon = true;
    }

    public void drawCircle() {
        drawPolygon = false;
    }

    public void setRadius(int radius) {

    }

    OnMapClickListener myMapClick = new OnMapClickListener() {

        @Override
        public void onMapClick(LatLng positionClick) {
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    positionClick, 8.0f));
        }
    };

    OnMapLongClickListener myMapLongClick = new OnMapLongClickListener() {

        @Override
        public void onMapLongClick(LatLng positionLongClick) {
            MarkerOptions marker = new MarkerOptions();
            marker.draggable(true);
            marker.position(positionLongClick);
            if (drawPolygon) {
                pointPolygon.add(positionLongClick);
                hashMarker.put(myMap.addMarker(marker), pointPolygon.size() - 1);
                createPolygon();
            } else {
                myMap.clear();
                hashMarker.clear();
                pointPolygon.clear();
                radius = 50000;
                LatLng edgePosition = getCircleEdge(radius,
                        positionLongClick.latitude, positionLongClick.longitude);
                MarkerOptions temp = new MarkerOptions();
                temp.draggable(true);
                temp.position(edgePosition);
                edgeMarkerCircle = myMap.addMarker(temp);
                pointPolygon.add(0, positionLongClick);
                hashMarker.put(myMap.addMarker(marker), pointPolygon.size() - 1);
                pointPolygon.add(1, edgePosition);
                hashMarker.put(edgeMarkerCircle, pointPolygon.size() - 1);
                createCircle();
            }

        }

    };


    OnMarkerDragListener markerDrag = new OnMarkerDragListener() {
        @Override
        public void onMarkerDrag(Marker marker) {
            pointPolygon.set(hashMarker.get(marker), marker.getPosition());
            if (drawPolygon) {
                createPolygon();
            } else {
                if (hashMarker.get(marker) == 0) {
                    radius = 50000;
                    LatLng edgePosition = getCircleEdge(radius,
                            marker.getPosition().latitude, marker.getPosition().longitude);
                    edgeMarkerCircle.setPosition(edgePosition);
                    pointPolygon.add(0, marker.getPosition());
                    pointPolygon.add(1, edgePosition);
                    createCircle();
                } else {
                    radius = havrsineFormula(pointPolygon.get(0), marker.getPosition());
                    createCircle();
                }
            }

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onMarkerDragStart(Marker marker) {
            // TODO Auto-generated method stub

        }

    };

    public double getRadius() {
        return radius;
    }

    public LatLng getPositionCircle() {
        if (pointPolygon != null) {
            return pointPolygon.get(0);
        }
        return null;
    }
}

