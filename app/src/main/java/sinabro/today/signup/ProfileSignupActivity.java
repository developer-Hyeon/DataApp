package sinabro.today.signup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sinabro.today.BuildConfig;
import sinabro.today.R;
import sinabro.today.custom.CustomImageView;
import sinabro.today.custom.CustomProgressDialog;
import sinabro.today.information.DeviceInfo;
import sinabro.today.main.MainActivity;
import sinabro.today.model.BuyModel;
import sinabro.today.model.NotificationModel;
import sinabro.today.model.UserModel;
import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class ProfileSignupActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 10;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss a");
    private SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
    private String sex;
    private String uid;
    private CustomImageView smallProfile;
    private CustomImageView bigProfile;
    private LinearLayout linearLayout_photo;
    private Uri oneImageUri;
    private Button profile_back_button;
    private ImageView sub_imageView;
    private TextView profile_textview_name , profile_textview_recommend;
    private EditText profile_edittext_name, profile_edittext_recommend;
    private TextView profile_textview_age;
    private TextView profile_textview_personality;
    private TextView profile_textview_tall;
    private TextView profile_textview_body;
    private LinearLayout profile_linearlayout_name;
    private LinearLayout profile_linearlayout_age;
    private LinearLayout profile_linearlayout_personality;
    private LinearLayout profile_linearlayout_tall;
    private LinearLayout profile_linearlayout_body;
    private Button signup;
    private UserModel userModel;
    private UserModel.Photo photo;
    private int albumFlag=0;
    private String age;
    List<Map<String, Object>> dialogItemList;
    private static final String TAG_TEXT = "text";
    private String password;
    private String androidId;
    private String email;
    private String tempHashCode;
    private CustomProgressDialog customProgressDialog;
    private Query databaseReference;
    private String recommend_heart_str;
    private int recommend_heart_int;
    private String profile_edit;
    private String recommend_edit;
    private int flagvalue = 0;

    private String masterPushToken;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_signup);

        customProgressDialog = new CustomProgressDialog(ProfileSignupActivity.this);
        Objects.requireNonNull(customProgressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        androidId = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
        SharedPreferences sf = getSharedPreferences("sinabro",MODE_PRIVATE);
        password = sf.getString("password","");
        email =  sf.getString("email","");
        sex = sf.getString("sex","");
        uid = sf.getString("uid","");

        profile_back_button = findViewById(R.id.profile_back_button);
        profile_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        ProfileSignupActivity.this,R.style.AlertDialogStyle);

                // AlertDialog 셋팅
                alertDialogBuilder
                        .setMessage("회원가입을 취소하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("가입취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {

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
                                                                    ProfileSignupActivity.this.finish();
                                                                    overridePendingTransition(R.anim.fromleft, R.anim.toright);
                                                                }
                                                            }); // 계정삭제
                                                            finish();
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
                                        dialog.cancel();
                                    }
                                });
                // 다이얼로그 생성
                AlertDialog alertDialog = alertDialogBuilder.create();
                // 다이얼로그 보여주기
                alertDialog.show();

            }


        });
        smallProfile = findViewById(R.id.profile_small_imageview);
        bigProfile = findViewById(R.id.profile_big_imageview);
        linearLayout_photo = findViewById(R.id.profile_linearlayout_photo);
        smallProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions();
                if(albumFlag ==1) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent, PICK_FROM_ALBUM);

                }
            }}); // 회원가입 창에 사진을 클릭시 앨범이 열리게 구현됨


        profile_linearlayout_name = findViewById(R.id.profile_linearlayout_name);
        profile_textview_name = findViewById(R.id.profile_textview_name);
        profile_textview_recommend = findViewById(R.id.profile_textview_recommend);
        profile_edittext_name = findViewById(R.id.profile_edittext_name);
        profile_edittext_recommend = findViewById(R.id.profile_edittext_recommend);

        profile_edittext_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_linearlayout_name.setBackgroundResource(R.drawable.border);
            }
        });




        profile_edittext_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { // 입력하기 전에
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { // 입력되는 텍스트에 변화가 있을떄
            }
            @Override
            public void afterTextChanged(final Editable editable) { // 입력이 끝났을때
                profile_edit  = editable.toString();

            } // afterText
        }); // etx

        profile_edittext_recommend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                recommend_edit = editable.toString();
            }
        });


        profile_linearlayout_age = findViewById(R.id.profile_linearlayout_age);
        profile_textview_age = findViewById(R.id.profile_textview_age);
        profile_textview_age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_linearlayout_age.setBackgroundResource(R.drawable.border);
                birthDialog();
            }
        });

        profile_linearlayout_personality = findViewById(R.id.profile_linearlayout_personality);
        profile_textview_personality  = findViewById(R.id.profile_textview_personality);
        profile_textview_personality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_linearlayout_personality.setBackgroundResource(R.drawable.border);
                if(sex.equals("남자"))
                    manPersonDialog();
                else
                    womanPersonDialog();
            }
        }); // 성격 등록

        profile_linearlayout_tall = findViewById(R.id.profile_linearlayout_tall);
        profile_textview_tall  = findViewById(R.id.profile_textview_tall);
        profile_textview_tall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_linearlayout_tall.setBackgroundResource(R.drawable.border);
                heightDialog();
            }
        });

        profile_linearlayout_body = findViewById(R.id.profile_linearlayout_body);
        profile_textview_body  = findViewById(R.id.profile_textview_body);
        profile_textview_body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_linearlayout_body.setBackgroundResource(R.drawable.border);
                if(sex.equals("남자"))
                    manBodyDialog();
                else
                    womanBodyDialog();
            }
        });

        signup = findViewById(R.id.profile_button);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (oneImageUri == null) {
                    startToast("메인 사진을 등록해주세요.");
                } else if (profile_edittext_name.getText().toString().length() == 0) {
                    startToast("닉네임을 입력해주세요.");
                    profile_linearlayout_name.setBackgroundResource(R.drawable.border1);
                } else if (profile_textview_age.getText().toString().length() == 0) {
                    startToast("나이를 입력해주세요.");
                    profile_linearlayout_age.setBackgroundResource(R.drawable.border1);
                }else if(profile_textview_personality.getText().toString().length() == 0)
                {
                    startToast("성격을 등록해주세요.");
                    profile_linearlayout_personality.setBackgroundResource(R.drawable.border1);
                } else if(profile_textview_tall.getText().toString().length() == 0)
                {
                    startToast("키를 등록해주세요.");
                    profile_linearlayout_tall.setBackgroundResource(R.drawable.border1);
                } else if(profile_textview_body.getText().toString().length() == 0)
                {
                    startToast("체형을 등록해주세요.");
                    profile_linearlayout_body.setBackgroundResource(R.drawable.border1);
                }


                else { // 등록은 다했음 -> 닉네임 중복검사 ,  추천인 입력했을때 상황

                    databaseReference = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userName").equalTo(profile_edit);
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                startToast("이미 등록된 닉네임입니다.");
                                profile_linearlayout_name.setBackgroundResource(R.drawable.border1);
                            } else{

                                if(profile_edittext_recommend.getText().toString().length() != 0){ // 추천인 입력했을 때
                                    databaseReference = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userName").equalTo(recommend_edit);
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(snapshot.getKey())).child("heart").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            recommend_heart_str = dataSnapshot.getValue(String.class);
                                                            recommend_heart_int = Integer.parseInt(Objects.requireNonNull(recommend_heart_str));
                                                            recommend_heart_int = recommend_heart_int + 100;
                                                            recommend_heart_str = Integer.toString(recommend_heart_int);
                                                            FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(snapshot.getKey())).child("heart").setValue(recommend_heart_str);


                                                            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
                                                            Date time = new Date();
                                                            String time1 = format.format(time);


                                                            String buys_date = time1;
                                                            String buys_comment = "추천인";
                                                            String buys_change = "+100";
                                                            String current_heart = recommend_heart_str;
                                                            String buys_id = "temp";

                                                            BuyModel buyModel = new BuyModel(buys_date,buys_comment,buys_change,current_heart,buys_id);
                                                            FirebaseDatabase.getInstance().getReference().child("Buys").child(snapshot.getKey()).push().setValue(buyModel);
                                                            if(android.os.Build.VERSION.SDK_INT >= 29){
                                                                DBupload_29();
                                                                Log.e("yoontaeho","29실행");
                                                            } else{
                                                                DBupload();// 업로드 하기 29이하
                                                                Log.e("yoontaeho","29 미만 실행");
                                                            }
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                                }

                                            }  else{
                                                startToast("존재하지 않는 추천인입니다.");
                                                profile_edittext_recommend.setText(null);
                                            }
                                        } // onDataChange(recommend)

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }else{
                                    if(android.os.Build.VERSION.SDK_INT >= 29){
                                        DBupload_29();
                                        Log.e("yoontaeho","29실행");
                                    } else{
                                        DBupload();// 업로드 하기 29이하
                                        Log.e("yoontaeho","29 미만 실행");
                                    }
                                }
                            } // else - 다 입력하고 닉네임까지 중복이 아닐때
                        } // onDatacChange(닉네임 중복검사)

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } // else
            } // click
        }); // 사인 업

    } // onCreate


    public void DBupload(){

        if(flagvalue == 1)
            return;

        flagvalue = 1;
        Log.d("namgung","디비 업로드");
        customProgressDialog.show();
        signup.setEnabled(false);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(profile_edittext_name.getText().toString())
                .build();
        Objects.requireNonNull(user).updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                }); // 디스플레이 네임 등록

        String realpath = getRealpath(oneImageUri);
        Bitmap bmp = null;
        byte[] data = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), oneImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bmp != null) {
            ExifInterface ei = null;
            try {
                ei = new ExifInterface(realpath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int orientation = 0;
            if (ei != null) {
                orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
            }

            Bitmap rotatedBitmap = null;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bmp, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bmp, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bmp, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bmp;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (rotatedBitmap != null) {
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                data = baos.toByteArray();
            }
        }


        userModel = new UserModel();
        photo = new UserModel.Photo();
        final StorageReference profileImageRef = FirebaseStorage.getInstance().getReference().child("userImages").child(uid).child(tempHashCode);
        profileImageRef.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Task<Uri> imageUrl = Objects.requireNonNull(task.getResult()).getStorage().getDownloadUrl();
                while (!imageUrl.isComplete());

                photo.temp =  Objects.requireNonNull(imageUrl.getResult()).toString();
                photo.tempHashCode = tempHashCode;
                userModel.userName = profile_edittext_name.getText().toString();
                userModel.sex = sex;
                userModel.tall = profile_textview_tall.getText().toString();
                userModel.body = profile_textview_body.getText().toString();
                userModel.age = age;
                userModel.personality = profile_textview_personality.getText().toString();
                userModel.heart = "100";
                userModel.email = email;
                userModel.platform = "android";

                DeviceInfo deviceInfo = new DeviceInfo(androidId, email, password);
                FirebaseDatabase.getInstance().getReference().child("deviceinfo").push().setValue(deviceInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    Log.d("namgung","채널생성 성공");
                                    String channelId = getString(R.string.namgung_channel_id);
                                    String channelName = getString(R.string.namgung_channel_name);

                                    NotificationManager notificationManager =
                                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                                    notificationChannel.setDescription("channel description");
                                    notificationChannel.enableLights(true);
                                    notificationChannel.setLightColor(Color.GREEN);
                                    notificationChannel.enableVibration(true);
                                    notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
                                    assert notificationManager != null;
                                    notificationManager.createNotificationChannel(notificationChannel);

                                } // 첫 가입시 푸시를 위한 채널생성

                                passPushTokenToServer();//푸시토큰 생성
                                SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("join", 2);
                                editor.apply();

                                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
                                Date time = new Date();
                                String time1 = format.format(time);

                                String buys_date = time1;
                                String buys_comment = "무료충전";
                                String buys_change = "+100";
                                String current_heart = "100";
                                String buys_id = "temp";
                                BuyModel buyModel = new BuyModel(buys_date,buys_comment,buys_change,current_heart,buys_id);
                                FirebaseDatabase.getInstance().getReference().child("Buys").child(uid).push().setValue(buyModel);



                                Intent intent1 = new Intent(ProfileSignupActivity.this, MainActivity.class);
                                startActivity(intent1);
                                customProgressDialog.dismiss();
                                signup.setEnabled(true);
                                overridePendingTransition(R.anim.fromright, R.anim.toleft);
                                ProfileSignupActivity.this.finish();


                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").push().setValue(photo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(uid).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                FirebaseDatabase.getInstance().getReference().child("administer").child("sinabro").child("hyeon").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        String masterUid = dataSnapshot.getValue(String.class);
                                                        assert masterUid != null;
                                                        FirebaseDatabase.getInstance().getReference().child("users").child(masterUid).child("pushToken").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                masterPushToken = dataSnapshot.getValue(String.class);
                                                                sendGcmAndroid();
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        }); // 관리자 푸시토큰 받기
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                }); // 관리자에게 푸시 보내기
                                            }
                                        });

                                    }
                                }); // 관리자 계정에 업로드
                            } // onSuccess()
                        }); // 유저정보 업로드
                    }
                }); // 디바이스 정보 업로드
            }
        }); //  스토리지 업로드
    }


    void sendGcmAndroid() {

        Gson gson = new Gson();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = masterPushToken;
        notificationModel.data.title = "심사요청";
        notificationModel.data.text = "신규고객님이 사진심사를 요청하셨습니다.";
        notificationModel.data.click_action = "p";

        RequestBody requestBody = RequestBody.create(gson.toJson(notificationModel), MediaType.parse("application/json; charset=utf8"));
        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=AAAAj2R4qNg:APA91bH2ZoT0hU5yDFYF24aea9CIw7m7gGHjb08PIHxgcIyLQfDhS_hDVG6tO8ld3yRR3Wabr4pawOiDSFGWC1wxoMmutG-gsXjIO4crrn7HDie0t2MaGBftNJQhSk4PY9y2rZwpBqVq")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }

    public void DBupload_29(){

        if(flagvalue == 1)
            return;

        flagvalue = 1;
        Log.d("namgung","디비 업로드");
        customProgressDialog.show();
        signup.setEnabled(false);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(profile_edittext_name.getText().toString())
                .build();
        Objects.requireNonNull(user).updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                }); // 디스플레이 네임 등록

        String realpath = getRealpath(oneImageUri);


        userModel = new UserModel();
        photo = new UserModel.Photo();

        FirebaseStorage.getInstance().getReference().child("userImages").child(uid).
                child(tempHashCode).putFile(oneImageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Task<Uri> imageUrl = Objects.requireNonNull(task.getResult()).getStorage().getDownloadUrl();
                        while (!imageUrl.isComplete());

                        photo.temp =  Objects.requireNonNull(imageUrl.getResult()).toString();
                        photo.tempHashCode = tempHashCode;
                        userModel.userName = profile_edittext_name.getText().toString();
                        userModel.sex = sex;
                        userModel.tall = profile_textview_tall.getText().toString();
                        userModel.body = profile_textview_body.getText().toString();
                        userModel.age = age;
                        userModel.personality = profile_textview_personality.getText().toString();
                        userModel.heart = "100";
                        userModel.email = email;
                        userModel.platform = "android";

                        DeviceInfo deviceInfo = new DeviceInfo(androidId, email, password);
                        FirebaseDatabase.getInstance().getReference().child("deviceinfo").push().setValue(deviceInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid)
                            {
                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            Log.d("namgung","채널생성 성공");
                                            String channelId = getString(R.string.namgung_channel_id);
                                            String channelName = getString(R.string.namgung_channel_name);

                                            NotificationManager notificationManager =
                                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                                            notificationChannel.setDescription("channel description");
                                            notificationChannel.enableLights(true);
                                            notificationChannel.setLightColor(Color.GREEN);
                                            notificationChannel.enableVibration(true);
                                            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
                                            assert notificationManager != null;
                                            notificationManager.createNotificationChannel(notificationChannel);

                                        } // 첫 가입시 푸시를 위한 채널생성

                                        passPushTokenToServer();//푸시토큰 생성
                                        SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putInt("join", 2);
                                        editor.apply();

                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
                                        Date time = new Date();
                                        String time1 = format.format(time);

                                        String buys_date = time1;
                                        String buys_comment = "무료충전";
                                        String buys_change = "+100";
                                        String current_heart = "100";
                                        String buys_id = "temp";
                                        BuyModel buyModel = new BuyModel(buys_date,buys_comment,buys_change,current_heart,buys_id);
                                        FirebaseDatabase.getInstance().getReference().child("Buys").child(uid).push().setValue(buyModel);


                                        Intent intent1 = new Intent(ProfileSignupActivity.this, MainActivity.class);
                                        startActivity(intent1);
                                        customProgressDialog.dismiss();
                                        signup.setEnabled(true);
                                        overridePendingTransition(R.anim.fromright, R.anim.toleft);
                                        ProfileSignupActivity.this.finish();


                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").push().setValue(photo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(uid).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });

                                            }
                                        }); // 관리자 계정에 업로드
                                    } // onSuccess()
                                }); // 유저정보 업로드
                            }
                        }); // 디바이스 정보 업로드


                    }
                });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode ==RESULT_OK){

            long nowTime = System.currentTimeMillis();
            simpleDateFormatTime.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            Date nowDate = new Date(nowTime);
            tempHashCode = simpleDateFormatTime.format(nowDate); // 사진을 업로드한 시간을 받는다.

            sub_imageView = findViewById(R.id.sub_image);
            sub_imageView.setVisibility(View.GONE);
            oneImageUri = data.getData();// 이미지 경로 원본
            Glide.with(this).load(oneImageUri).centerCrop()
                    .into(smallProfile);

            linearLayout_photo.setVisibility(View.VISIBLE);
            Glide.with(this).load(oneImageUri).centerCrop()
                    .into(bigProfile);

        }
    }


    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ProfileSignupActivity.this,R.style.AlertDialogStyle);

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage("회원가입을 취소하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("가입취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {

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
                                                            ProfileSignupActivity.this.finish();
                                                            overridePendingTransition(R.anim.fromleft, R.anim.toright);
                                                        }
                                                    }); // 계정삭제
                                                    finish();
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
                                dialog.cancel();
                            }
                        });
        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();
        // 다이얼로그 보여주기
        alertDialog.show();
    }

    private void startPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE} , 1);
    }

    private void requestPermissions() {
        boolean shouldProviceRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE);

        if( shouldProviceRationale ) {
            new AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("저장소 권한이 필요합니다.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startPermissionRequest();
                        }
                    })
                    .create()
                    .show();
        } else {
            startPermissionRequest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    albumFlag = 1;
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("알림")
                            .setMessage("저장소 권한이 필요합니다. 환경 설정에서 저장소 권한을 허가해주세요.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package",
                                            BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .create()
                            .show();
                }
        }
    }

    private void manPersonDialog(){
        dialogItemList = new ArrayList<>();

        final String text[] = {"지적인","차분한", "유머있는" , "낙천적인" , "내향적인", "외향적인", "감성적인", "상냥한", "귀여운", "열정적인","듬직한","개성있는"};

        for(int i=0; i<text.length;i++)
        {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put(TAG_TEXT, text[i]);
            dialogItemList.add(itemMap);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSignupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.personality_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSignupActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                profile_textview_personality.setText(text[position]);
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void womanPersonDialog(){
        dialogItemList = new ArrayList<>();

        final String text[] = {"지적인","차분한", "유머있는" , "낙천적인" , "내향적인", "외향적인", "감성적인", "상냥한", "귀여운", "섹시한","4차원","발랄한", "도도한"};

        for(int i=0; i<text.length;i++)
        {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put(TAG_TEXT, text[i]);
            dialogItemList.add(itemMap);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSignupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.personality_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSignupActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                profile_textview_personality.setText(text[position]);
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();

    }


    private void manBodyDialog(){

        dialogItemList = new ArrayList<>();
        final String text[] = {"평범한", "통통한", "근육질", "건장한" , "마른", "슬림탄탄"};

        for(int i=0; i<text.length;i++)
        {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put(TAG_TEXT, text[i]);
            dialogItemList.add(itemMap);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSignupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.body_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSignupActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                profile_textview_body.setText(text[position]);
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void womanBodyDialog(){
        dialogItemList = new ArrayList<>();

        final String text[] = {"평범한", "통통한", "살짝볼륨", "글래머" , "마른", "슬림탄탄"};

        for(int i=0; i<text.length;i++)
        {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put(TAG_TEXT, text[i]);
            dialogItemList.add(itemMap);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSignupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.body_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSignupActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                profile_textview_body.setText(text[position]);
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void  heightDialog()
    {
        int k=0;
        final String[] text =new String[51];
        dialogItemList = new ArrayList<>();

        for(int j=150; j<201; j++){
            text[k] = String.valueOf(j);
            k++;
        }

        for(int i=0; i<text.length;i++)
        {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put(TAG_TEXT, text[i]);
            dialogItemList.add(itemMap);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSignupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.height_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSignupActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                profile_textview_tall.setText(text[position]+"cm");
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void birthDialog() {

        int k=0;
        final String[] text =new String[23];
        dialogItemList = new ArrayList<>();


        for(int j=2003; j>1980; j--){
            text[k] = String.valueOf(j);
            k++;
        }

        for(int i=0; i<text.length;i++)
        {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put(TAG_TEXT, text[i]);
            dialogItemList.add(itemMap);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSignupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.birthday_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        long nowTime = System.currentTimeMillis();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        Date nowDate = new Date(nowTime);
        String getTime = simpleDateFormat.format(nowDate);
        String nowYear= getTime.substring(0,4);
        final int year = Integer.parseInt(nowYear);


        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSignupActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                profile_textview_age.setText(  (year - Integer.parseInt(text[position])+1) + "세" );
                age = text[position];
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    public String getRealpath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        @SuppressLint("Recycle") Cursor c = getContentResolver().query(uri, proj, null, null, null);
        assert c != null;
        int index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        c.moveToFirst();
        String path = c.getString(index);

        return path;
    }


    void passPushTokenToServer(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
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
}