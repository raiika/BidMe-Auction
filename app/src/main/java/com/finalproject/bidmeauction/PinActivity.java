package com.finalproject.bidmeauction;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class PinActivity extends AppCompatActivity {

    private EditText pinNumber;

    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;

    private User userModel;
    private long back_pressed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Pin");
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        pinNumber = (EditText) findViewById(R.id.pin_number);

        pinNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count >= 6) {
                    checkPin(userModel);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if(mAuth.getCurrentUser()!=null) {
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(User.class);
                    if (userModel != null) {
                        if (userModel.getPin() == null) {
                            Intent setupPinIntent = new Intent(PinActivity.this, SetupPinActivity.class);
                            startActivity(setupPinIntent);
                            finish();
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void checkPin(User userModel) {
        if (userModel != null) {
            if (userModel.getPin() != null) {
                if (pinNumber.getText().toString().equals(userModel.getPin())) {
                    Toast.makeText(PinActivity.this, "Pin match", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(PinActivity.this, MainActivity.class);
                    mainIntent.putExtra("success_pin", "success");
                    startActivity(mainIntent);
                } else {
                    Toast.makeText(PinActivity.this, "Pin not match", Toast.LENGTH_SHORT).show();
                    pinNumber.setText("");
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 1000 > System.currentTimeMillis()) {
            Intent exitIntent = new Intent(PinActivity.this, ExitActivity.class);
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

    public void pressPinButton(View v) {
        pinNumber.setText(pinNumber.getText() + v.getTag().toString());
    }

    public void pressClearButton(View v) {
        pinNumber.setText("");
    }

    public void pressDeleteButton(View v) {
        pinNumber.setText(pinNumber.getText().delete(pinNumber.getText().length() - 1, pinNumber.getText().length()));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.pin_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.logout_pin) {

            mAuth.signOut();
            
            Intent mainIntent = new Intent(PinActivity.this, MainActivity.class);
            mainIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);

        }

        return super.onOptionsItemSelected(item);
    }
}
