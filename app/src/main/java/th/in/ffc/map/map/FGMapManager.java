package th.in.ffc.map.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
//import org.osmdroid.google.wrapper.MyLocationOverlay;
//import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import th.in.ffc.R;
import th.in.ffc.map.FGActivity;
import th.in.ffc.map.MapFragment;
import th.in.ffc.map.ResourceProxyImpl;
import th.in.ffc.map.UserResourceProxyImpl;
import th.in.ffc.map.overlay.FGOverlay;
import th.in.ffc.map.overlay.ItemGestureListener;
import th.in.ffc.map.system.FGSystemManager;
import th.in.ffc.map.value.FinalValue;
import th.in.ffc.map.value.MARKER_TYPE;
import th.in.ffc.map.village.spot.Spot;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FGMapManager implements OnClickListener {

    private FGSystemManager fgSystemManager;

    private MapView mapView;
    private IMapController mapController;
    private MyLocationNewOverlay mLocation;
    private ItemGestureListener gesture;
    private ItemizedIconOverlay<Spot> markers = null;

    private MapTileProviderBasic tileProvider;

    private OnlineTileSourceBase sourceBase;

    private FGOverlay emptyOverlay;

    private TilesOverlay tilesOverlay;

    private ArrayList<String> sat_url;

    private ArrayList<String> hybrid_url;

    private ArrayList<String> maps_url;

    private static int currentMapStyle = -1;

    private static boolean[] group_check;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    public FGMapManager(FGSystemManager fgSystemManager) {
        this.fgSystemManager = fgSystemManager;

        group_check = new boolean[MARKER_TYPE.size];
        Arrays.fill(group_check, true);

        FGActivity fgActivity = this.fgSystemManager.getFGActivity();
        MapFragment mf = (MapFragment) fgActivity.getSupportFragmentManager().findFragmentById(R.id.map_fragment_id);
        this.mapView = (MapView) mf.getView().findViewById(R.id.mapview);
        this.gesture = new ItemGestureListener(fgSystemManager);
        // Download Google API
        requirePermission();
       InputStream target = null;

       File previous = this.fgSystemManager.getFGActivity().getFileStreamPath("map-loaded");
       String filename_current = downloadFile();

       if (filename_current != null) {
           File current = this.fgSystemManager.getFGActivity().getFileStreamPath(filename_current);
           previous.delete();
           current.renameTo(previous);

           Log.d("TAG!", "Download file is completed");
           try {
               target = new FileInputStream(previous);
           } catch (FileNotFoundException e) {
               e.printStackTrace();
           }
       } else if (previous.exists()) {

           Log.d("TAG!", "Download file is NOT completed, use the previous one");
           try {
               target = new FileInputStream(previous);
           } catch (FileNotFoundException e) {
               e.printStackTrace();
           }
       } else {

           Log.d("TAG!", "No previous file, cannot download, fall back! fall back!");
           try {
               target = this.fgSystemManager.getFGActivity().getAssets().open("maps-fallback");
           } catch (IOException e) {
               e.printStackTrace();
           }
       }

       this.tokenize(target);

        // ----

//        markers = new ItemizedIconOverlay<Spot>(new ArrayList<Spot>(), this.gesture, new ResourceProxyImpl(fgActivity.getApplicationContext()));
        markers = new ItemizedIconOverlay<Spot>(new ArrayList<Spot>(), this.gesture, this.mapView.getContext());

        this.emptyOverlay = new FGOverlay(fgSystemManager);


        this.mapController = this.mapView.getController();
//        Context ctx = this.getMapView().getContext();
//        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setMapStyle(FinalValue.INT_SATELLITE_OVERLAY);

        this.mapView.setUseDataConnection(true);

        this.initialButtonZoomControl();

        this.initializeCurrentLocation();

        //this.initialImageButtonMenu();

        checkGPS();
    }

    public void checkGPS() {

        LocationManager lm = fgSystemManager.getFGGPSManager().getLocationManager();

        Resources res = fgSystemManager.getFGActivity().getResources();
        String str = res.getString(R.string.gps_text);

        Message msg = new Message();
        msg.what = FGActivity.UPDATE_GPS_ICON;
        if (lm.isProviderEnabled("gps")) {
            msg.arg1 = R.drawable.gps1;
            msg.obj = str + "GPS";
        } else if (mapView.useDataConnection())
            if (lm.isProviderEnabled("network")) {
                msg.arg1 = R.drawable.gps3;
                msg.obj = str + "AGPS";
            } else {
                msg.arg1 = R.drawable.gps2;
                msg.obj = str + "Off";
            }
        else {
            msg.arg1 = R.drawable.gps2;
            msg.obj = str + "Off";
        }

        fgSystemManager.getFGActivity().getHandler().sendMessage(msg);
    }

    public void setMapStyle(int newStyle) {

        if (currentMapStyle == newStyle)
            return;

        this.clearCahce();
        List<Overlay> stackOverlay = this.mapView.getOverlays();
//        stackOverlay.clear();
        Context ctx = this.getMapView().getContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        switch (newStyle) {
            case FinalValue.INT_SATELLITE:
//                mapView.setTileSource(this.initializeSatellite());
                break;
            case FinalValue.INT_SATELLITE_OVERLAY:
//                mapView.setTileSource(this.initializeSatellite());
                break;
            case FinalValue.INT_GOOGLE_MAPS:
//                mapView.setTileSource(initializeGoogleMaps());
                break;
        }

        stackOverlay.add(emptyOverlay);
        stackOverlay.add(markers);
        // TODO Change to Google Map
/*        if (mLocation != null)
            stackOverlay.add(mLocation);*/

        currentMapStyle = newStyle;
    }

    public static int getCurrentMapStyle() {
        return currentMapStyle;
    }
    private void requirePermission(){
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET
        });
    }
    private OnlineTileSourceBase initializeSatellite() {
        if (sourceBase == null || (!sourceBase.name().equals("Google_Satellite"))) {
            sourceBase = new OnlineTileSourceBase("Google_Satellite", 1, 19, 256, ".jpg",
                    sat_url.toArray(new String[sat_url.size()])) {
                @Override
                public String getTileURLString(long l) {
                    return null;
                }
//                @Override
//                public String getTileURLString(final MapTile aTile) {
//                    return getBaseUrl() + "x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
//                }
            };
        }
        return sourceBase;
    }
    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getMapView().getContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(fgSystemManager.getFGActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
    private OnlineTileSourceBase initializeGoogleMaps() {
       if (sourceBase == null || (!sourceBase.name().equals("Google_Maps"))) {
           sourceBase = new OnlineTileSourceBase("Google_Maps", 1, 20, 256, ".jpg",
                   maps_url.toArray(new String[maps_url.size()])) {
               @Override
               public String getTileURLString(long pMapTileIndex) {
                   return null;
               }
//               @Override
//               public String getTileURLString(final MapTile aTile) {
//                   return getBaseUrl() + "x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
//               }
           };
       }
        return sourceBase;
    }

    private TilesOverlay initializeGoogleOverlay() {
        if (tileProvider == null) {
            tileProvider = new MapTileProviderBasic(this.fgSystemManager.getFGActivity());
           final ITileSource tileSource = new OnlineTileSourceBase("Google_Hybrid", 1, 19, 256, ".png",
                   hybrid_url.toArray(new String[hybrid_url.size()])) {
               @Override
               public String getTileURLString(long pMapTileIndex) {
                   return null;
               }
//               @Override
//               public String getTileURLString(final MapTile aTile) {
//
//                   return getBaseUrl() + "x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
//               }
           };
           tileProvider.setTileSource(tileSource);
        }
        if (tilesOverlay == null) {
            tilesOverlay = new TilesOverlay(tileProvider, this.fgSystemManager.getFGActivity());
            tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        }
        return tilesOverlay;
    }

    // private void initialImageButtonMenu() {
    // FGActivity fgActivity = this.fgSystemManager.getFGActivity();
    //
    // this.initialImageButtonSearch(fgActivity);
    // this.initialImageButtonGPS(fgActivity);
    // this.initialImageButtonNetwork(fgActivity);
    // this.initialImageButtonMyLocation(fgActivity);
    // this.initializeImageButtonFilter(fgActivity);
    // this.initialImageButtonMapStyle(fgActivity);
    // }

    // private void initialImageButtonUser(FGActivity fgActivity){
    // ImageButton imageButtonUser =
    // (ImageButton)fgActivity.findViewById(R.id.imagebutton_center);
    // imageButtonUser.setOnClickListener(new View.OnClickListener() {
    // @Override
    // public void onClick(View v) {
    //
    // }
    // });
    // }

    // private void initialImageButtonMyLocation(FGActivity fgActivity) {
    // ImageButton imgMyLocation = (ImageButton)
    // fgActivity.findViewById(R.id.imagebutton_center);
    // imgMyLocation.setOnClickListener(this);
    // }
    //
    // private void initialImageButtonSearch(FGActivity fgActivity) {
    // ImageButton imageButtonSearch = (ImageButton)
    // fgActivity.findViewById(R.id.imagebutton_search);
    // imageButtonSearch.setOnClickListener(this);
    // }
    //
    // private void initializeImageButtonFilter(FGActivity fgActivity) {
    // ImageButton imageButtonFilter = (ImageButton)
    // fgActivity.findViewById(R.id.imagebutton_filter);
    // imageButtonFilter.setOnClickListener(this);
    // }
    //
    // private void initialImageButtonMapStyle(FGActivity fgActivity) {
    // ImageButton imageButtonMapStyle = (ImageButton)
    // fgActivity.findViewById(R.id.imagebutton_mapstyle);
    // imageButtonMapStyle.setOnClickListener(this);
    // }

    // private void initialImageButtonBack(final FGActivity fgActivity){
    // ImageButton imageButtonBack =
    // (ImageButton)fgActivity.findViewById(R.id.imagebutton_back);
    // imageButtonBack.setOnClickListener(new View.OnClickListener() {
    // @Override
    // public void onClick(View v) {
    // Intent intent = new Intent(fgActivity, MainPage.class);
    // intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    // fgActivity.startActivity(intent);
    // }
    // });
    // }

    // private void initialImageButtonGPS(final FGActivity fgActivity) {
    // ImageButton imageGPS = (ImageButton)
    // fgActivity.findViewById(R.id.gps_status);
    // imageGPS.setOnClickListener(this);
    // }
    //
    // private void initialImageButtonNetwork(final FGActivity fgActivity) {
    // ImageButton wifi_button = (ImageButton)
    // fgActivity.findViewById(R.id.wifi_status);
    // wifi_button.setOnClickListener(this);
    // }

    private void initializeCurrentLocation() {
        Context context = fgSystemManager.getFGActivity().getApplicationContext();


//       mLocation = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mapView);
//        mapView.getOverlays().add(mLocation);
//         mLocation.enableMyLocation();
//         mLocation.enableFollowLocation();
//         mapView.getController().setCenter(mLocation.getMyLocation());
//         mLocation.runOnFirstFix(new Runnable() {
//         @Override
//         public void run() {
////         FGMapManager.this.fgSystemManager.getFGGPSManager().regenerateGeoPosition();
////             mapView.getController().setCenter(mLocation.getMyLocation());
//         }
//         });

//         this.fgSystemManager.getFGGPSManager().getLastKnownLocation();
//         GeoPoint geoPointCurrent;
//         if (mLocation != null) {
//            geoPointCurrent = new GeoPoint(mLocation.getMyLocation().getLatitude(),
//                    mLocation.getMyLocation().getLongitude());
//         } else {
//            geoPointCurrent = FinalValue.GEOPOINT_VICTORY;
//         }
//         if (mLocation.getMyLocation() != null) {
//             this.mapController.setCenter(mLocation.getMyLocation());
//         }
//         else {
//             this.mapController.setCenter(geoPointCurrent);
//         }
    }

    private void initialButtonZoomControl() {
//        this.mapController.setZoom(18);

        FGActivity fgActivity = this.fgSystemManager.getFGActivity();
        MapFragment mf = (MapFragment) fgActivity.getSupportFragmentManager().findFragmentById(R.id.map_fragment_id);

        ImageButton imageButtonZoomout = (ImageButton) mf.getView().findViewById(R.id.imagebutton_zoomout);
        imageButtonZoomout.setOnClickListener(this);

        ImageButton imageButtonZoomin = (ImageButton) mf.getView().findViewById(R.id.imagebutton_zoomin);
        imageButtonZoomin.setOnClickListener(this);
    }

    public MapView getMapView() {
        return this.mapView;
    }

    public ItemizedIconOverlay<Spot> getMarker() {
        return markers;
    }

    public IMapController getMapController() {
        return this.mapController;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // case R.id.imagebutton_center:
            // Location location =
            // fgSystemManager.getFGGPSManager().getLastKnownLocation();
            // GeoPoint geoPointCurrent;
            // if (mLocation.getMyLocation() != null)
            // fgSystemManager.getFGMapManager().getMapController().animateTo(mLocation.getMyLocation());
            // else if (location != null) {
            // geoPointCurrent = new GeoPoint(location.getLatitude(),
            // location.getLongitude());
            // fgSystemManager.getFGMapManager().getMapController().animateTo(geoPointCurrent);
            // }
            //
            // // For testing-purpose only
            //
            // // Spot[] set =
            // this.fgSystemManager.getFGDatabaseManager().getMarked().values().toArray(new
            // Spot[this.fgSystemManager.getFGDatabaseManager().getMarked().size()]);
            // // for (Spot spot : set) {
            // // this.fgSystemManager.removeMarkerOnMap(spot);
            // // }
            //
            // // -------------------------
            // break;
            // case R.id.imagebutton_filter:
            // this.fgSystemManager.getFGActivity().startActivityForResult(new
            // Intent(this.fgSystemManager.getFGActivity(), PreferenceFilter.class),
            // FinalValue.FILTER_REQUEST);
            // break;
            // case R.id.imagebutton_search:
            // DialogSearchHouseMarker dialogSearchHouseMarker =
            // this.fgSystemManager.getFGDialogManager().getDialogSearchHouseMarker();
            // dialogSearchHouseMarker.show();
            // break;
            case R.id.imagebutton_zoomout:
                this.mapController.zoomOut();
                break;
            case R.id.imagebutton_zoomin:
                this.mapController.zoomIn();
                break;
            // case R.id.wifi_status:
            // this.fgSystemManager.getFGActivity().startActivity(new
            // Intent(Settings.ACTION_WIFI_SETTINGS));
            // break;
            // case R.id.gps_status:
            // this.fgSystemManager.getFGActivity().startActivity(new
            // Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            // break;
            // case R.id.imagebutton_mapstyle:
            // DialogMapStyle dialogMapStyle =
            // FGMapManager.this.fgSystemManager.getFGDialogManager().getDialogMapStyle();
            // dialogMapStyle.show();
            // break;
        }
    }

    public MyLocationNewOverlay getMyLocationOverlay() {
        return mLocation;
    }

    public ItemGestureListener getGesture() {
        return gesture;
    }

    private void tokenize(InputStream input) {
        sat_url = new ArrayList<String>();
        hybrid_url = new ArrayList<String>();
        maps_url = new ArrayList<String>();

        if (input == null) {
            // If everything fail, Hard-coded
            Log.d("TAG!", "Everything fail, hard-coded");

            sat_url.add("https://khms0.googleapis.com/kh?v=113&hl=th&");
            sat_url.add("https://khms1.googleapis.com/kh?v=113&hl=th&");

            hybrid_url.add("https://mts0.googleapis.com/vt?lyrs=h@177000000&src=api&hl=th&");
            hybrid_url.add("https://mts1.googleapis.com/vt?lyrs=h@177000000&src=api&hl=th&");

            maps_url.add("https://mts0.googleapis.com/vt?lyrs=m@177000000&src=api&hl=th&");
            maps_url.add("https://mts1.googleapis.com/vt?lyrs=m@177000000&src=api&hl=th&");
            maps_url.add("https://mts0.googleapis.com/mapslt?hl=th&");
            maps_url.add("https://mts1.googleapis.com/mapslt?hl=th&");

            return;
        }

        double version = -1;

        Scanner sc = null;

        sc = new Scanner(input);

        while (sc.hasNextLine()) {
            String str = sc.nextLine();
            if (str.indexOf("googleapis") != -1) {
                String token[] = str.split("\\\"");
                for (int i = 0; i < token.length; i++) {
                    if (token[i].indexOf("http") != -1) {

                        if (token[i].indexOf("h@") != -1) {
                            token[i] = normalizeString(token[i]);
                            hybrid_url.add(token[i]);
                        }

                        if (token[i].indexOf("m@") != -1 || token[i].indexOf("mapslt?") != -1) {
                            token[i] = normalizeString(token[i]);
                            maps_url.add(token[i]);
                        }

                        if (token[i].indexOf("khm") != -1 && token[i].indexOf("kh?") != -1) {
                            token[i] = normalizeString(token[i]);
                            Uri uri = Uri.parse(token[i]);

                            double tmp = -2;
                            try {
                                tmp = Double.parseDouble(uri.getQueryParameter("v"));
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            if (tmp >= version) {
                                sat_url.add(token[i]);
                                version = tmp;
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    private String normalizeString(String str) {
        return str.trim().replaceAll("\\\\u0026", "&").replaceAll("en-US", "th");
    }

    private String downloadFile() {
        Log.d("TAG!", "Begin Downloading!");
        // AsyncTaskDownloadFile down = new
        // AsyncTaskDownloadFile(this.fgSystemManager.getFGActivity());
        // down.execute("https://maps.googleapis.com/maps/api/js?sensor=false");
        String result = null;
        // try {
        result = downloadManager("https://maps.googleapis.com/maps/api/js?sensor=false");
        // } catch (InterruptedException e1) {
        // e1.printStackTrace();
        // } catch (ExecutionException e1) {
        // e1.printStackTrace();
        // } catch (TimeoutException e) {
        // Log.d("TAG!", "Timeout");
        // e.printStackTrace();
        // }
        Log.d("TAG!", "*" + result + "*");
        return result;
    }

    private String downloadManager(String params) {
        String file_name = null;
        try {

            Log.d("TAG!", "Initializing Downloading!");

            URL url = new URL(params);
            Log.d("TAG!", "URL Connection!");
            URLConnection connection = url.openConnection();
            Log.d("TAG!", "Begin Connect!");
            connection.connect();

            // int fileLength = connection.getContentLength();

            Log.d("TAG!", "Begin Real Donwlaoding!");

            file_name = "temp_file";

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = fgSystemManager.getFGActivity().openFileOutput(file_name, Context.MODE_PRIVATE);

            byte data[] = new byte[1024];
            // long total = 0;
            int count;

            while ((count = input.read(data)) != -1) {
                // total += count;
                Log.d("TAG!", "Progress Updated!!");
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file_name;
    }

    public MapTileProviderBasic getOverlayTileProvider() {
        return tileProvider;
    }

    public void clearCahce() {
        this.mapView.getTileProvider().clearTileCache();
        if (tileProvider != null)
            tileProvider.clearTileCache();
    }

    public void close() {
        currentMapStyle = -1;
    }
}
