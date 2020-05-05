package sinabro.today.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import sinabro.today.R;
import sinabro.today.custom.CustomProgressDialog;
import sinabro.today.information.DeviceInfo;
import sinabro.today.main.MainActivity;
import sinabro.today.signup.EmailSignupActivity;
import sinabro.today.start.FirstActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText id;
    private EditText password;
    private Button login;
    private Button passwordFind;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Toolbar toolbar;
    private CustomProgressDialog customProgressDialog;
    private String androidId;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private List<String> deviceinfoKey = new ArrayList<>();
    private List<String> tmp = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        androidId = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        customProgressDialog = new CustomProgressDialog(LoginActivity.this);
        customProgressDialog .getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        id = findViewById(R.id.login_edittext_email);
        password =  findViewById(R.id.login_edittext_password);

        login = (Button) findViewById(R.id.login_activity_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(id.getText().toString().length() ==0)
                    startToast("이메일을 입력해주세요.");
                else if(password.getText().toString().length() < 6)
                    startToast("6자리 이상 비밀번호를 입력해주세요.");
                else {
                    customProgressDialog.show();
                    loginEvent();
                }
            }
        });

        //로그인 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //로그인
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //로그아웃
                }
            }
        };


        passwordFind =  findViewById(R.id.login_password_button);
        passwordFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        LoginActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View alertView = inflater.inflate(R.layout.email_dialog, null);
                alertDialogBuilder.setView(alertView);
                // 다이얼로그 생성
                final AlertDialog alertDialog = alertDialogBuilder.create();
                final TextView textView_no = alertView.findViewById(R.id.settingNo6);
                final EditText email_etx =  alertView.findViewById(R.id.email_send_etx);
                textView_no.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();

                    }
                });
                final TextView textView_yes = alertView.findViewById(R.id.settingYes6);
                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth auth = FirebaseAuth.getInstance();

                        if(!(email_etx.getText().toString().isEmpty())){
                            String emailAddress = email_etx.getText().toString();
                            auth.sendPasswordResetEmail(emailAddress)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("email", "Email sent.");
                                                startToast("이메일을 확인해 주세요.");
                                                alertDialog.cancel();
                                            } else{
                                                startToast("이메일이 잘못되었습니다.");
                                            }
                                        }
                                    });
                        } else{
                            startToast("이메일을 입력해 주세요.");
                        }
                    }
                });

                // 다이얼로그 보여주기
                alertDialog.show();
                // 비밀번호 찾기 버튼 구현 필요
            }
        });
    }



    void loginEvent() {
        login.setEnabled(false);
        final String password_error = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password.";
        final String email_error = "com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted.";

        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            //로그인 실패한부분
                            // Log.w("namgung", "signInWithEmail:failure", task.getException());
                            Log.d("namgung", task.getException().toString());
                            if(task.getException().toString().equals(password_error)) {
                                Toast.makeText(LoginActivity.this, "비밀번호가 틀렸습니다.",
                                        Toast.LENGTH_SHORT).show();
                                customProgressDialog.dismiss();
                            }
                            else if(task.getException().toString().equals(email_error)) {
                                Toast.makeText(LoginActivity.this, "등록된 이메일이 아닙니다.",
                                        Toast.LENGTH_SHORT).show();
                                customProgressDialog.dismiss();
                            }
                            else {
                                Toast.makeText(LoginActivity.this, "로그인 실패",
                                        Toast.LENGTH_SHORT).show();
                                customProgressDialog.dismiss();
                            }
                            login.setEnabled(true);
                        } else{
                            DeviceInfo deviceInfo = new DeviceInfo(androidId, id.getText().toString(), password.getText().toString());
                            FirebaseDatabase.getInstance().getReference().child("deviceinfo").push().setValue(deviceInfo);
                            user = mAuth.getCurrentUser();
                            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("platform").setValue("android");
                            Query databaseReference = FirebaseDatabase.getInstance().getReference().child("deviceinfo").orderByChild("email").equalTo(user.getEmail());
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        mDatabase.child("deviceinfo").child(Objects.requireNonNull(snapshot.getKey())).child("password").setValue(password.getText().toString());
                                        passPushTokenToServer();
                                        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putInt("logout", 0);
                                        editor.apply(); // 뱃지 쉐어드 생성
                                        login.setEnabled(true);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                            customProgressDialog.dismiss();
                        }
                    } // onComplete()
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(LoginActivity.this, FirstActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    void passPushTokenToServer(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("aaaaa", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        Map<String,Object> map = new HashMap<>();
                        map.put("pushToken",token);
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
                    }
                });

    } //assPushTokenToServer 현재 나의 uid 받아와 나의 토큰을 저장한다.


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LoginActivity.this, FirstActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }




}