package ir.sharif.rahpaapp.MapMVP;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

/**
 * Created by hmd on 06/29/2018.
 */

public class MapsModel implements MapsContract.Model {
    private MapsContract.Presenter presenter;

    @Override
    public void attachPresenter(MapsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void defaultLocation() {
        //default location for shaif Uni
        presenter.setDefaultLocation(35.703639, 51.351588);
    }

    @Override
    public String getCompleteAddress(Context context, double lat, double lng) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, new Locale("fa"));
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("Current loction address", strReturnedAddress.toString());
            } else {
                Log.w("Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Current loction address", "Canont get Address!");
        }
        return strAdd;
    }

}
