package com.example.todo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private EditText registeremail,registerpass;
    private Button registerbutton;
    private  TextView registertext;
    private FirebaseAuth auth;

    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
        registeremail=findViewById(R.id.registeremail);
        registerpass=findViewById(R.id.registerpass);
        registerbutton=findViewById(R.id.registerbutton);
        registertext=findViewById(R.id.registertext);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

        registertext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(ii);
            }
        });

        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registeremail.getText().toString().trim();
                String password = registerpass.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    registeremail.setError("Email is required.");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    registerpass.setError("Password is required.");
                    return;
                }
                else{
                    dialog.setMessage("Registration in Progress.");
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent iii = new Intent(RegisterActivity.this, HomeActivity.class);
                                startActivity(iii);
                                finish();
                                dialog.dismiss();
                            }
                            else {
                                String error = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Registration failed." +error, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }


}