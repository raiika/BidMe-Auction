package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SetupPinActivity extends AppCompatActivity {

    private EditText pinNumber;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    private String currentPin = "";

    private ProgressDialog mProgress;

    private boolean repeat = false; //false = first time, true = first input, if fail on second input back to false
    private long back_pressed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_pin);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Setup Pin (1/2)");
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress = new ProgressDialog(this);

        pinNumber = (EditText) findViewById(R.id.pin_number);

        pinNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count >= 6) {
                    if (repeat) {
                        if (pinNumber.getText().toString().equals(currentPin)) {

                            setupNewPin();

                        } else {
                            repeat = false;
                            currentPin = "";
                            pinNumber.setText("");
                            toolbar.setTitle("Setup Pin (1/2)");
                            Toast.makeText(SetupPinActivity.this, "Pin does not match, setup your pin again", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        currentPin = pinNumber.getText().toString();
                        repeat = true;
                        pinNumber.setText("");
                        toolbar.setTitle("Setup Pin (2/2)");
                        Toast.makeText(SetupPinActivity.this, "Repeat your pin", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void pressPinButton(View v) {
        pinNumber.setText(pinNumber.getText() + v.getTag().toString());
    }

    public void pressClearButton(View v) {
        pinNumber.setText("");
    }

    public void pressDeleteButton(View v) {
        if (pinNumber.getText().length() > 0) {
            pinNumber.setText(pinNumber.getText().delete(pinNumber.getText().length() - 1, pinNumber.getText().length()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.pin_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setupNewPin() {

        mProgress.setMessage("Pin match, Requesting server to store the pin");
        mProgress.setCancelable(false);
        mProgress.show();

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("pin").setValue(currentPin).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Intent pinIntent = new Intent(SetupPinActivity.this, PinActivity.class);
                pinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(pinIntent);

                finish();

                Toast.makeText(SetupPinActivity.this, "Success setup pin", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 1000 > System.currentTimeMillis()) {
            Intent exitIntent = new Intent(SetupPinActivity.this, ExitActivity.class);
            exitIntent.addFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
            exitIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(exitIntent);
        } else {
            Toast.makeText(getBaseContext(),
                    "Press once again to exit!", Toast.LENGTH_SHORT)
                    .show();
        }
        back_pressed = System.currentTimeMillis();

    }
}
