package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseVoucher;
    private StorageReference mStorageImage;
    private FirebaseStorage mFirebaseStorage;
    private TextView mProfileEmail;
    private TextView mProfileUsername;
    private TextView mProfilePhone;
    private TextView mProfileAddress;
    private TextView mProfileBalance;
    private Button mChangePasswordBtn;
    private Button mTopUpBtn;
    private Button mAddTopUpBtn;
    private ImageButton mProfileImage;
    private ProgressDialog mProgress;
    private Uri mImageUri = null;
    private User userModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileEmail = (TextView) findViewById(R.id.profile_email);
        mProfileUsername = (TextView) findViewById(R.id.profile_username);
        mProfilePhone = (TextView) findViewById(R.id.profile_phone);
        mProfileAddress = (TextView) findViewById(R.id.profile_address);
        mProfileBalance = (TextView) findViewById(R.id.profile_balance);
        mProfileImage = (ImageButton) findViewById(R.id.profile_image);
        mChangePasswordBtn = (Button) findViewById(R.id.change_password_btn);
        mTopUpBtn = (Button) findViewById(R.id.top_up_btn);
        mAddTopUpBtn = (Button) findViewById(R.id.add_top_up_btn);
        mProgress = new ProgressDialog(this);

        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
        mFirebaseStorage = FirebaseStorage.getInstance();

        mAuth = FirebaseAuth.getInstance();

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseVoucher = FirebaseDatabase.getInstance().getReference().child("Voucher");

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModel = dataSnapshot.getValue(User.class);
                showUserProfile(userModel);

                if(userModel.getType().equals("admin")){
                    mAddTopUpBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfileImage();
            }
        });

        mProfileUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName();
            }
        });

        mProfileAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAddress();
            }
        });

        mProfilePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePhone();
            }
        });

        mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        mTopUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topUp();
            }
        });

        mAddTopUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTopUp();
            }
        });

        for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if(user.getProviderId().equals("google.com")){
                mChangePasswordBtn.setVisibility(View.GONE);
            }
        }

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void addTopUp() {
        Context context = ProfileActivity.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText voucher = new EditText(context);
        voucher.setHint("10 alphanumeric voucher");
        layout.addView(voucher);

        final EditText value = new EditText(context);
        value.setHint("value");
        layout.addView(value);

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Add new voucher")
                .setMessage("Additional information goes here")
                .setView(layout)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        mProgress.setMessage("Adding new voucher");
                        mProgress.show();
                        mDatabaseVoucher.child(voucher.getText().toString()).setValue(Integer.parseInt(value.getText().toString()));
                        mProgress.dismiss();
                        }
                    })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .show();

    }

    private void topUp() {
        final EditText mTeksVoucher = new EditText(ProfileActivity.this);

        mTeksVoucher.setHint("a voucher consist 10 alphanumeric");

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Input your 10 alphanumeric voucher in here")
                .setMessage("Additional information goes here")
                .setView(mTeksVoucher)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mDatabaseVoucher.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild(mTeksVoucher.getText().toString())){
                                int add_balance = dataSnapshot.child(mTeksVoucher.getText().toString()).getValue(Integer.class);
                                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("balance").setValue(userModel.getBalance()+add_balance);
                                mDatabaseVoucher.child(mTeksVoucher.getText().toString()).removeValue();
                            }else{
                                Toast.makeText(ProfileActivity.this, "Invalid Voucher", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();

    }

    private void showUserProfile(User inUserModel) {
        mProfileEmail.setText(mAuth.getCurrentUser().getEmail());
        if(inUserModel.getName()!=null){
            mProfileUsername.setText(inUserModel.getName());
        }
        else{
            mProfileUsername.setText("click here to set your name");
        }
        if(inUserModel.getImage()!=null) {
            Picasso.with(getApplicationContext()).load(inUserModel.getImage()).transform(new additionalMethod.CircleTransform()).into(mProfileImage);
        }
        if(inUserModel.getAddress()!=null) {
            mProfileAddress.setText(inUserModel.getAddress());
        }else{
            mProfileAddress.setText("click here to set your address");
        }
        if(inUserModel.getPhone()!=null){
            mProfilePhone.setText(inUserModel.getPhone());
        }else{
            mProfilePhone.setText("click here to set your phone");
        }
        mProfileBalance.setText(additionalMethod.getRupiahFormattedString(inUserModel.getBalance()));
    }

    private void changeProfileImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    private void changeName() {
        final EditText mTeksUser = new EditText(ProfileActivity.this);

        mTeksUser.setHint(mProfileUsername.getText());

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Input your new name")
                .setMessage("Additional information goes here")
                .setView(mTeksUser)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String username = mTeksUser.getText().toString();
                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("name").setValue(username);
                        Toast.makeText(ProfileActivity.this,"Success changing your name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void changeAddress() {
        final EditText mTeksUser = new EditText(ProfileActivity.this);

        mTeksUser.setHint(mProfileAddress.getText());

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Input your new address")
                .setMessage("Additional information goes here")
                .setView(mTeksUser)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String address = mTeksUser.getText().toString();
                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("address").setValue(address);
                        Toast.makeText(ProfileActivity.this,"Success changing your address", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void changePhone() {
        final EditText mTeksUser = new EditText(ProfileActivity.this);
        mTeksUser.setInputType(InputType.TYPE_CLASS_NUMBER);
        mTeksUser.setHint(mProfilePhone.getText());

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Input your new phone number")
                .setMessage("Additional information goes here")
                .setView(mTeksUser)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String address = mTeksUser.getText().toString();
                        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("phone").setValue(address);
                        Toast.makeText(ProfileActivity.this,"Success changing your phone number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void changePassword() {
        Context context = ProfileActivity.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText oldPassword = new EditText(context);
        oldPassword.setHint("Old Password");
        layout.addView(oldPassword);

        final EditText newPassword1 = new EditText(context);
        newPassword1.setHint("New Password");
        layout.addView(newPassword1);

        final EditText newPassword2 = new EditText(context);
        newPassword2.setHint("Repeat New Password");
        layout.addView(newPassword2);

        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Input your old password")
                .setMessage("Additional information goes here")
                .setView(layout)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        mProgress.setMessage("checking Old Password");
                        mProgress.show();
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        AuthCredential credential = EmailAuthProvider
                                .getCredential(mAuth.getCurrentUser().getEmail(), oldPassword.getText().toString());

                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {


                                        if (task.isSuccessful()) {

                                            if(newPassword1.getText().toString().equals(newPassword2.getText().toString())) {

                                                user.updatePassword(newPassword1.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(ProfileActivity.this, "Success updating password", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(ProfileActivity.this, "Unknown Error in updating password", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            }else{
                                                Toast.makeText(ProfileActivity.this, "New password does not match", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                        }
                                        mProgress.dismiss();
                                    }
                                });


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();

                mProgress.setMessage("Uploading");
                mProgress.setCancelable(false);
                mProgress.show();

                final StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());
                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        mProfileImage.setImageURI(mImageUri);
                        final DatabaseReference current_user_db = mDatabaseUsers.child(mAuth.getCurrentUser().getUid());

                        current_user_db.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(dataSnapshot.child("image").getValue().toString());

                                photoRef.delete();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        String downloadUri = taskSnapshot.getDownloadUrl().toString();

                        current_user_db.child("image").setValue(downloadUri);

                        mProgress.dismiss();

                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
