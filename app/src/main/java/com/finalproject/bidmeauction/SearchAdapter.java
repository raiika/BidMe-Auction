package com.finalproject.bidmeauction;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private LinkedList<Blog> mItems;

    private DatabaseReference mDatabaseBlog;
    private DatabaseReference mDatabaseTime;
    private DatabaseReference mDatabaseBook;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;

    private Long currentTime = (long) 0;

    private String searchValue;

    public SearchAdapter(final String searchValue) {
        super();

        mItems = new LinkedList<Blog>();

        mAuth = FirebaseAuth.getInstance();

        mDatabaseBlog = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseTime = FirebaseDatabase.getInstance().getReference().child("Time");
        mDatabaseBook = FirebaseDatabase.getInstance().getReference().child("Book");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabaseBlog.keepSynced(true);
        mDatabaseTime.keepSynced(true);
        mDatabaseBook.keepSynced(true);

        mDatabaseBlog.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mItems = getAllData(dataSnapshot, searchValue);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private LinkedList<Blog> getAllData(DataSnapshot dataSnapshot, final String searchValue) {

        final LinkedList<Blog> mDatas = new LinkedList<Blog>();

        final DataSnapshot dataSnapshotParent = dataSnapshot;

        mDatabaseBlog.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (final DataSnapshot postSnapshot: dataSnapshotParent.getChildren()) {
                    final Blog blog = postSnapshot.getValue(Blog.class);

                    if(blog.getTitle().contains(searchValue)){
                        mDatas.addFirst(blog);
                    }
                    else if(blog.getDesc().contains(searchValue))
                    {
                        mDatas.addLast(blog);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return mDatas;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.blog_row, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final SearchAdapter.ViewHolder viewHolder, final int position) {

        final Blog model = mItems.get(position);

        final String post_key = model.getAuction_id();

        viewHolder.setTitle(model.getTitle());
        viewHolder.setDesc(model.getDesc());
        viewHolder.setImage(model.getImage());


        mDatabaseUsers.child(model.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    //mDatabaseBlog.child(post_key).child("username").setValue(dataSnapshot.child("name").getValue(String.class));
                    viewHolder.setUsername(dataSnapshot.child("name").getValue(String.class));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent singleBlogIntent = new Intent(v.getContext(), BlogSingleActivity.class);
                singleBlogIntent.putExtra("blog_id", post_key);
                v.getContext().startActivity(singleBlogIntent);

            }
        });

        mDatabaseBook.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final Date[] time = {new Date()};
                mDatabaseTime.setValue(ServerValue.TIMESTAMP);
                mDatabaseTime.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        time[0] = new Date((long)dataSnapshot.getValue());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.mBookRoomBtn.setVisibility(View.VISIBLE);

                if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                    mDatabaseBlog.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(new Date((long)dataSnapshot.child(post_key).child("waktu").getValue()).before(time[0])){
                                viewHolder.mBookRoomBtn.setText("Join");
                            }else {
                                viewHolder.mBookRoomBtn.setText("Cancel Book");
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else{
                    mDatabaseBlog.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(new Date((long)dataSnapshot.child(post_key).child("waktu").getValue()).before(time[0])){
                                viewHolder.mBookRoomBtn.setText("Join");
                            }else {
                                viewHolder.mBookRoomBtn.setText("Book");
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                //new BookedAdapter().notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        viewHolder.mBookRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(viewHolder.mBookRoomBtn.getText().equals("Join")){
                    Intent singleBlogIntent = new Intent(v.getContext(), BidActivity.class);
                    singleBlogIntent.putExtra("blog_id", post_key);
                    v.getContext().startActivity(singleBlogIntent);
                }else if(viewHolder.mBookRoomBtn.getText().equals("Book")){
                    mDatabaseBook.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(model.getWaktu());
                }else{
                    mDatabaseBook.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                }

            }
        });

    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  {

        View mView;

        Button mBookRoomBtn;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mBookRoomBtn = (Button) mView.findViewById(R.id.joinRoomBtn);

        }

        public void setUsername(String username){

            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText("by : " + username);

        }

        public void setTitle(String title){
            String readmore = "";
            if(title.length() > 20){
                readmore = "...";
            }
            String upToNCharacters = title.substring(0, Math.min(title.length(), 20));


            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(upToNCharacters + readmore);

        }

        public void setDesc(String desc){
            String readmore = "";
            if(desc.length() > 20){
                readmore = "...";
            }
            String upToNCharacters = desc.substring(0, Math.min(desc.length(), 20));

            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(upToNCharacters + readmore);

        }

        public void setImage(String image){

            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(post_image.getContext().getApplicationContext()).load(image).into(post_image);

        }

    }

}
