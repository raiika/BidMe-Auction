package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class BlogSingleActivity extends AppCompatActivity {

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    private RecyclerView mKomenList;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseKomen;
    private DatabaseReference mDatabaseBook;
    private DatabaseReference mDatabaseBid;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseLike;
    private FirebaseStorage mFirebaseStorage;
    private RecyclerView mRecyclerView;
    private String mPost_key = null;

    private ImageView mBlogSingleImage;
    private TextView mBlogSingleTitle;
    private TextView mBlogSingleDesc;

    private EditText mKomenTeks;

    private Button mKomenBtn;

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog mProgress;

    private FloatingActionButton mFloatingBtn;

    private NestedScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        mPost_key = getIntent().getStringExtra("blog_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseKomen = FirebaseDatabase.getInstance().getReference().child("Komen");
        mDatabaseBid = FirebaseDatabase.getInstance().getReference().child("Bid");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseBook = FirebaseDatabase.getInstance().getReference().child("Book");
        //mDatabaseTests = FirebaseDatabase.getInstance().getReference().child("Ping");
        mFirebaseStorage = FirebaseStorage.getInstance();

        mDatabase.keepSynced(true);
        mDatabaseKomen.keepSynced(true);

        mBlogSingleDesc = (TextView) findViewById(R.id.singleBlogDescription);
        mBlogSingleImage = (ImageView) findViewById(R.id.singleBlogView);
        mBlogSingleTitle = (TextView) findViewById(R.id.singleBlogTitle);
        mKomenTeks = (EditText) findViewById(R.id.komenTeks);
        mKomenBtn = (Button) findViewById(R.id.komenBtn);

        mFloatingBtn = (FloatingActionButton) findViewById(R.id.floating_button);
        mScrollView = (NestedScrollView) findViewById(R.id.scroll_view);

        mRecyclerView = (RecyclerView) findViewById(R.id.komen_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new BlogKomenAdapter(mPost_key);
        mRecyclerView.setAdapter(mAdapter);

        mFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScrollView.smoothScrollTo(0,0);
            }
        });

        mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY<2000){
                    mFloatingBtn.setVisibility(View.GONE);
                }else{
                    mFloatingBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        mKomenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startKomenting();
            }
        });

        mDatabaseKomen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Blog model = dataSnapshot.getValue(Blog.class);
                if(model != null){
                    mBlogSingleTitle.setText(model.getTitle());
                    mBlogSingleDesc.setText(model.getDesc());
                    Picasso.with(BlogSingleActivity.this).load(model.getImage()).into(mBlogSingleImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void startKomenting() {

        final String desc_val = mKomenTeks.getText().toString().trim();

        mProgress.setMessage("Posting Your Komen...");
        mProgress.setCancelable(false);

        if(!TextUtils.isEmpty(desc_val)) {

            mProgress.show();

            final DatabaseReference newPost = mDatabaseKomen.child(mPost_key).push();

            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    newPost.child("username").setValue(dataSnapshot.child(mCurrentUser.getUid()).child("name").getValue().toString());
                    newPost.child("desc").setValue(desc_val);
                    newPost.child("waktu").setValue(ServerValue.TIMESTAMP);
                    newPost.child("komen_id").setValue(newPost.getKey());
                    newPost.child("bestkomen").setValue(false);

                    Toast.makeText(BlogSingleActivity.this, "Success Commenting", Toast.LENGTH_LONG).show();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mProgress.dismiss();
        }

        mKomenTeks.setText("");

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.single_blog_menu, menu);

        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(mCurrentUser.getUid()).child("type").getValue().toString().equals("admin")) {
                    MenuItem itemDelete = menu.findItem(R.id.action_delete);
                    itemDelete.setVisible(true);
                    MenuItem itemEdit = menu.findItem(R.id.action_edit);
                    itemEdit.setVisible(true);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Delete Post");
            builder.setMessage("Are you sure you want to delete this post ?");
            builder.setPositiveButton("Ofcourse",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            finish();
                            Intent mainIntent = new Intent(BlogSingleActivity.this, MainActivity.class);
                            startActivity(mainIntent);

                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(dataSnapshot.child(mPost_key).child("image").getValue().toString());

                                    photoRef.delete();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            mDatabase.child(mPost_key).removeValue();
                            mDatabaseKomen.child(mPost_key).removeValue();
                            mDatabaseBid.child(mPost_key).removeValue();
                            mDatabaseLike.child(mPost_key).removeValue();
                            mDatabaseBook.child(mPost_key).removeValue();

                        }
                    });
            builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }

        if(item.getItemId() == R.id.action_edit){
            Intent editPostIntent = new Intent(BlogSingleActivity.this, EditPostActivity.class);
            editPostIntent.putExtra("blog_id", mPost_key);
            startActivity(editPostIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
