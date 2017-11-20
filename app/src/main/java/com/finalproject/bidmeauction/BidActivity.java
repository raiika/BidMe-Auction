package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.StringTokenizer;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class BidActivity extends AppCompatActivity {

    Blog modelAuction = null;
    User modelUser = null;
    Handler handler = new Handler();
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseTime;
    private String mPost_key = null;
    private TextView mBidUsername;
    private TextView mBidBid;
    private TextView mCountDown;
    private TextView mTrueBalance;
    private EditText mBidTeks;
    private Button mBidBtn;
    private Button mBidQuick;
    private long realTimeServer;
    private final Runnable run = new Runnable() {
        @Override
        public void run() {
            mDatabaseTime.setValue(ServerValue.TIMESTAMP);


            mDatabaseTime.setValue(ServerValue.TIMESTAMP);
            mDatabaseTime.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    realTimeServer = dataSnapshot.getValue(long.class);

                    if (new Date(realTimeServer).before(new Date(modelAuction.getTutup())) || isAdmin()) {

                        mCountDown.setText(String.valueOf((int) (modelAuction.getTutup() - dataSnapshot.getValue(long.class)) / 1000) + " Seconds left");

                    } else {

                        mCountDown.setText(String.valueOf((int) (modelAuction.getTutup() - dataSnapshot.getValue(long.class)) / 1000) + " Seconds left");
                        mBidTeks.setEnabled(false);
                        mBidBtn.setEnabled(false);
                        mBidQuick.setEnabled(false);
                        Log.v(String.valueOf(new Date(dataSnapshot.getValue(long.class))), String.valueOf(new Date(modelAuction.getTutup())));
                        mDatabase.child(mPost_key).child("available").setValue(false);
                        Intent mainIntent = new Intent(BidActivity.this, MainActivity.class);
                        mainIntent.addFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                        mainIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                        mainIntent.putExtra("success_pin", "success");
                        Toast.makeText(BidActivity.this, "Auction is finished", Toast.LENGTH_SHORT).show();
                        startActivity(mainIntent);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            handler.postDelayed(this, 1000);

        }
    };
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;
    private int bid_total = 0;
    private int bid_plus = 0;
    private String bid_name = "";
    private int trueBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid);

        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        mPost_key = getIntent().getExtras().getString("blog_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseTime = FirebaseDatabase.getInstance().getReference().child("Time");

        mDatabase.keepSynced(true);

        mBidTeks = (EditText) findViewById(R.id.bid_teks);
        mBidBid = (TextView) findViewById(R.id.bid_bid);
        mBidUsername = (TextView) findViewById(R.id.bid_username);
        mCountDown = (TextView) findViewById(R.id.countDown);
        mTrueBalance = (TextView) findViewById(R.id.true_balance);
        mBidBtn = (Button) findViewById(R.id.bid_btn);
        mBidQuick = (Button) findViewById(R.id.bid_quick);

        mDatabaseUser.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                modelUser = dataSnapshot.getValue(User.class);

                bid_name = modelUser.getName();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        checkBidBalance();

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                modelAuction = dataSnapshot.getValue(Blog.class);
                checkHighestBid(modelAuction);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mBidTeks.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mBidBtn.performClick();
                    return true;
                }
                return false;
            }
        });

        mBidTeks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                int cursorPosition = mBidTeks.getSelectionEnd();
                String originalStr = mBidTeks.getText().toString();

                //To restrict only two digits after decimal place
                mBidTeks.setFilters(new InputFilter[]{new MoneyValueFilter(2)});

                try {
                    mBidTeks.removeTextChangedListener(this);
                    String value = mBidTeks.getText().toString();

                    if (value != null && !value.equals("")) {
                        if (value.startsWith(".")) {
                            mBidTeks.setText("0.");
                        }
                        if (value.startsWith("0") && !value.startsWith("0.")) {
                            mBidTeks.setText("");
                        }
                        String str = mBidTeks.getText().toString().replaceAll(",", "");
                        if (!value.equals(""))
                            mBidTeks.setText(additionalMethod.getDecimalFormattedString(str));

                        int diff = mBidTeks.getText().toString().length() - originalStr.length();
                        mBidTeks.setSelection(cursorPosition + diff);
                    }
                    mBidTeks.addTextChangedListener(this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mBidTeks.addTextChangedListener(this);
                }
            }

        });

        handler.post(run);

    }

    private void checkBidBalance() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                trueBalance = modelUser.getBalance();

                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){

                    Blog auction = postSnapshot.getValue(Blog.class);

                    if(auction.getBiduid()!=null) {
                        if (auction.getBiduid().equals(mAuth.getCurrentUser().getUid()) && !auction.getAuction_id().equals(mPost_key)) {

                            trueBalance -= auction.getBid();

                        }
                    }

                }

                mTrueBalance.setText(additionalMethod.getRupiahFormattedString(trueBalance));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(run);
    }


    public void performQuickBid(View v) {
        bid_total = bid_total + (bid_total / 20);

        if(trueBalance >= bid_total) {

            final DatabaseReference newPost = mDatabase.child(mPost_key);

            newPost.child("bid").setValue(bid_total);

            newPost.child("bidname").setValue(bid_name);

            newPost.child("biduid").setValue(mCurrentUser.getUid());

            updateTutup(newPost);

            mBidTeks.setText("");
        }else{
            Toast.makeText(BidActivity.this, "Could not use Quick Bid because your balance is not enough", Toast.LENGTH_SHORT).show();
        }
    }

    public void performBid(View v) {
        String bidTeks = mBidTeks.getText().toString().replace(",","");
        if (bidTeks.equals("")) {
            Toast.makeText(BidActivity.this, "Input your amount", Toast.LENGTH_SHORT).show();
        } else if (Integer.parseInt(bidTeks) <= bid_total) {
            Toast.makeText(BidActivity.this, "Your Ammount is less than the highest bid", Toast.LENGTH_SHORT).show();
        } else if (trueBalance < Integer.parseInt(bidTeks)){
            Toast.makeText(BidActivity.this, "Could not Bid because your balance is not enough", Toast.LENGTH_SHORT).show();
        }
        else{
            final DatabaseReference newPost = mDatabase.child(mPost_key);
            newPost.child("bid").setValue(Integer.parseInt(bidTeks));
            newPost.child("bidname").setValue(bid_name);
            newPost.child("biduid").setValue(mCurrentUser.getUid());
            updateTutup(newPost);
        }

        mBidTeks.setText("");
    }

    private void updateTutup(final DatabaseReference newPost) {
        mDatabaseTime.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(modelAuction.getTutup() - dataSnapshot.getValue(long.class) < 480000) {
                    newPost.child("tutup").setValue(dataSnapshot.getValue(long.class) + 480000);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkHighestBid(Blog model) {
        if(model != null) {
            bid_total = model.getBid();

            String rupiahFormat = additionalMethod.getRupiahFormattedString(model.getBid());
            mBidBid.setText(rupiahFormat);

            mBidUsername.setText(model.getBidname());

            if (model.getBiduid().equals(mCurrentUser.getUid())) {
                mBidTeks.setEnabled(false);
                mBidBtn.setEnabled(false);
                mBidQuick.setEnabled(false);
            } else {
                mBidTeks.setEnabled(true);
                mBidBtn.setEnabled(true);
                mBidQuick.setEnabled(true);
            }

            bid_plus = bid_total / 20;

            mBidQuick.setText("QUICK BID - " + String.valueOf(bid_total + bid_plus));
        }
    }

    public void goToUserProfile(View v) {

        if (new Date(realTimeServer).after(new Date(modelAuction.getTutup())) && isAdmin()) {
            Intent profileIntent = new Intent(BidActivity.this, ProfileActivity.class);
            profileIntent.putExtra("userID", modelAuction.getBiduid());
            startActivity(profileIntent);
        }

    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public boolean isAdmin() {
        return modelUser.getType().equals("admin");

    }

    class MoneyValueFilter extends DigitsKeyListener {
        private int digits;

        public MoneyValueFilter(int i) {
            super(false, true);
            digits = i;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            CharSequence out = super.filter(source, start, end, dest, dstart, dend);

            // if changed, replace the source
            if (out != null) {
                source = out;
                start = 0;
                end = out.length();
            }

            int len = end - start;

            // if deleting, source is empty
            // and deleting can't break anything
            if (len == 0) {
                return source;
            }

            int dlen = dest.length();

            // Find the position of the decimal .
            for (int i = 0; i < dstart; i++) {
                if (dest.charAt(i) == '.') {
                    // being here means, that a number has
                    // been inserted after the dot
                    // check if the amount of digits is right
                    return additionalMethod.getDecimalFormattedString((dlen - (i + 1) + len > digits) ? "" : String.valueOf(new SpannableStringBuilder(source, start, end)));
                }
            }

            for (int i = start; i < end; ++i) {
                if (source.charAt(i) == '.') {
                    // being here means, dot has been inserted
                    // check if the amount of digits is right
                    if ((dlen - dend) + (end - (i + 1)) > digits)
                        return "";
                    else
                        break; // return new SpannableStringBuilder(source,
                    // start, end);
                }
            }

            // if the dot is after the inserted part,
            // nothing can break
            return additionalMethod.getDecimalFormattedString(String.valueOf(new SpannableStringBuilder(source, start, end)));
        }
    }

}


