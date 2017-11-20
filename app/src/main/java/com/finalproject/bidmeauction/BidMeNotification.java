package com.finalproject.bidmeauction;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by RAIIKA on 11/20/2017.
 */

public class BidMeNotification extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        context.startService(new Intent(context, BidMeNotificationService.class));
    }
}
