package ir.sharif.rahpaapp.MapMVP;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hmd on 06/29/2018.
 */

public interface MapsContract {
    interface View {
        void setLanguage();

        void checkPermission();

        void permissionGranted();

        void permissionNotGranted();

        void setDefaultLocation(double lat,double lng);

        void setOriginMarker();

        void setDestinationMarker();

        void showCompleteAddress(String address);
    }

    interface Presenter {
        void attachView(View view);

        void requestPermissions();

        void onPermissionsResult(boolean permission);

        void getDefaultLocation();

        void setDefaultLocation(double lat,double lng);

        void setMarker(boolean setOrigin, boolean setDestination);

        void getCompleteAddress(Context context, double lat, double lng);

    }

    interface Model {
        void attachPresenter(Presenter presenter);

        void defaultLocation();

        String getCompleteAddress(Context context, double lat, double lng);

    }
}
