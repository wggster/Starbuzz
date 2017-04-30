package edu.ucsd.wgg.starbuzz;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.locks.Lock;

public class DrinkActivity extends AppCompatActivity {

    public static final String EXTRA_DRINKNUM = "drinkNum";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);

        int drinkNum = (Integer) getIntent().getExtras().get(EXTRA_DRINKNUM);
        Drink drink = Drink.drinks[drinkNum];

        TextView name = (TextView) findViewById(R.id.name);
        name.setText(drink.getName());

        TextView desc = (TextView) findViewById(R.id.description);
        desc.setText(drink.getDescription());
    }

    public void onClickOrder(View view) {
        final int prepTimeSecs = 5;
        new OrderDrinkTask().execute(prepTimeSecs);

        TextView name = (TextView) findViewById(R.id.name);
        Intent intent = new Intent(this, CoffeeReadyService.class);
        intent.putExtra(getString(R.string.drink_name_flag), (String) name.getText());
        intent.putExtra(getString(R.string.prep_time_flag), prepTimeSecs);
        startService(intent);
    }

    private class OrderDrinkTask extends AsyncTask<Integer, Integer, Boolean> {
        protected void onPreExecute() {
            TextView progress = (TextView) findViewById(R.id.progress);
            progress.setText(".");
        }
        protected Boolean doInBackground(Integer... waitSecs) {
            for (int i = 1; i <= waitSecs[0]; i++) {
                synchronized (this) {
                    try { wait(1000); } catch (InterruptedException e) { e.printStackTrace(); }
                    publishProgress(i);
                    Log.v("OrderDrinkTask", "doInBackground: " + i);
                }

            }
            return true;
        }
        protected void onProgressUpdate(Integer... values) {
            TextView progress = (TextView) findViewById(R.id.progress);
            progress.setText(progress.getText() + ".");
        }
        protected void onPostExecute(Boolean ready) {
            Toast.makeText(getApplicationContext(), "Your Coffee is Ready!", Toast.LENGTH_LONG).show();
        }
    }
}
