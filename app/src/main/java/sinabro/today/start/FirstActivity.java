package sinabro.today.start;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import sinabro.today.R;
import sinabro.today.information.TimerInfo;
import sinabro.today.login.LoginActivity;
import sinabro.today.popup.SexSelectPopupActivity;
import sinabro.today.setting.PrivacyActivity;
import sinabro.today.setting.PrivacyActivity2;
import sinabro.today.signup.EmailSignupActivity;
import sinabro.today.signup.ProfileSignupActivity;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class FirstActivity extends AppCompatActivity {

    long backKeyPressedTime;
    private Button login;
    private TimerInfo timerInfo;
    private String today = null;
    private Date now_date;
    private Date db_date;
    private String androidId;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private SimpleDateFormat sdformat;
    private Button first_signup_button;
    private VideoView videoView;
    private FrameLayout placeholder;
    private TextView goPrivacy,goPrivacy2;


    @SuppressLint({"HardwareIds", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        androidId = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        first_signup_button = findViewById(R.id.first_signup_button);

        Log.e("androidid",androidId);

        Date date = new Date(); // 현재시간

        // 포맷변경 ( 년월일 시분초)
        sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Java 시간 더하기

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        today = sdformat.format(cal.getTime());

        try {
            now_date = sdformat.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDatabase.child("TimeInfo").child(androidId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // Get user value
                timerInfo = dataSnapshot.getValue(TimerInfo.class);
                try {
                    if(timerInfo!=null) {
                        db_date = sdformat.parse(timerInfo.getNow());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(db_date!=null) {
                    if (now_date.after(db_date)) { // 30일이 다 지남 // db에서 삭제해주기
                        mDatabase.child("TimeInfo").child(androidId).removeValue();

                    } else { // 아직 30일이 안 지났을 때
                        first_signup_button.setEnabled(false);
                        AlertDialog.Builder alert = new AlertDialog.Builder(FirstActivity.this);
                        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();     //닫기
                            }
                        });
                        alert.setMessage("30일 동안 회원가입을 하실수 없습니다.");
                        alert.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });

        SharedPreferences sf = getSharedPreferences("sinabro",MODE_PRIVATE);
        int joinCheck = sf.getInt("join",0);
        final String email = sf.getString("email","");
        final String password = sf.getString("password" , "");
        if(joinCheck == 1)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    FirstActivity.this, R.style.AlertDialogStyle);

            // AlertDialog 셋팅
            alertDialogBuilder
                    .setMessage("기존 "+email+" 로 가입을 진행하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("가입취소",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        final DialogInterface dialog, int id) {

                                    AuthCredential credential = EmailAuthProvider
                                            .getCredential(email,password);

                                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).reauthenticate(credential)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                // shared에서 join값 지워주기 및 이메일 삭제해주기
                                                                SharedPreferences sharedPreferences = getSharedPreferences("sinabro", MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                editor.remove("join");
                                                                editor.apply();
                                                                dialog.dismiss();
                                                            }
                                                        }); // 계정삭제
                                                        finish();
                                                    } else {
                                                        SharedPreferences sharedPreferences = getSharedPreferences("sinabro", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.remove("join");
                                                        editor.apply();
                                                        dialog.dismiss();
                                                    }
                                                }

                                            });
                                }
                            })
                    .setNegativeButton("계속하기",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {
                                    // 다이얼로그를 취소한다
                                    Intent intent = new Intent(FirstActivity.this, ProfileSignupActivity.class);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(R.anim.fromright, R.anim.toleft);
                                    dialog.cancel();
                                }
                            });
            // 다이얼로그 생성
            AlertDialog alertDialog = alertDialogBuilder.create();
            // 다이얼로그 보여주기
            alertDialog.show();
        }

        login = findViewById(R.id.first_login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FirstActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fromright, R.anim.toleft);
            }
        });



        goPrivacy = findViewById(R.id.goPrivacyTxt);
        goPrivacy2 = findViewById(R.id.goPrivacyTxt2);

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
    } // oncreate

    @Override
    protected void onResume() {
        super.onResume();
        placeholder = findViewById(R.id.placeholder);
        placeholder.setVisibility(View.VISIBLE);
        videoView = findViewById(R.id.first_videoView);
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


    //버튼
    public void mOnPopupClick(View v){
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, SexSelectPopupActivity.class);
        intent.putExtra("data", "Test Popup");
        startActivityForResult(intent, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                //데이터 받기
                int result = data.getIntExtra("result",0);
                if(result == 1)
                {
                    SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("sex", "남자");
                    editor.apply();

                    Intent intent = new Intent(this, EmailSignupActivity.class);
                    intent.putExtra("sex", 1);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fromright, R.anim.toleft);
                    finish();


                }else if(result == 2)
                {
                    SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("sex", "여자");
                    editor.apply();

                    Intent intent = new Intent(this, EmailSignupActivity.class);
                    intent.putExtra("sex", 2);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fromright, R.anim.toleft);
                    finish();

                }else{

                }
            }
        }
    }
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
        Toast.makeText(FirstActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


}