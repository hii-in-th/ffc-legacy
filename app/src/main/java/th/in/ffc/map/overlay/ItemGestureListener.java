package th.in.ffc.map.overlay;

import android.content.Intent;
import android.view.View;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.LayoutParams;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import th.in.ffc.R;
import th.in.ffc.map.FGActivity;
import th.in.ffc.map.MapFragment;
import th.in.ffc.map.balloon.BalloonOverlayView;
import th.in.ffc.map.system.FGSystemManager;
import th.in.ffc.map.ui.markerActivity.EditMarkerActivity;
import th.in.ffc.map.value.MARKER_TYPE;
import th.in.ffc.map.village.spot.Spot;

public class ItemGestureListener implements
        ItemizedIconOverlay.OnItemGestureListener<Spot> {

    private MapView mapView;
    private boolean isRecycled = false;
    private BalloonOverlayView<Spot> balloonView;

    public ItemGestureListener(FGSystemManager fgsystem) {
        FGActivity fg = fgsystem.getFGActivity();
        MapFragment mf = (MapFragment) fg.getSupportFragmentManager().findFragmentById(R.id.map_fragment_id);
        mapView = (MapView) mf.getView().findViewById(R.id.mapview);
    }

    //TODO Change to Google Map
/*    @Override
    public boolean onItemDoubleTap(int arg0, Spot spot) {
        // Log.i("TAG!","TAG! double!");

        // final int thisIndex;
        IGeoPoint point = spot.getPoint();

        // List<Overlay> mapOverlays = mapView.getOverlays();
        // if (mapOverlays.size() > 1) {
        // hideOtherBalloons(mapOverlays);
        // }

        // params.mode = MapView.LayoutParams.MODE_MAP;

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point,
                LayoutParams.BOTTOM_CENTER, 0, -10);
        // setBalloonTouchListener(thisIndex);

        if (isRecycled) {
            balloonView.setVisibility(View.GONE);
            balloonView.setData(spot);
            balloonView.setLayoutParams(params);
            balloonView.setVisibility(View.VISIBLE);
        } else {
            balloonView = new BalloonOverlayView<Spot>(mapView.getContext(), 0);
            balloonView.setVisibility(View.GONE);
            balloonView.setData(spot);
            balloonView.setVisibility(View.VISIBLE);
            mapView.addView(balloonView, 0, params);
            isRecycled = true;
        }

        MapController mapController = FGActivity.fgsys.getFGMapManager().getMapController();
        mapController.animateTo(point);

        return true;
    }*/

    @Override public boolean onItemSingleTapUp(int i, Spot spot) {
        if (spot.getUid().equals(MARKER_TYPE.HOUSE.name()))
            FGActivity.fgsys.getFGActivity()
                    .startFamilyTree(spot.getPartialID());

        return true;
    }

    @Override
    public boolean onItemLongPress(int arg0, Spot spot) {

        Intent intent = new Intent(FGActivity.fgsys.getFGActivity(),
                EditMarkerActivity.class);

        MARKER_TYPE type = Enum.valueOf(MARKER_TYPE.class, spot.getUid());

        intent.putExtra("edit", true);
        // intent.putExtra("text1", spot.getStringVillName());
        intent.putExtra("text2",
                spot.getRepresentativeString(type.getSpinnerText()));
        intent.putExtra("id_filename", spot.getID());
        intent.putExtra("type", type);
        // intent.putExtra("index", index);

        intent.putExtra("doubleLatitude", spot.getDoubleLat());
        intent.putExtra("doubleLongitude", spot.getDoubleLong());
        FGActivity.fgsys.startActivity(intent);
        return true;
    }

    //TODO Change to Google Map
//    @Override
//    public boolean onItemSingleTapConfirmed(int arg0, Spot spot) {
//
//        if (spot.getUid().equals(MARKER_TYPE.HOUSE.name()))
//            FGActivity.fgsys.getFGActivity()
//                    .startFamilyTree(spot.getPartialID());
//
//        return true;
//    }

}
