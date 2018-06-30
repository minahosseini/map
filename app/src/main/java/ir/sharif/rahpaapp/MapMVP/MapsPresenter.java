package ir.sharif.rahpaapp.MapMVP;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hmd on 06/29/2018.
 */

public class MapsPresenter implements MapsContract.Presenter {
    private MapsContract.View view;
    private MapsModel model;

    @Override
    public void attachView(MapsContract.View view) {
        this.view = view;
        model = new MapsModel();
        model.attachPresenter(this);
    }

    @Override
    public void requestPermissions() {
        view.checkPermission();
    }

    @Override
    public void onPermissionsResult(boolean permission) {
        if (permission)
            view.permissionGranted();
        else
            view.permissionNotGranted();
    }

    @Override
    public void getDefaultLocation() {
        model.defaultLocation();
    }

    @Override
    public void setDefaultLocation(double lat,double lng) {
        view.setDefaultLocation(lat,lng);
    }

    @Override
    public void setMarker(boolean setOrigin, boolean setDestination) {
        if (setOrigin == false)
            view.setOriginMarker();
        else
            view.setDestinationMarker();
    }


    //ToDo
    @Override
    public void getCompleteAddress(Context context, double lat, double lng) {
        if (model.getCompleteAddress(context, lat, lng) != null)
            view.showCompleteAddress(model.getCompleteAddress(context, lat, lng));
        else
            view.showCompleteAddress("Address not found");
    }

}
