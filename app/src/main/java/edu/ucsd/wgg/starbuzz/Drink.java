package edu.ucsd.wgg.starbuzz;

/**
 * Created by Bill on 9/17/2016.
 */
public class Drink {
    private String name;
    private String description;

    public static final Drink[] drinks = {
       new Drink("Latte", "A couple of espresso shots with steamed milk"),
       new Drink("Cappuccino", "Espresso, hot milk, and steamed foam"),
       new Drink("Filter", "Drip-brewed coffee")
    };

    protected Drink(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getDescription() { return description; }
    public String getName() { return name; }
    public String toString() { return name; }
}
