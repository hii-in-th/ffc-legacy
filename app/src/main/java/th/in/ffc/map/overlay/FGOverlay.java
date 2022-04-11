package th.in.ffc.map.overlay;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import th.in.ffc.R;
import th.in.ffc.map.system.FGSystemManager;
import th.in.ffc.map.ui.markerActivity.CreateMarkerActivity;

public class FGOverlay extends Overlay {

    private FGSystemManager fgSystemManager;

    public FGOverlay(FGSystemManager fgSystemManager) {
        super(fgSystemManager.getFGActivity().getApplicationContext());
        this.fgSystemManager = fgSystemManager;
    }

    @Override
    public void draw(Canvas arg0, MapView arg1, boolean arg2) {
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e, MapView mapView) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e, MapView mapView) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e, MapView mapView) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onLongPress(MotionEvent e, MapView mapView) {

        //TODO Change to Google Map
        ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = null;
        GeoPoint loc = (GeoPoint) mapView.getProjection().fromPixels((int) e.getX(),
                (int) e.getY());

        if (this.fgSystemManager.getFGDatabaseManager().getAvailable().size() == 0) {
            AlertDialog alertDialogAllHouseHasBeenCreated = this.fgSystemManager
                    .getFGDialogManager()
                    .getAlertDialogAllHouseHasBeenCreated();
            alertDialogAllHouseHasBeenCreated.show();
            return true;
        }
//        final Drawable marker = fgSystemManager.getFGActivity().getResources().getDrawable(R.drawable.marker_default);

        Intent act = new Intent(fgSystemManager.getFGActivity(),
                CreateMarkerActivity.class);
//        ArrayList<OverlayItem> overlayArray = new ArrayList<OverlayItem>();
//        OverlayItem mapItem = new OverlayItem("", "", new GeoPoint((((double)loc.getLatitude())), (((double)loc.getLongitude()))));
//        mapItem.setMarker(marker);
//        overlayArray.add(mapItem);
        double doubleLatitude = (double) (loc.getLatitude());
        act.putExtra("doubleLatitude", doubleLatitude);
        double doubleLongitude = (double) (loc.getLongitude());
        act.putExtra("doubleLongitude", doubleLongitude);
//        if(anotherItemizedIconOverlay==null){
//            anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(fgSystemManager.getFGActivity(), overlayArray,null);
//            mapView.getOverlays().add(anotherItemizedIconOverlay);
//            mapView.invalidate();
//        }else{
//            mapView.getOverlays().remove(anotherItemizedIconOverlay);
//            mapView.invalidate();
//            anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(fgSystemManager.getFGActivity(), overlayArray,null);
//            mapView.getOverlays().add(anotherItemizedIconOverlay);
//        }
        // act.putExtra("number", 0);

        fgSystemManager.startActivity(act);

        return true;
    }

}
