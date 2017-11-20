package com.finalproject.bidmeauction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class RegisterActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mRepeatPasswordField;
    private EditText mAddressField;
    private EditText mPhoneField;
    private Button mRegisterBtn;
    private Uri mImageUri = null;
    private ImageButton mRegisterImageBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;
    private StorageReference mStorageImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Register User");
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");

        mProgress = new ProgressDialog(this);


        mNameField = (EditText) findViewById(R.id.nameField);
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mRepeatPasswordField = (EditText) findViewById(R.id.repeatPasswordField);
        mPhoneField = (EditText) findViewById(R.id.phoneField);
        mAddressField = (EditText) findViewById(R.id.addressField);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);

        mRegisterImageBtn = (ImageButton) findViewById(R.id.registerImagebtn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startRegister();

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

    public void goToLogin(View v) {
        onBackPressed();
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void startRegister() {

        final String name = mNameField.getText().toString().trim();
        final String email = mEmailField.getText().toString().trim();
        final String password = mPasswordField.getText().toString().trim();
        final String repeatPassword = mRepeatPasswordField.getText().toString().trim();
        final String phone = mPhoneField.getText().toString().trim();
        final String address = mAddressField.getText().toString().trim();

        if ((repeatPassword.equals(password)) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && mImageUri != null && !TextUtils.isEmpty(repeatPassword) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(address)) {

            mProgress.setMessage("Signing up...");
            mProgress.setCancelable(false);
            mProgress.show();

            final StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                String user_id = mAuth.getCurrentUser().getUid();

                                final DatabaseReference current_user_db = mDatabase.child(user_id);

                                String downloadUri = taskSnapshot.getDownloadUrl().toString();

                                current_user_db.child("image").setValue(downloadUri);
                                current_user_db.child("name").setValue(name);
                                current_user_db.child("phone").setValue(phone);
                                current_user_db.child("address").setValue(address);
                                current_user_db.child("balance").setValue(0);
                                current_user_db.child("type").setValue("user").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mProgress.dismiss();

                                        finish();

                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(mainIntent);

                                    }
                                });


                            }
                        });

                    }else{

                        mProgress.dismiss();

                        Toast.makeText(RegisterActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();

                    }


                }
            });



        }
        else{
            if (!TextUtils.isEmpty(name)){
                Toast.makeText(RegisterActivity.this,"Fill your name", Toast.LENGTH_SHORT).show();
            } else if (mImageUri == null) {
                Toast.makeText(RegisterActivity.this, "Choose your profile picture", Toast.LENGTH_SHORT).show();
            }
            else if (!TextUtils.isEmpty(email)){
                Toast.makeText(RegisterActivity.this,"Fill your e-mail", Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(phone)) {
                Toast.makeText(RegisterActivity.this, "Fill your phone", Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(address)) {
                Toast.makeText(RegisterActivity.this, "Fill your address", Toast.LENGTH_SHORT).show();
            }
            else if(!TextUtils.isEmpty(password)){
                Toast.makeText(RegisterActivity.this,"Fill your password", Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(repeatPassword)) {
                Toast.makeText(RegisterActivity.this, "Fill your repeat password", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(repeatPassword)) {
                Toast.makeText(RegisterActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
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

                Picasso.with(getApplicationContext()).load(mImageUri).transform(new additionalMethod.CircleTransform()).into(mRegisterImageBtn);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
