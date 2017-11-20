package com.finalproject.bidmeauction;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by RAIIKA on 11/20/2017.
 */

public class BidMeNotificationService extends Service {

    private boolean disableOnce;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private DatabaseReference mDatabaseNotification;

    private Context context = this;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent pIntent, int flags, int startId) {

        pref = context.getSharedPreferences("BidMePref", MODE_PRIVATE);
        editor = pref.edit();
        mDatabaseNotification = FirebaseDatabase.getInstance().getReference().child("Notification");

        disableOnce = false;
        ValueEventListener notificationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (disableOnce) {
                    String settingNotification = pref.getString("notification", null);
                    if (!(settingNotification == null || settingNotification.equals("disable"))) {
                        Log.v("Raiika", String.valueOf(settingNotification));
                        if (dataSnapshot.child("abc") != null) {
                            if (dataSnapshot.child("abc").child("title").getValue(String.class) != null && dataSnapshot.child("abc").child("desc").getValue(String.class) != null) {

                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(context)
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setContentTitle(dataSnapshot.child("abc").child("title").getValue(String.class))
                                                .setContentText(dataSnapshot.child("abc").child("desc").getValue(String.class));

                                int mNotificationId = 001;
                                NotificationManager mNotifyMgr =
                                        (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                                mNotifyMgr.notify(mNotificationId, mBuilder.build());

                            }

                        }
                    } else {

                        Log.v("Raiika", String.valueOf(settingNotification));
                    }
                }
                disableOnce = true;


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseNotification.removeEventListener(notificationListener);
        mDatabaseNotification.addValueEventListener(notificationListener);

        return super.onStartCommand(pIntent, flags, startId);
    }
}
