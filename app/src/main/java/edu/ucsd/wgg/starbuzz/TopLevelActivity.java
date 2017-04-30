package edu.ucsd.wgg.starbuzz;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.AdapterView; // Super of ListView
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class TopLevelActivity extends AppCompatActivity {

    final int REQUEST_CODE = 1113;
    private static final String[] LOCATION_PERMS ={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    boolean locationPermitted;

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
        setContentView(R.layout.activity_top_level);

        /*
         * Due to new "features" in Android, I have to request GPS permissions from user
         *
         * This needs to come before hooking up the other Activities because NearbyStoresMapActivity
         * also uses location.
         */
        if (checkSelfPermission(LOCATION_PERMS[0]) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(LOCATION_PERMS, REQUEST_CODE); // runs startCoffeeNearService on success
        else // previously approved
            startCoffeeNearService();

        AdapterView.OnItemClickListener itemClickListener =
                new AdapterView.OnItemClickListener() {
                    Intent intent;
                    public void onItemClick(AdapterView<?> listView, View v, int position, long id) {
                        switch (position) {
                            case 0:
                                intent = new Intent(TopLevelActivity.this, DrinkCategoryActivity.class);
                                startActivity(intent);
                                break;
                            case 2:
                                intent = new Intent(TopLevelActivity.this, NearbyStoresMapActivity.class);
                                startActivity(intent);
                                break;
                            default:
                                Toast.makeText(TopLevelActivity.this, "Option not yet supported", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                };
        ListView listView = (ListView) findViewById(R.id.list_options);
        listView.setOnItemClickListener(itemClickListener);

        new PopulateClosestStoreTask().execute();
    }

        @Override
        /*
         * Adapted from https://developer.android.com/training/permissions/requesting.html
         *
         * Requesting permissions from the user results in a callback, because we can't lock the Main Thread.
         */
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            switch (requestCode) {
                case REQUEST_CODE: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startCoffeeNearService();
                    } else {
                        Toast.makeText(this, "Location permissions denied", Toast.LENGTH_LONG).show();
                    }
                }
                default:
                    Log.v("TopLevelActivity", "unexpected request code returned from permissions request" + requestCode);
            }
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(connection);
            serviceBound = false;
        }
    }

    /*
     * Not only does this Bound Service run in the background looking for a close store, but it
     * also provides the distance to the closest store as a method call.
     */
    protected void startCoffeeNearService() {
        Intent intent = new Intent(this, CoffeeNearService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    /*
     * Wait for CoffeeNearService to come up and for a location to be available.
     * Then populate the TextView.
     *
     * I could wait for the CoffeeNearService to come up via the onServiceConnected callback,
     * but since I have to wait for location to come up anyway, might as well do it with the
     * AsyncTask.
     */
    private class PopulateClosestStoreTask extends AsyncTask<Void, Void,  Boolean> {
        private TextView closestView;

        protected void onPreExecute() {
            closestView = (TextView) findViewById(R.id.closest_store);
            if (!serviceBound) {
                closestView.setText("GPS is not available, cannot find closest store.");
            }
        }
        protected Boolean doInBackground(Void... noArgs) {
            for (int i = 1; true; i *= 2) { // exponential backoff
                if (serviceBound && coffeeNearService.hasLocation())
                    return true;
                else {
                    synchronized (this) {
                        try { wait(i*10); } catch (InterruptedException e) { e.printStackTrace(); }
                        Log.v("PopulateClosestStoreTask", "doInBackground: " + i);
                    }
                }
            }
        }

        protected void onProgressUpdate(Void... voids) {}

        protected void onPostExecute(Boolean ready) {
            // TODO - perhaps getMiles is slow enough that it should be run in the background
            if (coffeeNearService.hasLocation()) // in case we don't have a location provider!
                closestView.setText(String.format("The nearest store is %1$.1f miles from you!",coffeeNearService.getMiles()));
        }
    }
}