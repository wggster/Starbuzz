package edu.ucsd.wgg.starbuzz;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/*
 * In this test we'll create our own LocationProvider and see whether our proximity test works right.
 * Becaues I haven't figured out how to have two @Test's, I'll just put everything in one @Test.
 */
public class CoffeeNearServiceNearTest {

    protected CoffeeNearService service;
    protected LocationProviderMock lpMock;

    /*
     * This class provides a convenient interface to the TestProvider facilities of LocationManager.
     * Adapted from https://mobiarch.wordpress.com/2012/07/17/testing-with-mock-location-data-in-android/
     */
    private class LocationProviderMock {
        private String providerName; // this is the service we'll coopt
        private Context context;
        private LocationManager locMgr;

        public LocationProviderMock(String providerName, Context context) {
            this.providerName = providerName;
            this.context = context;

            locMgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locMgr.addTestProvider(providerName, false, false, false, false, false,true, true, 0, 10);  // WGG- how important is the accuracy?
            locMgr.setTestProviderEnabled(providerName, true);
        }

        public Location setLocation(double latitude, double longitude) {
            Location newLoc = new Location(providerName);
            newLoc.setLatitude(latitude);
            newLoc.setLongitude(longitude);
            newLoc.setAltitude(0);
            newLoc.setAccuracy(10);
            newLoc.setTime(System.currentTimeMillis());
            newLoc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos()); // needs API 17+
            locMgr.setTestProviderLocation(providerName, newLoc);
            return newLoc;
        }

        public void destroy() {
            locMgr.removeTestProvider(providerName);
        }
    }

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Before
    public void createService() throws TimeoutException {
        Context targetContext = InstrumentationRegistry.getTargetContext();

        lpMock = new LocationProviderMock(LocationManager.GPS_PROVIDER, targetContext);
        lpMock.setLocation(32.879409,-117.2382162); // UCSD
        // set here so that CoffeeNearService has a known location at start up

        IBinder binder = mServiceRule.bindService(new Intent(targetContext, CoffeeNearService.class));
        service = ((CoffeeNearService.CNSBinder) binder).getCoffeeNear();
    }

    @Test
    public void testNear() {
        Location testLoc;

        // We're far from the Governor Starbucks
        testLoc = lpMock.setLocation(32.879409,-117.2382162); // UCSD (again, for locality)
        assertTrue("CoffeeNearService.nearStore reported near at UCSD", !service.nearStore(testLoc));

        // We're far from the Governor Starbucks
        testLoc = lpMock.setLocation(32.8417407,-117.2289234); // Home
        assertTrue("CoffeeNearService.nearStore reported near at Home", !service.nearStore(testLoc));

        // We're close to the Governor Starbucks
        testLoc = lpMock.setLocation(32.8514615,-117.2159587); // Should be close
        assertTrue("CoffeeNearService.nearStore reported far at Standley Rec", service.nearStore(testLoc));

        // We're at the Governor Starbucks
        testLoc = lpMock.setLocation(CoffeeNearService.STARBUZZ_LATITUDE,CoffeeNearService.STARBUZZ_LONGITUDE); // Starbucks
        assertTrue("CoffeeNearService.nearStore reported near at Starbucks", service.nearStore(testLoc));
    }
}