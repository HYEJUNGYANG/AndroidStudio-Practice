package com.jung.practice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private EditText et_email;
    private EditText et_pwd;
    private EditText et_pwd2;
    private EditText et_name;
    private TextView tv_pwd;

    private int focusCount = 0;
    private boolean isSame = false;
    int keyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // 뒤로가기 버튼 활성화 (Manifest.xml에서 부모 액티비티 지정해줘야함)
//        getSupportActionBar().setDisplayShowTitleEnabled(false);  // toolbar title 제거

        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("user");

        tv_pwd = findViewById(R.id.tv_pwd);
        et_email = findViewById(R.id.email);
        et_pwd = findViewById(R.id.pwd);
        et_pwd2 = findViewById(R.id.et_pwd2);
//        et_pwd2.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                // 입력 전
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                // 입력란에 변화가 있을 시
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                // 입력이 끝난 후
//            }
//        });



        et_pwd2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (focusCount == 0) focusCount += 1;
                    else if (focusCount > 0) {
                        if (et_pwd.getText().toString().equals(et_pwd2.getText().toString())) {
                            isSame = true;
                            tv_pwd.setText("");
                            et_pwd2.setBackgroundColor(0);
                        } else {
                            isSame = false;
                            tv_pwd.setText("비밀번호가 다릅니다.");
                            et_pwd2.setBackgroundResource(R.drawable.red_edittext);
                        }
                    }
                }
                else if (focusCount > 0 && b == false) {
                    if (et_pwd.getText().toString().equals(et_pwd2.getText().toString())) {
                        isSame = true;
                        tv_pwd.setText("");
                        et_pwd2.setBackgroundResource(0);
                    } else {
                        isSame = false;
                        tv_pwd.setText("비밀번호가 다릅니다.");
                        et_pwd2.setBackgroundResource(R.drawable.red_edittext);
                    }
                }
            }
        });


        et_name = findViewById(R.id.name);

        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_enter, R.anim.none);
                finish();
            }
        });

        Button btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_pwd.getText().toString().equals(et_pwd2.getText().toString())) {
                    isSame = true;
                    tv_pwd.setText("");
                }
                else {
                    isSame = false;
                }
                String str_email = et_email.getText().toString();
                String str_pwd = et_pwd.getText().toString();
                String str_name = et_name.getText().toString();

                if (str_email != "" && str_pwd != "" && str_name.length() > 2 && isSame) {
                    mAuth.createUserWithEmailAndPassword(str_email, str_pwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {  // 가입 성공시
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                UserRegister userRegister = new UserRegister();
                                userRegister.setEmail(firebaseUser.getEmail());
                                userRegister.setPassword(str_pwd);
                                userRegister.setName(str_name);
                                myRef.child(firebaseUser.getUid()).setValue(userRegister);

                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

                                builder.setTitle("회원가입 완료");
                                builder.setMessage("바로 로그인 하시겠습니까?");
                                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   // 다시 확인해보기
                                        startActivity(intent);
                                        Toast.makeText(RegisterActivity.this, str_name + "님 안녕하세요 🙂", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });

                                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        Toast.makeText(RegisterActivity.this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });

                                builder.show();

                            } else {
                                Toast.makeText(RegisterActivity.this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else if (isSame == false) {
                    tv_pwd.setText("비밀번호가 다릅니다.");
                    et_pwd2.setBackgroundResource(R.drawable.red_edittext);
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다 😅", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(RegisterActivity.this, "값을 전부 입력해주세요 🙏🏻", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.none, R.anim.slide_right_exit);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{   // 툴바에 있는 뒤로가기 버튼 눌렀을 때
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}