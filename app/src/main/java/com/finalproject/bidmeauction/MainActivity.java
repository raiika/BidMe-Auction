package com.finalproject.bidmeauction;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public TextView noData;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    SwipeRefreshLayout mySwipeRefreshLayout;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseTime;
    private DatabaseReference mDatabaseNotification;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Navigation Menu
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigationView;
    //Navigation Header
    private TextView mNavTeksName;
    private TextView mNavTeksSaldo;
    private ImageView mNavProfileImage;
    private String afterPin = null;
    private boolean admin = false;
    private User userModel;
    private MaterialSearchView searchView;
    private int trueBalance;
    private long back_pressed = 0;
    private boolean disableOnce;
    Thread thread = new Thread() {
        @Override
        public void run() {

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
                                            new NotificationCompat.Builder(MainActivity.this)
                                                    .setSmallIcon(R.mipmap.ic_launcher)
                                                    .setContentTitle(dataSnapshot.child("abc").child("title").getValue(String.class))
                                                    .setContentText(dataSnapshot.child("abc").child("desc").getValue(String.class));

                                    int mNotificationId = 001;
                                    NotificationManager mNotifyMgr =
                                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pref = getApplicationContext().getSharedPreferences("BidMePref", MODE_PRIVATE);
        editor = pref.edit();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseTime = FirebaseDatabase.getInstance().getReference().child("Time");
        mDatabaseNotification = FirebaseDatabase.getInstance().getReference().child("Notification");

        //mDatabase.keepSynced(true);
        //mDatabaseUsers.keepSynced(true);
        //mDatabaseTime.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = (RecyclerView) findViewById(R.id.blog_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MainAdapter();
        mRecyclerView.setAdapter(mAdapter);

        searchView = (MaterialSearchView)findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                doSearch(s);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }

        });

        //Navigation Menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Swipe to REFRESH

        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        mySwipeRefreshLayout.setRefreshing(true);
                        mAdapter.notifyDataSetChanged();

                        if(mAdapter.getItemCount() <1){
                            noData.setVisibility(View.VISIBLE);
                        }
                        else{
                            noData.setVisibility(View.GONE);
                        }
                        mySwipeRefreshLayout.setRefreshing(false);

                    }
                }
        );

        noData = (TextView) findViewById(R.id.main_no_data);

        noData.setVisibility(View.GONE);
        ValueEventListener auctionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAdapter.notifyDataSetChanged();
                mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        Log.v("RAIIKA", "KEPRINT GA YA ?");
                        if(mAdapter.getItemCount() <1){
                            noData.setVisibility(View.VISIBLE);
                        }
                        else{
                            noData.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.removeEventListener(auctionListener);
        mDatabase.addValueEventListener(auctionListener);

        //Access to navigation header
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        mNavTeksName = (TextView) headerView.findViewById(R.id.nav_teks_name);
        mNavTeksSaldo = (TextView) headerView.findViewById(R.id.nav_teks_saldo);
        mNavProfileImage = (ImageView) headerView.findViewById(R.id.nav_profile_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() == null){

                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();

                }

            }
        };

        if(mAuth.getCurrentUser()!=null) {

            ValueEventListener userNavListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {

                        userModel = dataSnapshot.getValue(User.class);
                        if (userModel.getName() != null && userModel.getImage() != null ) {
                            if(userModel.getType() != null) {
                                if (userModel.getType().equals("admin")) {
                                    mNavTeksName.setText("[Admin] " + userModel.getName());
                                    mDatabaseNotification.child("abc").child("title").setValue("si " + userModel.getName() + " login");
                                    mDatabaseNotification.child("abc").child("desc").setValue("Sebagai Admin");
                                } else {
                                    mNavTeksName.setText(userModel.getName());
                                    mDatabaseNotification.child("abc").child("title").setValue("si " + userModel.getName() + " login");
                                    mDatabaseNotification.child("abc").child("desc").setValue("Sebagai User");
                                }
                            }
                            Picasso.with(getApplicationContext()).load(userModel.getImage()).transform(new additionalMethod.CircleTransform()).into(mNavProfileImage);
                        }

                        if (checkIsAdmin(userModel)) {

                            Menu nav_Menu = navigationView.getMenu();
                            nav_Menu.findItem(R.id.nav_add_admin).setVisible(true);

                        }
                        checkBidBalance();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).removeEventListener(userNavListener);
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(userNavListener);

            checkPinSuccess();
        }

        checkUserExist();


        startService(new Intent(MainActivity.this, BidMeNotificationService.class));

        //thread.start();
    }

    private void notificationPermission() {

        final String settingNotification = pref.getString("notification", null);

        if (settingNotification == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Set Notification");
            builder.setMessage("Do you want to receive Notification ?");
            builder.setPositiveButton("Ofcourse",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putString("notification", "enable");
                            editor.commit();
                        }
                    });
            builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editor.putString("notification", "disable");

                    editor.commit();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkBidBalance() {
        ValueEventListener updateBalance = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                trueBalance = userModel.getBalance();

                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){

                    Blog auction = postSnapshot.getValue(Blog.class);

                    if(auction.getBiduid()!=null) {
                        if (auction.getBiduid().equals(mAuth.getCurrentUser().getUid())) {

                            trueBalance -= auction.getBid();

                        }
                    }

                }

                mNavTeksSaldo.setText(additionalMethod.getRupiahFormattedString(trueBalance));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.removeEventListener(updateBalance);
        mDatabase.addValueEventListener(updateBalance);
    }

    private boolean checkIsAdmin(User userModel) {
        if(userModel.getType() != null) {
            if (userModel.getType().equals("admin")) {
                return true;
            }
        }

        return false;
    }

    private void doSearch(String s) {
        Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
        searchIntent.putExtra("searchValue", s);
        startActivity(searchIntent);
    }

    private void checkPinSuccess() {
        afterPin = getIntent().getStringExtra("success_pin");
        if(afterPin == null) {
            Intent pinIntent = new Intent(MainActivity.this, PinActivity.class);
            startActivity(pinIntent);
            finish();
        } else {
            notificationPermission();
        }
    }

    @Override
    public void onBackPressed() {
/*
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Exit BidMe");
        builder.setMessage("Are you sure you want to exit ?");
        builder.setPositiveButton("Ofcourse",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent exitIntent = new Intent(MainActivity.this, ExitActivity.class);
                        exitIntent.addFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                        exitIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(exitIntent);

                    }
                });
        builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();*/
        if (back_pressed + 1000 > System.currentTimeMillis()) {
            Intent exitIntent = new Intent(MainActivity.this, ExitActivity.class);
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

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

    }

    private void checkUserExist() {

        if(mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();

            ValueEventListener userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                        finish();

                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mDatabaseUsers.removeEventListener(userListener);
            mDatabaseUsers.addValueEventListener(userListener);
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {


        if (item.getItemId() == R.id.nav_logout){

            mAuth.signOut();

        }

        if (item.getItemId() == R.id.nav_account){

            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);

        }

        if (item.getItemId() == R.id.nav_booked){

            Intent bookedIntent = new Intent(MainActivity.this, BookedActivity.class);
            startActivity(bookedIntent);

        }

        if (item.getItemId() == R.id.nav_history){

            Toast.makeText(MainActivity.this, "Not available yet", Toast.LENGTH_SHORT).show();

        }

        if (item.getItemId() == R.id.nav_upcoming){

            Intent upcomingIntent = new Intent(MainActivity.this, UpcomingActivity.class);
            startActivity(upcomingIntent);

        }

        if (item.getItemId() == R.id.nav_add_admin){

            Intent registerAdminIntent = new Intent(MainActivity.this, RegisterAdminActivity.class);
            startActivity(registerAdminIntent);

        }

        if (item.getItemId() == R.id.nav_setting){

            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        ValueEventListener adminListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue(String.class) != null) {
                    if (dataSnapshot.getValue(String.class).equals("admin")) {

                        MenuItem itemAddAuction = menu.findItem(R.id.action_add);
                        itemAddAuction.setVisible(true);

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        if (mAuth.getCurrentUser() != null) {
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("type").removeEventListener(adminListener);
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("type").addValueEventListener(adminListener);
        }

        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }

        if (item.getItemId() == R.id.action_add){

            Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
            startActivity(postIntent);

        }

        return super.onOptionsItemSelected(item);
    }

}
