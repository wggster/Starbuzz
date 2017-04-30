package edu.ucsd.wgg.starbuzz;

import android.os.Bundle;
import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.content.Intent;

/*
 * A ListActivity is a top-level ListView, and comes with a built-in layout, so we don't need
 * to set up or connect to the layout.  However, unlike in ListView, you don't need to set up
 * your own Listener.  You just implement onListItemClick(), like we did with previous activities.
 *
 * In the example below, instead of defining the list items with a static array from the arrays.xml
 * resource, we define the list programmatically, to model the situation that the list is
 * constructed on-the-fly based on the context (e.g., retrieving menu items from a live database).
 * This requires explicitly setting up the ArrayAdapter, which the ListView constructor requires.
 * (In the static case, the ListView constructor is called by the IDE when processing the array ref.)
 * The use of an ArrayAdapter provides additional flexibility by allowing different types of arrays,
 * e.g., Java arrays or Collection class Lists.  While you could build this into the ListView
 * constructor itself (having lots of different constructors, we typically factor out flexibility
 * like type adaptation to a separate class.
 */
public class DrinkCategoryActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listDrinks = getListView();
        ArrayAdapter<Drink> listAdapter = new ArrayAdapter<Drink>(this, android.R.layout.simple_list_item_1, Drink.drinks);
        listDrinks.setAdapter(listAdapter);
    }

    @Override
    /*
     * This is the Listener method that we need to implement, no messy anonymous class!  While
     * this looks a lot like what we did in the early chapters with onItemClick() methods and
     * what-not, those were not true listeners with pre-defined interfaces.  We we just binding
     * a method implementation to a click action via the layout declaration.
     */
    public void onListItemClick(ListView listView, View itemView, int position, long id) {
        // WGG - why do I need "DrinkCategoryActivity.this" instead of just "this"?
        // I don't see a scoping problem with a nest anonymous class or anything.
        Intent intent = new Intent(DrinkCategoryActivity.this, DrinkActivity.class);
        // WGG - why do I have to cast long to int here?  Just quiet the warnings?
        intent.putExtra(DrinkActivity.EXTRA_DRINKNUM, (int) id);
        startActivity(intent);
    }
}
