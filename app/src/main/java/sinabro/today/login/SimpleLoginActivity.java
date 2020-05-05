package sinabro.today.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import sinabro.today.R;
import sinabro.today.information.DeviceInfo;
import sinabro.today.main.MainActivity;
import sinabro.today.setting.PrivacyActivity;
import sinabro.today.setting.PrivacyActivity2;

public class SimpleLoginActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private String androidId;
    private String email;
    private String password;
    private Button goLogin;
    private DatabaseReference mDatabase;
    private VideoView videoView;
    private FrameLayout placeholder;
    private TextView goPrivacy,goPrivacy2;
    long backKeyPressedTime;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_login);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        goLogin = findViewById(R.id.simplelogin_button);
        androidId = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);

        mDatabase.child("deviceinfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final DeviceInfo deviceInfo = snapshot.getValue(DeviceInfo.class);
                    if (Objects.requireNonNull(deviceInfo).getDeviceID().equals(androidId)) {
                        email = deviceInfo.getEmail();
                        password = deviceInfo.getPassword();
                        goLogin.setText(email + " 님 로그인");
                        goLogin.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });


        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        goPrivacy = findViewById(R.id.goPrivacyTxt3);
        goPrivacy2 = findViewById(R.id.goPrivacyTxt4);

        goPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(PrivacyActivity2.class);
            }
        });

        goPrivacy2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(PrivacyActivity.class);
            }
        });


    } // oncreate()

    @Override
    protected void onResume() {
        super.onResume();
        placeholder = findViewById(R.id.placeholder2);
        placeholder.setVisibility(View.VISIBLE);
        videoView = findViewById(R.id.simplelogin_videoView);
        String uriPath = "android.resource://" + getPackageName() + "/" + R.raw.mainvideo;
        Uri uri = Uri.parse(uriPath);
        videoView.setVideoURI(uri);
        videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            placeholder.setVisibility(View.GONE);
                            mp.setLooping(true);
                        }
                        return false;
                    }
                });
            }
        });
    }

    public void login(){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            user = mAuth.getCurrentUser();
                            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("platform").setValue("android");

                            passPushTokenToServer();
                            SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.remove("logout");
                            editor.apply(); // 뱃지 쉐어드 생성
                            myStartActivity(MainActivity.class);
                            finish();
                        } else {
                            // 비밀번호가 맞지 않을 경우

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    SimpleLoginActivity.this);
                            LayoutInflater inflater = getLayoutInflater();
                            View alertView = inflater.inflate(R.layout.dialog_simple_error, null);
                            alertDialogBuilder.setView(alertView);
                            // 다이얼로그 생성
                            final TextView email_simple_error = alertView.findViewById(R.id.email_simple_error);
                            final TextView password_simple_error = alertView.findViewById(R.id.password_simple_error);
                            Button settingYes_simple = alertView.findViewById(R.id.settingYes_simple); // 로그인
                            Button settingNo_simple = alertView.findViewById(R.id.settingNo_simple); // 비밀번호찾기
                            email_simple_error.setText(email);

                            final AlertDialog alertDialog = alertDialogBuilder.create();
                            settingYes_simple.setOnClickListener(new View.OnClickListener() { // 로그인
                                @Override
                                public void onClick(View v) {

                                    mAuth.signInWithEmailAndPassword(email_simple_error.getText().toString(), password_simple_error.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if(task.isSuccessful()){

                                                        user = mAuth.getCurrentUser();
                                                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("platform").setValue("android");

                                                        Query databaseReference = FirebaseDatabase.getInstance().getReference().child("deviceinfo").orderByChild("email").equalTo(email);
                                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                    mDatabase.child("deviceinfo").child(Objects.requireNonNull(snapshot.getKey())).child("password").setValue(password_simple_error.getText().toString());
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                                        passPushTokenToServer();
                                                        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
                                                        SharedPreferences.Editor editor = settings.edit();
                                                        editor.putInt("logout", 0);
                                                        editor.apply(); // 뱃지 쉐어드 생성
                                                        myStartActivity(MainActivity.class);
                                                        finish();
                                                    } else{
                                                        startToast("비밀번호가 틀렸습니다.");
                                                    }

                                                } // onComplete()
                                            });

                                }
                            });

                            settingNo_simple.setOnClickListener(new View.OnClickListener() { // 비번찾기
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                            SimpleLoginActivity.this);
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
                                            String emailAddress = email_etx.getText().toString();

                                            auth.sendPasswordResetEmail(emailAddress)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d("email", "Email sent.");
                                                                startToast("이메일을 확인해 주세요.");
                                                                alertDialog.cancel();
                                                            }
                                                        }
                                                    });
                                        }
                                    });

                                    // 다이얼로그 보여주기
                                    alertDialog.show();
                                }
                            });

                            // 다이얼로그 보여주기
                            alertDialog.show();



                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            String emailAddress = email;

                            auth.sendPasswordResetEmail(emailAddress)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("email", "Email sent.");

                                            }
                                        }
                                    });

                        }
                    }
                });

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

    public void onBackPressed() {
        //1번째 백버튼 클릭
        if(System.currentTimeMillis()>backKeyPressedTime+2000){
            backKeyPressedTime = System.currentTimeMillis();
            startToast("나가려면 BACK 버튼을 누르세요.");
        }
        //2번째 백버튼 클릭 (종료)
        else{
            AppFinish();
        }
    }

    //앱종료
    public void AppFinish(){
        finish();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void startToast(String msg) {
        Toast.makeText(SimpleLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}