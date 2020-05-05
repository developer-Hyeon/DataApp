package sinabro.today.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import sinabro.today.setting.FaqActivity;
import sinabro.today.setting.MapSettingActivity;
import sinabro.today.setting.NoticeActivity;
import sinabro.today.R;
import sinabro.today.administer.AdministerActivity;
import sinabro.today.block.BlockActivity;
import sinabro.today.custom.CustomProgressDialog;
import sinabro.today.fragment.ChatFragment;
import sinabro.today.fragment.LoveFragment;
import sinabro.today.fragment.MapingFragment;
import sinabro.today.information.TimerInfo;
import sinabro.today.login.SimpleLoginActivity;
import sinabro.today.model.AdministerModel;
import sinabro.today.setting.PrivacyActivity;
import sinabro.today.setting.PrivacyActivity2;
import sinabro.today.profile.ProfileActivity;
import sinabro.today.start.FirstActivity;
import sinabro.today.store.StoreActivity;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity extends AppCompatActivity {
    private CustomProgressDialog customProgressDialog;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private PagerAdapter pagerAdapter;
    private AdministerModel administerModel;
    private int     requestCount = 100;
    private ValueEventListener valueEventListener;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private Query databaseReference;
    private List<String> tmp = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private View drawerView;
    private Button etc1,etc2,etc3,etc4,etc5,etc6,etc7, etc8, etc9;
    private String androidId;
    private String uid;
    private List<String> photourl = new ArrayList<>();
    private String my_heart;
    private String my_coupon;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    long backKeyPressedTime;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.parseColor("#865FF8"));

        customProgressDialog = new CustomProgressDialog(MainActivity.this);
        Objects.requireNonNull(customProgressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.drawer);


        drawerLayout.addDrawerListener(listener);
        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        androidId = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);


        uid = user.getUid();
        Log.e("my_uid", uid);

        valueEventListener = mDatabase.child("users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.exists())){
                    mAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, FirstActivity.class);
                    MainActivity.this.finish();
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("freeDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    long nowTime = System.currentTimeMillis();
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                    Date nowDate = new Date(nowTime);
                    String getTime = simpleDateFormat.format(nowDate);

                    String nowYear = getTime.substring(0, 4);
                    String nowMonth = getTime.substring(5, 7);
                    String nowDay = getTime.substring(8, 10);

                    String freetime = dataSnapshot.getValue(String.class);

                    String freeYear = freetime.substring(0, 4);
                    String freeMonth = freetime.substring(5, 7);
                    String freeDay  = freetime.substring(8, 10);

                    if(!freeDay.equals(nowDay)){
                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("freeCoupon").setValue("1");
                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("freeDate").setValue(getTime);
                        my_coupon ="1";
                    } // 날짜가 달라서 그냥 지급
                    else{
                        if(freeYear.equals(nowYear) && freeMonth.equals(nowMonth)){
                            // 미지급
                        }else{
                            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("freeCoupon").setValue("1");
                            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("freeDate").setValue(getTime);
                            my_coupon ="1";
                        } // 지급
                    }// 날짜가 같을 때 연도 및 월 비교

                } // 기존유저
                else{ // 신규유저
                    long nowTime = System.currentTimeMillis();
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                    Date nowDate = new Date(nowTime);
                    String getTime = simpleDateFormat.format(nowDate);
                    FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("freeCoupon").setValue("1");
                    FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("freeDate").setValue(getTime);
                    my_coupon ="1";
                } // 지급
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        uid = FirebaseAuth.getInstance().getUid();
        administerModel = new AdministerModel();
        FirebaseDatabase.getInstance().getReference().child("administer").child("sinabro").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    administerModel = dataSnapshot.getValue(AdministerModel.class);
                    if(requestCount > 0) {
                        assert administerModel != null;
                        assert uid != null;
                        if(uid.equals(administerModel.sung) || uid.equals(administerModel.hyeon) || uid.equals(administerModel.yoon)) {

                            FirebaseDatabase.getInstance().getReference().child("administer").child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                MainActivity.this);
                                        LayoutInflater inflater = getLayoutInflater();
                                        View alertView = inflater.inflate(R.layout.administer_dialog, null);
                                        alertDialogBuilder.setView(alertView);
                                        // 다이얼로그 생성
                                        final AlertDialog alertDialog = alertDialogBuilder.create();
                                        final TextView textView_gogo = alertView.findViewById(R.id.gogo);
                                        textView_gogo.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(MainActivity.this, AdministerActivity.class);
                                                startActivity(intent);
                                                alertDialog.dismiss();
                                            }
                                        });
                                        final TextView textView_nono = alertView.findViewById(R.id.Notgogo);
                                        textView_nono.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                alertDialog.cancel();
                                            }
                                        });
                                        // 다이얼로그 보여주기
                                        alertDialog.show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        initViewPager();

        Button store_Btn = findViewById(R.id.main_store);
        store_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
                intent.putExtra("heart",my_heart);
                startActivity(intent);
                overridePendingTransition(R.anim.fromright, R.anim.toleft);
            }
        });

        Button btn_open = (Button)findViewById(R.id.main_profile_button);
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        Button btn_close = findViewById(R.id.main_profile_button_cancel);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });


        etc1 = findViewById(R.id.etc1);
        etc2 = findViewById(R.id.etc2);
        etc3 = findViewById(R.id.etc3);
        etc4 = findViewById(R.id.etc4);
        etc5 = findViewById(R.id.etc5);
        etc6 = findViewById(R.id.etc6);
        etc7 = findViewById(R.id.etc7);
        etc8 = findViewById(R.id.etc8);
        etc9 = findViewById(R.id.etc9);


        etc1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
            }
        }); // 프로필 설정

        etc2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(BlockActivity.class);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
            }
        }); // 차단목록

        etc3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PrivacyActivity2.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
            }
        }); // 약관

        etc4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PrivacyActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
            }
        }); // 개인정보 취급방침

        etc5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoticeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
            }
        });


        etc6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(
                        MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View alertView = inflater.inflate(R.layout.deleteid_dialog, null);
                alertDialogBuilder.setView(alertView);
                // 다이얼로그 생성
                final androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
                final TextView textView_no = alertView.findViewById(R.id.settingNo3);
                textView_no.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();

                    }
                });
                final TextView textView_yes = alertView.findViewById(R.id.settingYes3);
                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();
                        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(
                                MainActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View alertView = inflater.inflate(R.layout.deleteid_dialog2, null);
                        alertDialogBuilder.setView(alertView);
                        // 다이얼로그 생성
                        final androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
                        final TextView textView_no = alertView.findViewById(R.id.settingNo10);
                        final EditText password_my = alertView.findViewById(R.id.password_my);
                        user = mAuth.getCurrentUser();
                        textView_no.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.cancel();

                            }
                        });
                        final TextView textView_yes = alertView.findViewById(R.id.settingYes10);
                        textView_yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(Objects.requireNonNull(user.getEmail()), password_my.getText().toString());

                                user.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    customProgressDialog.show();
                                                    delete();
                                                    myStartActivity(FirstActivity.class);
                                                    customProgressDialog.dismiss();
                                                    finish();
                                                } else {
                                                    startToast("비밀번호가 틀렸습니다. 다시 입력해 주세요.");
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

            }
        }); // 계정탈퇴

        etc7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(
                        MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View alertView = inflater.inflate(R.layout.logout_dialog, null);
                alertDialogBuilder.setView(alertView);
                // 다이얼로그 생성
                final androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
                final TextView textView_no = alertView.findViewById(R.id.settingNo2);
                textView_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();
                    }
                });
                final TextView textView_yes = alertView.findViewById(R.id.settingYes2);
                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("logout", 1);
                        editor.apply(); // 뱃지 쉐어드 생성
                        myStartActivity(SimpleLoginActivity.class);
                        finish();
                        overridePendingTransition(R.anim.fromright, R.anim.toleft);
                        alertDialog.dismiss();
                        mDatabase.child("man_location").child(user.getUid()).removeValue();
                        mDatabase.child("woman_location").child(user.getUid()).removeValue();
                    }
                });

                // 다이얼로그 보여주기
                alertDialog.show();
            }

        }); // 로그아웃

        etc8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(MapSettingActivity.class);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
            }
        }); // 지도설정

        etc9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(FaqActivity.class);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
            }
        }); // 공지사항, FAQ


    } // oncreate


    public void delete() {

        FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(uid).removeValue(); // 관리자 계정 사진 요청 삭제
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" +uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    if(item.child("users").child(uid).exists()){
                        Log.d("namgung","방 키값 "+item.getKey());
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(Objects.requireNonNull(item.getKey())).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); // 채팅방이 존재하면 삭제해준다

        FirebaseDatabase.getInstance().getReference().child("Buys").child(uid).removeValue();// 구매목록 삭제
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();

        mDatabase.child("users").child(user.getUid()).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("imageHashCode").exists()) {
                        photourl.add(snapshot.child("imageHashCode").getValue(String.class));
                    } else if(snapshot.child("tempHashCode").exists()) {
                        photourl.add(snapshot.child("tempHashCode").getValue(String.class));
                    }
                }

                databaseReference = FirebaseDatabase.getInstance().getReference().child("deviceinfo").orderByChild("email").equalTo(user.getEmail());
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            mDatabase.child("deviceinfo").child(Objects.requireNonNull(snapshot.getKey())).removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                    }
                });

                mDatabase.child("users").child(user.getUid()).removeValue();
                mDatabase.child("man_location").child(user.getUid()).removeValue();
                mDatabase.child("woman_location").child(user.getUid()).removeValue();
                mDatabase.child("Buys").child(user.getUid()).removeValue();

                for(int i =0; i< photourl.size(); i++) {
                    StorageReference deleteRef = storageRef.child("userImages/" + uid + "/" + photourl.get(i));
                    deleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            Log.e("delete", " 스토리지 사진 삭제");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                        }
                    });
                }


                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User account deleted.");
                                }
                            }
                        });

            } // onDataChange()

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        String today = null;
        Date date = new Date(); // 현재시간

        // 포맷변경 ( 년월일 시분초)
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Java 시간 더하기

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(Calendar.DATE, 30); //30 일후
        today = sdformat.format(cal.getTime());

        TimerInfo timerInfo = new TimerInfo(today, androidId); //
        mDatabase.child("TimeInfo").child(androidId).setValue(timerInfo);

        SharedPreferences pref = getSharedPreferences("sinabro", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();


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
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
    private  void initViewPager() {
        viewPager = findViewById(R.id.main_viewpager);
        //프레그먼트 리스트에 추가
        List<Fragment> listFragments = new ArrayList<>();

        listFragments.add(new MapingFragment());
        listFragments.add(new ChatFragment());
        listFragments.add(new LoveFragment());

        tabLayout = findViewById(R.id.main_tapbar);
        //탭 어댑터에 리스트 넘겨준 후 뷰페이저 연결
        pagerAdapter = new sinabro.today.adapter.PagerAdapter(getSupportFragmentManager(),listFragments);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(pagerAdapter);


        Intent intent = getIntent();
        Log.d("namgungfragment","넘어온 인텐트 값  "+intent.getIntExtra("move",0));
        viewPager.setCurrentItem(intent.getIntExtra("move",0));
        Objects.requireNonNull(tabLayout.getTabAt(intent.getIntExtra("move", 0))).select(); // 프레그먼트 전부다 호출하는 구조, 덮어써서 문제된다.
        // 뷰페이저 이동했을때 & 탭 눌렀을때 해당 위치로 이동
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

        });

    }

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    @Override
    public void onResume()
    {
        super.onResume();

        SharedPreferences sf = getSharedPreferences("sinabro",MODE_PRIVATE);
        int profile_what = sf.getInt("profile",0);

        if(profile_what == 1){
            startToast("차단한 사람은 볼 수 없습니다.");
        } else if(profile_what == 2){
            startToast("상대방이 나를 차단하였습니다.");
        } else if(profile_what == 3){
            startToast("상대방이 회원탈퇴를 하였습니다.");
        } else if(profile_what == 4){
            startToast("상대방이 위치설정을 껐습니다."); // 로그아웃 or 위치설정끔
        }

        SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("profile", 0); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐)
        editor.apply();

        mDatabase.child("users").child(user.getUid()).child("heart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                my_heart = dataSnapshot.getValue(String.class);
                final TextView heart_txt = findViewById(R.id.heart_text);
                heart_txt.setText(my_heart);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabase.child("users").child(user.getUid()).child("freeCoupon").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                my_coupon = dataSnapshot.getValue(String.class);
                final TextView coupon_txt = findViewById(R.id.coupon_text);
                coupon_txt.setText(my_coupon);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Intent intent = getIntent();
        if(intent.getIntExtra("move",0) != 0){
            viewPager.setCurrentItem(intent.getIntExtra("move",0));
            tabLayout.getTabAt(intent.getIntExtra("move",0)).select();
        }

    }

}