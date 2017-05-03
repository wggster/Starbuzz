package edu.ucsd.wgg.starbuzz;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

/**
 * This service both monitors for close stores and provides a method telling you how far the closest
 * store is.
 *
 * TODO: I think Google advocates for using Google Play Services APIs over Android APIs for location:
 *  https://developer.android.com/training/location/index.html
 * TODO: In particular, for location, they advocate using the FusedLocationProviderApi:
 *  https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi
 *
 * TODO: To keep this service running even when the system might like to unload it, you have to declare
 * it a Foreground Service.  You do that by calling startForeground(NOTIFICATION_ID,Notification),
 * which changes the status of the service and puts a persistent notification on the status bar.
 * I noticed that Spotify's notification has an "X" on it for closing it out.  I think that cancels
 * the foregrounding.  https://developer.android.com/guide/components/services.html#Foreground
 */
public class CoffeeNearService extends Service {

    // From the Google Maps web interface, the lat/lon at the end

    private final IBinder binder = new CNSBinder();

    private Location lastLocation;
    private StarbuzzLocations starbuzzLocations;
    private LocationListener buzzListener;
    private LocationManager locManager;
    static final int MIN_DISTANCE = 500; // meters

      public class CNSBinder extends Binder {
        CoffeeNearService getCoffeeNear() { return CoffeeNearService.this; }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {

        starbuzzLocations = new StarbuzzLocations();

        LocationListener buzzListener = new LocationListener() {
            Location lastListenerLocation; // track the last location signalled from this Listener
            @Override
            public void onLocationChanged(Location newListenerLocation) {
                // Avoids repeated notifications while within the "near" zone
                // TODO: susceptible to toggling is near the boundary and have GPS drift;
                // TODO: would want to add a timer or something
                if (!starbuzzLocations.nearStore(lastListenerLocation, MIN_DISTANCE) && starbuzzLocations.nearStore(newListenerLocation, MIN_DISTANCE)) {
                    showMessage("You are near StarBuzz!");
                }
                lastListenerLocation = lastLocation = newListenerLocation;
            }
            @Override
            public void onProviderDisabled(String arg0) {}
            @Override
            public void onProviderEnabled(String arg0) {}
            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
        };
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_DISTANCE, 1, buzzListener);
    }


    @Override
    public void onDestroy() {
        if (locManager != null && buzzListener != null) {
            locManager.removeUpdates(buzzListener);
            locManager = null;
            buzzListener = null;
        }
    }


    /*
     * True if we have a location or way to get one (TODO - could there be a race here?)
     * Simply getting "enabled" Providers (true) means they are allowed by the user/system.
     */
    public boolean hasLocation() {
        return lastLocation != null || locManager.getProviders(true).size() > 0;
    }

    // PRE: hasLocation()
    // POST: lastLocation != null
    public Location getLastLocation() {
        if (lastLocation != null)
            return lastLocation;
        else {
            Criteria criterion = new Criteria();
            criterion.setAccuracy(Criteria.ACCURACY_FINE);
            lastLocation = locManager.getLastKnownLocation(locManager.getBestProvider(criterion, true));
            return lastLocation;
        }
    }

    public boolean nearStore(Location location, int minDistance) {
        return starbuzzLocations.nearStore(location,minDistance);
    }

    // PRE: hasLocation()
    public double getMiles() {
        return starbuzzLocations.getMiles(getLastLocation());
    }

    /*
     * OMG the things we have to do to post a notification.  First, we need to set up the Activity
     * to be activated when the notification is clicked.  Then we need to build the notification
     * with a Builder (using the Builder pattern).  Finally, we get to post the notification.
     */
    protected void showMessage(String message) {
        // WGG - if we were really clever here we'd remember the last page we were on
        Intent mainIntent = new Intent(this,TopLevelActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TopLevelActivity.class);
        stackBuilder.addNextIntent(mainIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)  // Used to be Max, but could be annoying
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .build();

        final int NOTIFICATION_ID = 5454; // made up, different than the others
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


}
