package edu.ucsd.wgg.starbuzz;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Created by Bill on 9/21/2016.
 */
public class CoffeeNearServiceTest {

    protected CoffeeNearService service;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    /*
     * WGG: Note - you can't set up the service and then run multiple tests on it apparently.
     * You have to run each test totally independently.  The Service gets torn down after each
     * test.
     *
     * Actually, it appears worse than that.  I can't invoke two @Test's without a failure!  This:
     * java.lang.NullPointerException: Attempt to invoke virtual method
     *   'edu.ucsd.wgg.starbuzz.CoffeeNearService edu.ucsd.wgg.starbuzz.CoffeeNearService$CNSBinder.getCoffeeNear()'
     *   on a null object reference
     *
     * And it continues to get weider.  Now that I've broken these out into two test classes, they won't run in sequence,
     * but I can run either one-at-a-time and each works fine.  I suspect some kind of tear-down problem.
     *
     * However, after fixing a bug, I can at least set up the Service in the @Before and test it
     * in the one @Test.
     */

    @Before
    public void createService() throws TimeoutException {
        IBinder binder = mServiceRule.bindService(
                new Intent(InstrumentationRegistry.getTargetContext(), CoffeeNearService.class));
        service = ((CoffeeNearService.CNSBinder) binder).getCoffeeNear();
    }


    /*
     * Why wouldn't this be a race condition like in TopLevelActivity?  mServiceRule is smart?
     *
     * As written, this test could actually fail, but it was simple to set up.
     *
     */
/*    @Test
    public void testHasMiles() throws TimeoutException {
        assertTrue("True wasn't returned by CoffeeNearService.hasMiles()", service.hasMiles());
    }
*/
    @Test
    public void testGetMiles() throws TimeoutException {
        assertTrue("CoffeeService.getMiles() returned a value less than 0", service.getMiles(service.getLastLocation()) >= 0);
    }
}