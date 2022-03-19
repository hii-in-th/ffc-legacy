package th.in.ffc.googlemap.lib;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.content.PermissionChecker;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class MapPolygonLib {
    private double radius;
    private boolean areaMode;
    private boolean editMode;
    private GoogleMap myMap;
    private ArrayList<LatLng> point;
    private boolean markerClicked;
    private Marker edge;
    private Circle circle;
    private HashMap<Marker, Integer> markerMap;
    private Polygon polygonOnMap = null;

    OnMapClickListener mMapClick = new OnMapClickListener() {

        @Override public void onMapClick(LatLng point) {
            myMap.animateCamera(CameraUpdateFactory.newLatLng(point));
            myMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
            markerClicked = false;
        }
    };

    OnMarkerDragListener myMapDrag = new OnMarkerDragListener() {
        @Override public void onMarkerDrag(Marker marker) {
            if (editMode) {
                if (areaMode) {
                    point.set(markerMap.get(marker), marker.getPosition());
                    addPolygon(point);
                } else {
                    if (markerMap.get(marker) == 1) {
                        // Drag edge marker
                        radius = havrsineFormula(point.get(0), marker.getPosition());
                        circle.setRadius(radius);
                    } else {
                        Log.d("panda", "hello");
                        // myMap.clear();
                        // Drag center marker
                        point.set(0, marker.getPosition());
                        // edgeMarker = new MarkerOptions();
                        edge.setPosition(getCircleEdge(radius, marker.getPosition().latitude, marker.getPosition().longitude));
                        addCircle(point);
                    }
                }
            }
        }

        @Override public void onMarkerDragEnd(Marker marker) {

        }

        @Override public void onMarkerDragStart(Marker marker) {

        }
    };
    OnMapLongClickListener myMapLongClick = new OnMapLongClickListener() {

        @Override public void onMapLongClick(LatLng setMarkerPoint) {
            if (editMode) {
                if (areaMode) {
                    MarkerOptions mm = new MarkerOptions();
                    mm.position(setMarkerPoint).draggable(true);
                    markerMap.put(myMap.addMarker(mm), point.size());
                    point.add(setMarkerPoint);
                    addPolygon(point);
                    markerClicked = false;
                } else {
                    Log.d("panda", "create");
                    myMap.clear();
                    point = new ArrayList<LatLng>();
                    MarkerOptions mm = new MarkerOptions();
                    mm.position(setMarkerPoint).draggable(true);
                    MarkerOptions edgeMarker = new MarkerOptions();
                    edgeMarker.position(getCircleEdge(radius, setMarkerPoint.latitude, setMarkerPoint.longitude)).draggable(true);
                    edge = myMap.addMarker(edgeMarker);
                    markerMap.put(edge, 1);
                    markerMap.put(myMap.addMarker(mm), 0);
                    point.add(setMarkerPoint);
                    addCircle(point);
                    markerClicked = false;
                }
            }
        }
    };

    public MapPolygonLib(final Activity activity, int MapViewId) {
        radius = 100;
        areaMode = true;
        editMode = false;
        point = new ArrayList<LatLng>();
        markerMap = new HashMap<Marker, Integer>();
        FragmentManager myFragmentManager = activity.getFragmentManager();
        MapFragment myMapFragment = (MapFragment) myFragmentManager.findFragmentById(MapViewId);
        myMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override public void onMapReady(GoogleMap googleMap) {
                myMap = googleMap;
                if (PermissionChecker.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && PermissionChecker.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    myMap.setMyLocationEnabled(true);
                }
                myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                myMap.setOnMapClickListener(mMapClick);
                myMap.setOnMapLongClickListener(myMapLongClick);
                myMap.setOnMarkerDragListener(myMapDrag);
            }
        });

        markerClicked = false;
    }

    // addPolygon onMap
    public void addPolygon(ArrayList<LatLng> position) {
        if (position.size() != 1) {
            if (polygonOnMap != null) {
                polygonOnMap.remove();
            }
            LatLng latlng[] = new LatLng[position.size()];
            latlng = position.toArray(latlng);
            polygonOnMap = myMap.addPolygon(new PolygonOptions().add(latlng)
                    .fillColor(Color.argb(150, 255, 255, 255)));
            polygonOnMap.setStrokeColor(Color.RED);
        }

    }

    public void addCircle(ArrayList<LatLng> position) {
        if (position.size() == 1) {
            if (circle != null) {
                circle.remove();
            }
            circle = myMap.addCircle(new CircleOptions()
                    .center(position.get(0)).radius(radius)
                    .strokeColor(Color.RED).fillColor(Color.BLUE));
        }
    }

    public void clearMap() {
        myMap.clear();
        point.clear();
    }

    public void setFillColor(int color) {
        polygonOnMap.setFillColor(color);
    }

    public void setStoke(int color) {
        polygonOnMap.setStrokeColor(color);
    }

    public void startEditMode() {
        editMode = true;
    }

    public void stopEditMode() {
        editMode = false;
    }

    public void saveInstanceState(Bundle state) {
        if (state != null) {
            state.putParcelableArrayList("point", point);
            state.putDouble("radius", radius);
            state.putBoolean("editMode", editMode);
            state.putBoolean("areaMode", areaMode);
            state.putBoolean("markerClicked", markerClicked);

        }
    }

    public void restoreInstanceState(Bundle state) {
        if (state != null) {
            markerMap.clear();
            editMode = state.getBoolean("editMode");
            areaMode = state.getBoolean("areaMode");
            markerClicked = state.getBoolean("markerClicked");
            radius = state.getDouble("radius");
            point = state.getParcelableArrayList("point");
            try {
                if (!areaMode) {
                    myMap.clear();
                    MarkerOptions mm = new MarkerOptions();
                    mm.position(point.get(0)).draggable(true);
                    MarkerOptions edgeMarker = new MarkerOptions();
                    edgeMarker.position(
                            getCircleEdge(radius, point.get(0).latitude,
                                    point.get(0).longitude)).draggable(true);
                    edge = myMap.addMarker(edgeMarker);
                    markerMap.put(edge, 1);
                    markerMap.put(myMap.addMarker(mm), 0);
                    addCircle(point);
                    markerClicked = false;
                } else {
                    for (LatLng eachPoint : point) {
                        MarkerOptions mm = new MarkerOptions();
                        mm.position(eachPoint).draggable(true);
                        markerMap.put(myMap.addMarker(mm), point.size());
                    }
                    addPolygon(point);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public void setPolgonMode() {
        areaMode = true;
        clearMap();
    }

    public void setCircleMode() {
        areaMode = false;
        clearMap();
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

    public LatLng getCirclePoint() {
        return point.get(0);
    }

    public double getCircleRadius() {
        return radius;
    }

    public ArrayList<LatLng> getPolygonPoint() {
        return point;
    }

}
