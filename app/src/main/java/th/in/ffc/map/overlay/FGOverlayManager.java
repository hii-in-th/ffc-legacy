package th.in.ffc.map.overlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import th.in.ffc.R;
import th.in.ffc.map.database.FGDatabaseManager;
import th.in.ffc.map.system.FGSystemManager;
import th.in.ffc.map.value.FinalValue;
import th.in.ffc.map.value.MARKER_TYPE;
import th.in.ffc.map.village.spot.Spot;

public class FGOverlayManager {

    private FGSystemManager fgSystemManager;

    private ItemizedIconOverlay<Spot> markers;

    public static boolean pic_changed;

    public FGOverlayManager(FGSystemManager fgSystemManager) {

        pic_changed = false;

        this.fgSystemManager = fgSystemManager;

        this.markers = fgSystemManager.getFGMapManager().getMarker();

        this.markMarkerOnMapFirstStart();

    }

    private void markMarkerOnMapFirstStart() {

        FGDatabaseManager db = this.fgSystemManager.getFGDatabaseManager();

        for (Spot entry : db.getMarked().values())
            this.markMarkerOnMap(entry);
    }

    public void markMarkerOnMap(Spot spot) {

        Resources resources = fgSystemManager.getFGActivity().getResources();

        Bundle b = spot.getBundle();

        MARKER_TYPE type = Enum.valueOf(MARKER_TYPE.class, spot.getUid());

        if (type == MARKER_TYPE.HOUSE) {
            String stringColor = b.getString("Color");

            int chosen = -1;
            if (stringColor.equals(FinalValue.STRING_RED)) {

                if (b.getBoolean("Special")) {
                    chosen = R.drawable.house_red_special;
                } else {
                    chosen = R.drawable.house_red;
                }

            } else {
                if (b.getBoolean("Special")) {
                    chosen = R.drawable.house_green_special;
                } else {
                    chosen = R.drawable.house_green;
                }
            }

            spot.setMarker(this.getMarkerWithText(chosen, fgSystemManager.getFGActivity(), spot.getBundle().getString("HNo"), Color.BLACK, resources));
            markers.addItem(0, spot);

//            Drawable icon = this.getMarker(chosen, resources);
//            icon.setBounds(0,0,icon.getIntrinsicWidth(),icon.getIntrinsicHeight());
//            spot.setMarker(icon);

            markers.addItem(spot);
        } else if (type == MARKER_TYPE.POI) {
            int intType = spot.getBundle().getInt("Type");
            int drawableID = -1;
            switch (intType) {
                case 1:
                    drawableID = R.drawable.drugstore;
                    break;
                case 2:
                    drawableID = R.drawable.clinic;
                    break;
                case 3:
                    drawableID = R.drawable.hotel;
                    break;
                case 4:
                    drawableID = R.drawable.shopping_mall;
                    break;
                case 5:
                    drawableID = R.drawable.shop;
                    break;
                case 6:
                    drawableID = R.drawable.market;
                    break;
                case 7:
                    drawableID = R.drawable.seven_eleven;
                    break;
                case 8:
                    drawableID = R.drawable.waterfall_2;
                    break;
                case 9:
                    drawableID = R.drawable.cinema;
                    break;
                case 10:
                    drawableID = R.drawable.entertain;
                    break;
                case 11:
                    drawableID = R.drawable.palm_tree_export;
                    break;
                case 12:
                    drawableID = R.drawable.soccer;
                    break;
                case 13:
                    drawableID = R.drawable.police;
                    break;
                case 14:
                    drawableID = R.drawable.airport;
                    break;
                case 15:
                    drawableID = R.drawable.bus_station;
                    break;
                case 16:
                    drawableID = R.drawable.train_station;
                    break;
                case 17:
                    drawableID = R.drawable.ferry;
                    break;
                case 18:
                    drawableID = R.drawable.tollstation;
                    break;
                case 19:
                    drawableID = R.drawable.car;
                    break;
                case 20:
                    drawableID = R.drawable.oil_station;
                    break;
                case 21:
                    drawableID = R.drawable.ngv_station;
                    break;
                case 22:
                    drawableID = R.drawable.lpg_station;
                    break;
                case 23:
                    drawableID = R.drawable.repair_car;
                    break;
                case 24:
                    drawableID = R.drawable.bank;
                    break;
                case 25:
                    drawableID = R.drawable.atm;
                    break;
                case 26:
                    drawableID = R.drawable.company;
                    break;
                case 27:
                    drawableID = R.drawable.factory;
                    break;
                case 28:
                    drawableID = R.drawable.constructioncrane;
                    break;
                case 29:
                    drawableID = R.drawable.workoffice;
                    break;
                case 30:
                    drawableID = R.drawable.court;
                    break;
                case 31:
                    drawableID = R.drawable.embassy;
                    break;
                case 32:
                    drawableID = R.drawable.castle_2;
                    break;
                case 33:
                    drawableID = R.drawable.communitycentre;
                    break;
                case 34:
                    drawableID = R.drawable.goverment;
                    break;
                case 35:
                    drawableID = R.drawable.family;
                    break;
                case 36:
                    drawableID = R.drawable.pagoda_2;
                    break;
                case 37:
                    drawableID = R.drawable.sight_2;
                    break;
                case 38:
                    drawableID = R.drawable.sozialeeinrichtung;
                    break;
                case 39:
                    drawableID = R.drawable.poweroutage;
                    break;
                default:
                    drawableID = R.drawable.poi;
                    break;
            }
            spot.setMarker(this.getMarker(drawableID, resources));
            markers.addItem(spot);
        } else {
            spot.setMarker(this.getMarker(type.getDrawableID(), resources));
            markers.addItem(spot);
        }

        // mapView.invalidate();
    }

