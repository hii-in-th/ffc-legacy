package th.in.ffc.map.system;

import android.content.Intent;
import android.util.Log;
import com.ibus.phototaker.PhotoTaker;
import th.in.ffc.map.FGActivity;
import th.in.ffc.map.database.FGDatabaseManager;
import th.in.ffc.map.date.FGDateManager;
import th.in.ffc.map.dialog.FGDialogManager;
import th.in.ffc.map.gps.FGGPSManager;
import th.in.ffc.map.map.FGMapManager;
import th.in.ffc.map.overlay.FGOverlayManager;
import th.in.ffc.map.user.FGUserManager;
import th.in.ffc.map.village.spot.Spot;

public class FGSystemManager {

    private FGActivity fgActivity;

    private FGDatabaseManager fgDatabaseManager;

    private FGGPSManager fgGPSManager;

    private FGUserManager fgUserManager;

    private FGMapManager fgMapManager;

    private FGOverlayManager fgOverlayManager;

    private FGDialogManager fgDialogManager;

    private FGDateManager fgDateManager;

    private PhotoTaker pt;

    public FGSystemManager(FGActivity fgActivity) {

        this.fgActivity = fgActivity;

        Log.d("TAG!", "Begin Database!");

        this.fgDatabaseManager = new FGDatabaseManager(FGSystemManager.this);

        Log.d("TAG!", "Complete Database");

        this.fgDialogManager = new FGDialogManager();

        this.fgDateManager = new FGDateManager(FGSystemManager.this);

        this.pt = new PhotoTaker(fgActivity);

        this.setFGActivity(fgActivity);
    }

    public void setFGActivity(FGActivity act) {
        this.fgActivity = act;

//		this.pt.setActivity(act);

        Log.d("TAG!", "Begin Optional!");

        Log.d("TAG!", "Begin GPS!");

        this.fgGPSManager = new FGGPSManager(FGSystemManager.this);

        Log.d("TAG!", "Begin UserMG!");

        this.fgUserManager = new FGUserManager(FGSystemManager.this);

        Log.d("TAG!", "Begin MapMG!");

        this.fgMapManager = new FGMapManager(FGSystemManager.this);

        Log.d("TAG!", "Begin OverlayMG!");

        this.fgOverlayManager = new FGOverlayManager(FGSystemManager.this);

        Log.d("TAG!", "End Optional!");
    }

    public void startActivity(Intent intent) {
        try {
            fgActivity.startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("startAct", "error while start app");
        }
    }

    public boolean removeMarkerOnMap(Spot spot) {

        Double lat = spot.getDoubleLat();
        Double lon = spot.getDoubleLong();

        //TODO Change to Google Map
        spot.setLatitude(0);
        spot.setLongitude(0);

        if (this.fgDatabaseManager.updateGeoPointToDatabase(spot)) {
            spot.setMarker(null);

            this.fgOverlayManager.removeMarkerFromMap(spot);
            this.fgDatabaseManager.addSpotToAvailable(spot);
            return true;
        } else {
            //TODO Change to Google Map
            spot.setLatitude(lat);
            spot.setLongitude(lon);

            return false;
        }
    }

    public void markMarkerOnMap(Spot spot) {
        if (this.fgDatabaseManager.updateGeoPointToDatabase(spot)) {
            this.fgOverlayManager.markMarkerOnMap(spot);
            this.fgDatabaseManager.addSpotToMarked(spot);
        }

    }

    public void editMarkerOnMap(Spot spot) {
        if (this.fgDatabaseManager.updateGeoPointToDatabase(spot)) {
            this.fgMapManager.getMapView().invalidate();
            //TODO Change to Google Map
             this.fgOverlayManager.removeMarkerFromMap(spot);
             this.fgOverlayManager.markMarkerOnMap(spot);
        }
    }

    public FGActivity getFGActivity() {
        return fgActivity;
    }

    public FGDatabaseManager getFGDatabaseManager() {
        return fgDatabaseManager;
    }

    public FGGPSManager getFGGPSManager() {
        return fgGPSManager;
    }

    public FGMapManager getFGMapManager() {
        return fgMapManager;
    }

    public FGOverlayManager getFGOverlayManager() {
        return fgOverlayManager;
    }

    public FGDialogManager getFGDialogManager() {
        return fgDialogManager;
    }

    public FGDateManager getFgDateManager() {
        return fgDateManager;
    }

    public FGUserManager getFgUserManager() {
        return fgUserManager;
    }

    public PhotoTaker getPhotoTaker() {
        return pt;
    }

    public void close() {
        fgActivity = null;

        fgDatabaseManager.getDatabaseManager().closeDatabase();
        fgDatabaseManager = null;

        fgGPSManager = null;
        fgUserManager = null;

        fgMapManager.close();
        fgMapManager = null;
        fgOverlayManager = null;
        fgDialogManager = null;
        fgDateManager = null;

    }

}
