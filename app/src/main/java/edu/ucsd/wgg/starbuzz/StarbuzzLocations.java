package edu.ucsd.wgg.starbuzz;

import android.location.Location;

/**
 * Created by Bill on 5/3/2017.
 */

public class StarbuzzLocations {
    private Location starBuzzLocation;
    static final double STARBUZZ_LATITUDE = 32.8516218;
    static final double STARBUZZ_LONGITUDE = -117.2157016;

    StarbuzzLocations() {initStarbuzzDatabase();}

    private void initStarbuzzDatabase() {
        starBuzzLocation = new Location(""); // empty provider, go figure
        starBuzzLocation.setLatitude(STARBUZZ_LATITUDE); // Starbucks on Governor
        starBuzzLocation.setLongitude(STARBUZZ_LONGITUDE); // via Google Maps search
    }
    /*
     * This method was exposed partially for reasons of Testing and DbC.
     * Now I can test the "near" calculation without depending on callbacks and Notifications.
     */
    public boolean nearStore(Location location, int minDistance) {
        return (location != null) && (location.distanceTo(starBuzzLocation) <= minDistance);
    }

    // PRE: hasLocation()
    public double getMiles(Location location) {
        return location.distanceTo(starBuzzLocation) / 1609.344; // meters -> miles
    }
}
