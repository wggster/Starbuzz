package edu.ucsd.wgg.starbuzz;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Bill on 9/19/2016.
 */
public class DrinkTest {
    private String name = "foo";
    private String description = "description of foo";
    private Drink testDrink;

    @Before
    public void setUp() throws Exception {
         testDrink = new Drink(name, description);
    }

    @Test
    public void testGetDescription() throws Exception {
        assert(testDrink.getDescription().equals(description));
    }

    @Test
    public void testGetName() throws Exception {
        assert(testDrink.getName().equals(name));
    }

    @Test
    public void testToString() throws Exception {
        assert(testDrink.toString().equals(name));
    }
}