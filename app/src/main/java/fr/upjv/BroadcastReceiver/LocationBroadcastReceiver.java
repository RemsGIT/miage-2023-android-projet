package fr.upjv.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fr.upjv.Services.LocationTrackingService;

/**
 * Permet l'arrêt du service
 */
public class LocationBroadcastReceiver extends BroadcastReceiver {
    private LocationTrackingService locationService;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals("STOP_LOCATION_SERVICE")) {
            if (locationService != null) {
                System.out.println("STOP SERVICE LOCATION");
                context.stopService(new Intent(context, LocationTrackingService.class));
                context.unregisterReceiver(this);
                locationService = null;
            }
        }
    }

    public void setLocationService(LocationTrackingService locationService) {
        this.locationService = locationService;
    }
}
