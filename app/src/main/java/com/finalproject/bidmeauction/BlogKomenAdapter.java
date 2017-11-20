package com.finalproject.bidmeauction;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by RAIIKA on 5/1/2017.
 */

public class BlogKomenAdapter extends RecyclerView.Adapter<BlogKomenAdapter.ViewHolder> {

    private LinkedList<Komen> mKomens;

    private DatabaseReference mDatabaseBlog;
    private DatabaseReference mDatabaseTime;
    private DatabaseReference mDatabaseBook;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseKomen;

    private String mPostKey = null;

    private FirebaseAuth mAuth;

    private Long currentTime = (long) 0;

    private boolean admin = false;

    public BlogKomenAdapter(String post_key) {
        super();

        mPostKey = post_key;

        mKomens = new LinkedList<Komen>();

        mAuth = FirebaseAuth.getInstance();

        mDatabaseBlog = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseTime = FirebaseDatabase.getInstance().getReference().child("Time");
        mDatabaseBook = FirebaseDatabase.getInstance().getReference().child("Book");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseKomen = FirebaseDatabase.getInstance().getReference().child("Komen");

        mDatabaseBlog.keepSynced(true);
        mDatabaseTime.keepSynced(true);
        mDatabaseBook.keepSynced(true);
        mDatabaseUser.keepSynced(true);
        mDatabaseKomen.keepSynced(true);

        mDatabaseKomen.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reloadComment(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void reloadComment(DataSnapshot dataSnapshot) {
        mKomens.clear();
        for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

            Komen komen = postSnapshot.getValue(Komen.class);

            if(komen.isBestkomen()){
                mKomens.addFirst(komen);
            }else {
                mKomens.add(komen);
            }

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.komen_row, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BlogKomenAdapter.ViewHolder viewHolder, final int position) {

        final Komen model = mKomens.get(position);

        viewHolder.setDesc(model.getDesc());
        viewHolder.setUsername(model.getUsername());
        viewHolder.setWaktu(model.getWaktu());
        viewHolder.setBestKomen(model.isBestkomen());

        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showDeleteBtn(dataSnapshot, model, viewHolder);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                selectBestComment(admin, model, v);

            }
        });

        viewHolder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setCancelable(true);
                builder.setTitle("Delete Post");
                builder.setMessage("Are you sure you want to delete this post ?");
                builder.setPositiveButton("Ofcourse",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mDatabaseKomen.addValueEventListener(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        mDatabaseKomen.child(mPostKey).child(model.getKomen_id()).removeValue();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

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
        });


    }

    private void selectBestComment(boolean isAdmin, final Komen model, final View v) {
        if(isAdmin){

            mDatabaseKomen.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Komen komenModel = dataSnapshot.child(mPostKey).child(model.getKomen_id()).getValue(Komen.class);

                    if(komenModel.isBestkomen()){

                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setCancelable(true);
                        builder.setTitle("Cancel this as best comment ?");
                        builder.setMessage(model.getDesc());
                        builder.setPositiveButton("Ofcourse",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        DatabaseReference newPost = mDatabaseKomen.child(mPostKey).child(model.getKomen_id());
                                        newPost.child("bestkomen").setValue(false);

                                    }
                                });
                        builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }else{

                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setCancelable(true);
                        builder.setTitle("Select this as best comment ?");
                        builder.setMessage(model.getDesc());
                        builder.setPositiveButton("Ofcourse",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        DatabaseReference newPost = mDatabaseKomen.child(mPostKey).child(model.getKomen_id());
                                        newPost.child("bestkomen").setValue(true);

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

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void showDeleteBtn(DataSnapshot dataSnapshot, Komen model, BlogKomenAdapter.ViewHolder viewHolder) {
        User userModel = dataSnapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class);

        if (model.getUsername().equals(userModel.getName()))
        {

            viewHolder.mDeleteBtn.setVisibility(View.VISIBLE);

        }

        admin = userModel.getType().equals("admin");
    }

    @Override
    public int getItemCount() {

        return mKomens.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  {


        View mView;

        TextView mDeleteBtn;

        TextView mBestKomen;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mDeleteBtn = (TextView) mView.findViewById(R.id.deleteBtn);
            mBestKomen = (TextView) mView.findViewById(R.id.post_best_komen);
        }

        public void setBestKomen(boolean b){
            if(b){mBestKomen.setVisibility(View.VISIBLE);}
            else{mBestKomen.setVisibility(View.GONE);}
        }

        public void setUsername(String username){

            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText(username);

        }

        public void setWaktu(Long waktu){

            TextView post_datetime = (TextView) mView.findViewById(R.id.post_datetime);
            post_datetime.setText(String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy / hh:mm a", new Date(waktu))));

        }

        public void setDesc(String desc){

            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);

        }

    }

}
