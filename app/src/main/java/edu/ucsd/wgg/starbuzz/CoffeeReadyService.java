package edu.ucsd.wgg.starbuzz;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CoffeeReadyService extends IntentService {

    public CoffeeReadyService() {
        super("CoffeeReadyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String drinkName = intent.getStringExtra(getString(R.string.drink_name_flag));
            final int prepTime = intent.getIntExtra(getString(R.string.prep_time_flag), 10);
            final String message = "Your " + " is ready!";  // TODO - FIX

            synchronized (this) {
                try { wait(prepTime * 1000); } catch (InterruptedException e) { e.printStackTrace(); }
            }
            showMessage(message);
        }
    }

    /*
     * OMG the things we have to do to post a notification.  First, we need to set up the Activity
     * to be activated when the notification is clicked.  Then we need to build the notification
     * with a Builder (using the Builder pattern).  Finally, we get to post the notification.
     */
    protected void showMessage(String message) {
        // WGG - if we were really clever here we'd remember the last page we were on
        Intent mainIntent = new Intent(this,TopLevelActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TopLevelActivity.class);
        stackBuilder.addNextIntent(mainIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .build();

        final int NOTIFICATION_ID = 5453; // made up
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


}
