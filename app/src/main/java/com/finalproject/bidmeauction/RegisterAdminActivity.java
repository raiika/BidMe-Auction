package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Date;

public class RegisterAdminActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mRegisterBtn;
    private Uri mImageUri = null;
    private ImageButton mRegisterImageBtn;
    private FirebaseAuth mAuth1;
    private FirebaseAuth mAuth2;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;
    private StorageReference mStorageImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_admin);

        mAuth1 = FirebaseAuth.getInstance();

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://bidme-auction.firebaseio.com/")
                .setApiKey("AIzaSyD39FZh33JXtN1obiVcS8T1OT7P76CHOC8")
                .setApplicationId("bidme-auction").build();

        FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(),firebaseOptions,
                String.valueOf(new Date()));

        mAuth2 = FirebaseAuth.getInstance(myApp);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");

        mProgress = new ProgressDialog(this);


        mNameField = (EditText) findViewById(R.id.nameField);
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);

        mRegisterImageBtn = (ImageButton) findViewById(R.id.registerImagebtn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startRegisterAdmin();

            }
        });

        mRegisterImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void startRegisterAdmin() {

        final String name = mNameField.getText().toString().trim();
        final String email = mEmailField.getText().toString().trim();
        final String password = mPasswordField.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && mImageUri != null) {

            mProgress.setMessage("Signing up...");
            mProgress.setCancelable(false);
            mProgress.show();

            final StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());

            mAuth2.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                String user_id = mAuth2.getCurrentUser().getUid();

                                final DatabaseReference current_user_db = mDatabase.child(user_id);

                                String downloadUri = taskSnapshot.getDownloadUrl().toString();

                                current_user_db.child("image").setValue(downloadUri);
                                current_user_db.child("name").setValue(name);
                                current_user_db.child("type").setValue("admin").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mNameField.setText("");
                                        mEmailField.setText("");
                                        mPasswordField.setText("");
                                        mImageUri = null;
                                        mRegisterImageBtn.setImageResource(android.R.color.transparent);

                                        mProgress.dismiss();

                                        Toast.makeText(RegisterAdminActivity.this, "Success creating new admin named"+name, Toast.LENGTH_SHORT).show();

                                        mAuth2.signOut();
                                    }
                                });


                            }
                        });

                    }else{


                        mProgress.dismiss();

                        Toast.makeText(RegisterAdminActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                    }


                }
            });



        }
        else{
            if (!TextUtils.isEmpty(name)){
                Toast.makeText(RegisterAdminActivity.this,"Fill your name", Toast.LENGTH_SHORT).show();
            }
            else if (!TextUtils.isEmpty(email)){
                Toast.makeText(RegisterAdminActivity.this,"Fill your e-mail", Toast.LENGTH_SHORT).show();
            }
            else if(!TextUtils.isEmpty(password)){
                Toast.makeText(RegisterAdminActivity.this,"Fill your password", Toast.LENGTH_SHORT).show();
            }
            else if(mImageUri == null){
                Toast.makeText(RegisterAdminActivity.this,"Choose your profile picture", Toast.LENGTH_SHORT).show();
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

                mRegisterImageBtn.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
