package th.in.ffc.map.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import th.in.ffc.R;
import th.in.ffc.map.FGActivity;
import th.in.ffc.map.dialog.buider.BuilderAllHouseMarkerHasBeanCreated;
import th.in.ffc.map.value.FinalValue;

public class FGDialogManager {

//	private FGSystemManager fgSystemManager;

    // private DialogSearchHouseMarker dialogSearchHouseMarker;
    //
    // private DialogOption dialogOption;
    //
    // private AlertDialog alertDialogConfirmDeleteHouseMarker;
    //
    // private AlertDialog alertDialogAllHouseHasBeenCreated;
    //
    // private AlertDialog alertDialogFilterList;
    //
    // private AlertDialogConfirmUseYourLocation
    // alertDialogConfirmUseYourLocation;
    //
    // private AlertDialogLocationNotFound alertDialogLocationNotFound;
    //
    // private DialogMapStyle dialogMapStyle;

    public FGDialogManager() {
//		this.fgSystemManager = fgSystemManager;

    }

    public DialogSearchHouseMarker getDialogSearchHouseMarker() {
        DialogSearchHouseMarker dialogSearchHouseMarker = new DialogSearchHouseMarker(
                FGActivity.fgsys);
        return dialogSearchHouseMarker;
    }

//	public DialogOption getDialogOption() {
//		DialogOption dialogOption = new DialogOption(this.fgSystemManager);
//		return dialogOption;
//	}

    public AlertDialog getAlertDialogAllHouseHasBeenCreated() {
        BuilderAllHouseMarkerHasBeanCreated builderAllHouseMarkerHasBeanCreated = new BuilderAllHouseMarkerHasBeanCreated(
                FGActivity.fgsys);
        AlertDialog alertDialogAllHouseHasBeenCreated = builderAllHouseMarkerHasBeanCreated
                .create();
        alertDialogAllHouseHasBeenCreated
                .setTitle(FinalValue.STRING_All_HOUSE_MARKER_HAS_BEEN_CREATED_TITLE);
        alertDialogAllHouseHasBeenCreated
                .setIcon(android.R.drawable.ic_dialog_info);
        return alertDialogAllHouseHasBeenCreated;
    }

//	public AlertDialogConfirmUseYourLocation getAlertDialogConfirmUseYourLocation(AlertDialogConfirmUseYourLocation dialog) {
//		AlertDialogConfirmUseYourLocation alertDialogConfirmUseYourLocation = new AlertDialogConfirmUseYourLocation(
//				this.fgSystemManager,dialog);
//		return alertDialogConfirmUseYourLocation;
//	}

    public AlertDialog getAlertDialogLocationNotFound(Activity act) {
        Resources res = act.getResources();
        AlertDialog.Builder b = new AlertDialog.Builder(act,android.R.style.Theme_Material_Light_Dialog_Alert);
        b.setTitle(res.getString(R.string.STRING_LOCATION_NOT_FOUND_TITLE));
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setCancelable(false);
        b.setMessage(res.getString(R.string.STRING_LOCATION_NOT_FOUND_MESSAGE));
        b.setPositiveButton("OK", null);
        return b.create();
    }

//	public DialogMapStyle getDialogMapStyle() {
//		DialogMapStyle dialogMapStyle = new DialogMapStyle(this.fgSystemManager);
//		return dialogMapStyle;
//	}

}
