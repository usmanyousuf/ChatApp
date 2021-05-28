package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {
    FirebaseAuth auth;
    TextView txt_signIn,signUp_btn;
    CircleImageView profile_image;
    EditText reg_name,reg_email,reg_password,reg_cPassword;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    Uri imageuri;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String imageURI;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait....");
        progressDialog.setCancelable(false);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        setContentView(R.layout.activity_registration);
        txt_signIn = findViewById(R.id.txt_signIn);
        profile_image.findViewById(R.id.profile_image);
        reg_name = findViewById(R.id.reg_name);
        reg_email = findViewById(R.id.reg_email);
        reg_password = findViewById(R.id.reg_password);
        reg_cPassword = findViewById(R.id.reg_cPassword);
        signUp_btn = findViewById(R.id.signUp_btn);
        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String name = reg_name.getText().toString();
                String email = reg_email.getText().toString();
                String password = reg_password.getText().toString();
                String cPassword = reg_cPassword.getText().toString();
                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(cPassword)){
                    progressDialog.dismiss();
                    Toast.makeText(RegistrationActivity.this,"Please Enter Valid Data",Toast.LENGTH_LONG).show();
                }else if(email.matches(emailPattern)){
                    progressDialog.dismiss();
                    reg_email.setError("Please Enter Valid Email");
                    Toast.makeText(RegistrationActivity.this,"Please Enter Valid Email",Toast.LENGTH_LONG).show();
                }else if(!password.equals(cPassword)){
                    progressDialog.dismiss();
                    Toast.makeText(RegistrationActivity.this,"Password Does Not Matches",Toast.LENGTH_LONG).show();
                }else if(password.length()<6){
                    progressDialog.dismiss();
                    Toast.makeText(RegistrationActivity.this,"Password should be of min 6 char",Toast.LENGTH_LONG).show();

                }else {
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {progressDialog.dismiss();
                                DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
                                StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());
                                if(imageuri!= null){
                                    storageReference.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imageURI = uri.toString();
                                                        Users users = new Users(auth.getUid(),name,email,imageURI);
                                                        reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    startActivity(new Intent(RegistrationActivity.this,HomeActivity.class));

                                                                }else {
                                                                    Toast.makeText(RegistrationActivity.this,"Error in creating a new user",Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }else {
                                    imageURI ="https://firebasestorage.googleapis.com/v0/b/chat-app-7be8c.appspot.com/o/profile_image.png?alt=media&token=f56150da-ff36-47fb-9069-c39e13e9e653";
                                    Users users = new Users(auth.getUid(),name,email,imageURI);
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegistrationActivity.this,HomeActivity.class));

                                            }else {
                                                Toast.makeText(RegistrationActivity.this,"Error in creating a new user",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }

                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(RegistrationActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }

            }
        });
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);

            }
        });

        txt_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this,LoginActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10){
            if(data!= null){
                imageuri = data.getData();
                profile_image.setImageURI(imageuri);
            }
        }
    }
}