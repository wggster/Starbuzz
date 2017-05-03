package edu.ucsd.wgg.starbuzz;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/*
 * On a map around user's location, show nearby Starbucks stores.
 *
 * TODO: the current implementation has only one store near by house.
 *
 * It appears the "right" way to retrieve nearby stores is with the Google Places GeoDataApi,
 * using the getAutocompletePredictions method.  This appears to be a good example:
 *   http://www.truiton.com/2015/04/android-places-api-autocomplete-getplacebyid/  (1/3 way down)
 * We'd also want an AutocompleteFilter so that we get places of a certain type
 *   https://developer.android.com/reference/com/google/android/gms/location/places/AutocompleteFilter.html
 * Note that AutoComplete uses LatLngBounds to retrieve from an area.
 */
public class NearbyStoresMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private CoffeeNearService coffeeNearService;
    private boolean serviceBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            CoffeeNearService.CNSBinder coffeeNearBinder = (CoffeeNearService.CNSBinder) binder;
            coffeeNearService = coffeeNearBinder.getCoffeeNear();
            serviceBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {serviceBound = false; }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_stores_map);

        startCoffeeNearService(); // get this going so it will be ready for use by Map

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);  // WGG - "this" implements OnMapReadyCallback
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;  // set object's map field
        /* mMap.getUiSettings().setMyLocationButtonEnabled(false); */
        googleMap.setMyLocationEnabled(true);
        new ShowNearbyStoresTask().execute();
    }

    /*
     * Not only does this Bound Service run in the background looking for a close store, but it
     * also provides location as a method call.
     */
    protected void startCoffeeNearService() {
        Intent intent = new Intent(this, CoffeeNearService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    /*
     * Wait for CoffeeNearService to come up and for a location to be available.
     * Then center the map.
     *
     * I could wait for the CoffeeNearService to come up via the onServiceConnected callback,
     * but since I have to wait for location to come up anyway, might as well do it with the
     * AsyncTask.
     */
    private class ShowNearbyStoresTask extends AsyncTask<Void, Void,  Boolean> {

        protected void onPreExecute() {}

        protected Boolean doInBackground(Void... noArgs) {
            for (int i = 1; true; i *= 2) { // exponential backoff
                if (serviceBound && coffeeNearService.hasLocation())
                    return true;
                else {
                    Log.v("ShowNearbyStoresTask", "serviceBound = " + serviceBound);
                    synchronized (this) {
                        try { wait(i*10); } catch (InterruptedException e) { e.printStackTrace(); }
                        Log.v("ShowNearbyStoresTask", "doInBackground: " + i);
                    }
                }
            }
        }

        protected void onProgressUpdate(Void... voids) {}

        protected void onPostExecute(Boolean ready) {
            // A quick-and-dirty layout in case the clever code below is slow
            Location lastLocation = coffeeNearService.getLastLocation();
/*            mMap.moveCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
*/
            final LatLng starBuzzLL = new LatLng(StarbuzzLocations.STARBUZZ_LATITUDE, StarbuzzLocations.STARBUZZ_LONGITUDE);
            Marker starBuzzMarker = mMap.addMarker(new MarkerOptions()
                    .position(starBuzzLL)
                    .title("StarBuzz")
                    .snippet("Serving great coffee since 2016!"));

            // http://stackoverflow.com/questions/3779173/determining-the-size-of-an-android-view-at-runtime
            final FrameLayout layout = (FrameLayout) findViewById(R.id.map);
/*
            ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
*/
            int frameWidth = layout.getWidth();
            int frameHeight = layout.getHeight();
            LatLng myLL = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder()
                    .include(starBuzzLL)
                    .include(myLL)
                    .build(), frameWidth, frameHeight, 200));
                    }
/*                });
            }
        }
*/
    }

}
