package com.example.cockmate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText user_email;
    private EditText user_password;
    private EditText user_password_check;
    private EditText user_name;
    private Button buttonSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        user_email = (EditText) findViewById(R.id.user_email);
        user_password = (EditText) findViewById(R.id.user_password);
        user_password_check = (EditText) findViewById(R.id.user_password_check);
        user_name = (EditText) findViewById(R.id.user_name);

        buttonSign = (Button) findViewById(R.id.btn_signup);
        buttonSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!user_email.getText().toString().equals("") && !user_password.getText().toString().equals("") && !user_password_check.getText().toString().equals("")) {
                    // 이메일과 비밀번호가 공백이 아닌 경우
                    if(user_password.getText().toString().equals(user_password_check.getText().toString())){
                        createUser(user_email.getText().toString(), user_password.getText().toString(), user_name.getText().toString());
                    }
                    else {
                        // 비밀번호가 안 맞는 경우
                        Toast.makeText(SignupActivity.this, "비밀번호를 맞게 입력해주세요.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // 이메일과 비밀번호가 공백인 경우
                    Toast.makeText(SignupActivity.this, "계정과 비밀번호를 입력하세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createUser(String email, String password, String name) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공시
                            Toast.makeText(SignupActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // 계정이 중복된 경우
                            Toast.makeText(SignupActivity.this, "이미 존재하는 계정입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
