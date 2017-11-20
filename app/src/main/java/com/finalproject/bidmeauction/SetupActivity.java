package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private ImageButton mSetupImageBtn;
    private EditText mSetupNameField;
    private Button mSetupSubmitBtn;
    private EditText mAddressField;
    private EditText mPhoneField;
    private Uri mImageUri = null;
    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseUsers;

    private StorageReference mStorageImage;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();

        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProgress = new ProgressDialog(this);

        mSetupImageBtn = (ImageButton) findViewById(R.id.setupImageBtn);
        mSetupNameField = (EditText) findViewById(R.id.setupNameField);
        mPhoneField = (EditText) findViewById(R.id.setupPhoneField);
        mAddressField = (EditText) findViewById(R.id.setupAddressField);
        mSetupSubmitBtn = (Button) findViewById(R.id.setupSubmitBtn);

        mSetupSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startSetupAccount();

            }
        });

        mSetupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

    }

    public void goToLogin(View v) {
        mAuth.signOut();

        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);

        finish();
    }

    private void startSetupAccount() {

        final String name = mSetupNameField.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();
        final String phone = mPhoneField.getText().toString().trim();
        final String address = mAddressField.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && mImageUri != null && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(address)) {

            mProgress.setMessage("Finishing Setup");
            mProgress.show();

            StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUri = taskSnapshot.getDownloadUrl().toString();

                    mDatabaseUsers.child(user_id).child("name").setValue(name);
                    mDatabaseUsers.child(user_id).child("image").setValue(downloadUri);
                    mDatabaseUsers.child(user_id).child("address").setValue(address);
                    mDatabaseUsers.child(user_id).child("phone").setValue(phone);
                    mDatabaseUsers.child(user_id).child("balance").setValue(0);
                    mDatabaseUsers.child(user_id).child("type").setValue("user").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mProgress.dismiss();

                            Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);

                            finish();

                        }
                    });


                }
            });


        } else {
            if (!TextUtils.isEmpty(name)) {
                Toast.makeText(SetupActivity.this, "Fill your name", Toast.LENGTH_SHORT).show();
            } else if (mImageUri == null) {
                Toast.makeText(SetupActivity.this, "Choose your profile picture", Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(phone)) {
                Toast.makeText(SetupActivity.this, "Fill your phone", Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(address)) {
                Toast.makeText(SetupActivity.this, "Fill your address", Toast.LENGTH_SHORT).show();
            }
        }

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

                Picasso.with(getApplicationContext()).load(mImageUri).transform(new additionalMethod.CircleTransform()).into(mSetupImageBtn);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
