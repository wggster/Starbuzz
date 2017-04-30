package edu.ucsd.wgg.starbuzz;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/**
 * Created by Bill on 9/21/2016.
 */
public class CoffeeNearServiceHasMilesTest {

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
     */

    /*
     * Why wouldn't this be a race condition like in TopLevelActivity?  mServiceRule is smart?
     *
     * As written, this test could actually fail, but it was simple to set up.
     *
     */
    @Test
    public void testHasMiles() throws TimeoutException {
        IBinder binder = mServiceRule.bindService(
                new Intent(InstrumentationRegistry.getTargetContext(), CoffeeNearService.class));
        CoffeeNearService service = ((CoffeeNearService.CNSBinder) binder).getCoffeeNear();

        assertTrue("True wasn't returned by CoffeeNearService.hasMiles()", service.hasLocation());

    }

}