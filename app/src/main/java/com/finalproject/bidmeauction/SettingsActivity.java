package com.finalproject.bidmeauction;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private Switch mNotificationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pref = getApplicationContext().getSharedPreferences("BidMePref", MODE_PRIVATE);
        editor = pref.edit();

        final String settingNotification = pref.getString("notification", null);

        mNotificationSwitch = (Switch) findViewById(R.id.notification_switch);
        if (settingNotification == null || settingNotification.equals("enable")) {
            mNotificationSwitch.setChecked(true);
        } else {
            mNotificationSwitch.setChecked(false);
        }

        mNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putString("notification", "enable");
                    editor.apply();

                    Log.v("Raiika", String.valueOf(pref.getString("notification", null)));
                } else {
                    editor.putString("notification", "disable");
                    editor.apply();

                    Log.v("Raiika", String.valueOf(pref.getString("notification", null)));
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