    @SuppressWarnings("deprecation")
    private Drawable getMarkerWithText(int markerID, final Context context,
                                       String text, final int textColor, Resources res) {
        Drawable marker = res.getDrawable(markerID);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) marker;
//        Bitmap bitmap = Bitmap.createBitmap(bitmapDrawable.getBitmap().copy(
//                Bitmap.Config.ARGB_8888, true));

        Bitmap bitmap = Bitmap.createBitmap(bitmapDrawable.getBitmap().copy(
                Bitmap.Config.ARGB_8888, true));
        double mul= 1.3;
        Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*mul), (int)(bitmap.getHeight()*mul), false);
//        Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
        Canvas canvas = new Canvas(bitmap2);

        float default_width = 5f;

        final float densityMultiplier = context.getResources()
                .getDisplayMetrics().density;
        final float size = default_width * densityMultiplier;

        Paint paint = new Paint();
        paint.setColor(textColor);
        paint.setAntiAlias(true);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize((int)(size));

        final float center_x = (canvas.getWidth()) / 2;
        final float center_y = ((canvas.getHeight()) / 2) + 5;

        if ((text != null)) {

            int img_width = canvas.getWidth();
            if (img_width < 40)
                img_width -= 20;
            else
                img_width -= 30;

//            while (paint.measureText(text) > img_width && default_width > 0) {
//                paint.setTextSize(--default_width * densityMultiplier);
//            }

            canvas.drawText(text, center_x, center_y, paint);
        }

        bitmapDrawable = new BitmapDrawable(bitmap2);
//		bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth()*50,
//				bitmapDrawable.getIntrinsicHeight()*50);

        return bitmapDrawable;
    }

    @SuppressWarnings("deprecation")
    private Drawable getMarker(int markerID, Resources res) {

        Drawable marker = res.getDrawable(markerID);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) marker;
        Bitmap bitmap = Bitmap.createBitmap(bitmapDrawable.getBitmap().copy(
                Bitmap.Config.ARGB_8888, false));

        double mul= 1.3;
        Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*mul), (int)(bitmap.getHeight()*mul), false);
//        Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
        bitmapDrawable = new BitmapDrawable(bitmap2);
        bitmapDrawable.setBounds(0,0, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());
//		bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(),
//				bitmapDrawable.getIntrinsicHeight());

        return bitmapDrawable;

    }

    public void removeMarkerFromMap(Spot item) {
        markers.removeItem(item);
        // mapView.invalidate();
    }

    public ItemizedIconOverlay<Spot> getMarker() {
        return markers;
    }
}
