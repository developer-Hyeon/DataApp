package sinabro.today.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
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
import sinabro.today.custom.CustomProgressDialog;
import sinabro.today.model.NotificationModel;
import sinabro.today.model.UserModel;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class ProfileSettingActivity extends AppCompatActivity {
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
    private static final int PICK_FROM_ALBUM = 10;
    private Toolbar toolbar;
    private ImageView[] imageViews;
    private TextView textView_name;
    private TextView textView_age;
    private TextView textView_personality;
    private TextView textView_tall;
    private TextView textView_body;
    private EditText editText_comment;
    private String uid;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private List<String>  photoids;
    private Map<String, UserModel.Photo> photos;
    private int photo_number;
    private RecyclerView recyclerView;
    private int realPhotoSize =0;
    private List<String> photoGlide;
    private int oneTimeReadFlag=0;
    private int checkDuplication;
    private CustomProgressDialog customProgressDialog;
    private List<Map<String, Object>> dialogItemList;
    private static final String TAG_TEXT = "text";
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private String my_sex;
    private int requestCheck=0;
    private String age;


    private List<UserModel.Photo> test;

    private String masterPushToken;

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);
        getWindow().setStatusBarColor(Color.parseColor("#865FF8"));

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        customProgressDialog = new CustomProgressDialog(ProfileSettingActivity.this);
        Objects.requireNonNull(customProgressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        requestPermissions(); // 권한 없으면 묻기
        toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        long nowTime = System.currentTimeMillis();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        Date nowDate = new Date(nowTime);
        String getTime = simpleDateFormat.format(nowDate);
        String nowYear= getTime.substring(0,4);
        final int year = Integer.parseInt(nowYear);

        textView_name = findViewById(R.id.setting_name);
        textView_age = findViewById(R.id.setting_age);
        textView_personality = findViewById(R.id.setting_textview_personality);
        textView_tall = findViewById(R.id.setting_textview_tall);
        textView_body = findViewById(R.id.setting_textview_body);
        editText_comment = findViewById(R.id.setting_edittext_comment);
        imageViews = new ImageView[5];
        imageViews[0] = findViewById(R.id.imageview0);
        imageViews[1] = findViewById(R.id.imageview1);
        imageViews[2] = findViewById(R.id.imageview2);
        imageViews[3] = findViewById(R.id.imageview3);
        imageViews[4] = findViewById(R.id.imageview4);

        photoids = new ArrayList<>();
        photos = new HashMap<>();
        photoGlide = new ArrayList<>();
        uid = FirebaseAuth.getInstance().getUid();
        Intent intent = getIntent();
        textView_name.setText(intent.getStringExtra("profileName"));
        age = intent.getStringExtra("profileAge");
        textView_age.setText((year - Integer.parseInt(age)+1) + "세");
        textView_personality.setText(intent.getStringExtra("profilePersonality"));
        textView_body.setText(intent.getStringExtra("profileBody"));
        textView_tall.setText(intent.getStringExtra("profileTall"));
        if(intent.getStringExtra("profileComment") != null) {
            editText_comment.setText(intent.getStringExtra("profileComment"));
        }

        mDatabase.child("users").child(user.getUid()).child("sex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                my_sex = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        textView_personality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( my_sex.equals("남자"))
                    manPersonDialog();
                else if(my_sex.equals("여자"))
                    womanPersonDialog();
            }
        });


        textView_tall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heightDialog();
            }
        });

        textView_body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( my_sex.equals("남자"))
                    manBodyDialog();
                else if(my_sex.equals("여자"))
                    womanBodyDialog();
            }
        });



        if( intent.getSerializableExtra("profilePhotos") != null ){
            photos = (Map<String, UserModel.Photo>) intent.getSerializableExtra("profilePhotos");
        }

        if( intent.getSerializableExtra("profilePhotosKey") != null ){
            photoids =  (List<String>) intent.getSerializableExtra("profilePhotosKey");
        }

        for (int i = 0; i < photos.size(); i++) {
            if (Objects.requireNonNull(photos.get(photoids.get(i))).temp != null)
                photoGlide.add(Objects.requireNonNull(photos.get(photoids.get(i))).temp);
            else
                photoGlide.add(Objects.requireNonNull(photos.get(photoids.get(i))).image);

            Glide.with(ProfileSettingActivity.this)
                    .load(photoGlide.get(i))
                    .apply(new RequestOptions().centerCrop())
                    .into(imageViews[i]);
        } // 글라이더로 그릴 리스트에 처음 넣어주고 그린다.

        imageViews[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo_number = 0;
                if(photoGlide.size() > photo_number)
                    photoAlbumClick(1, photo_number);
                else
                    photoAlbumClick(0,photo_number);
            }
        });


        imageViews[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo_number = 1;
                if(photoGlide.size() > photo_number)
                    photoAlbumClick(1, photo_number);
                else
                    photoAlbumClick(0,photo_number);
            }
        });

        imageViews[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo_number = 2;
                if(photoGlide.size() > photo_number)
                    photoAlbumClick(1, photo_number);
                else
                    photoAlbumClick(0,photo_number);
            }
        });

        imageViews[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo_number = 3;
                if(photoGlide.size() > photo_number)
                    photoAlbumClick(1, photo_number);
                else
                    photoAlbumClick(0,photo_number);
            }
        });

        imageViews[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo_number = 4;
                if(photoGlide.size() > photo_number)
                    photoAlbumClick(1, photo_number);
                else
                    photoAlbumClick(0,photo_number);
            }
        });


    } //oncreate

    public void photoAlbumClick(final int flag, final int position){
        if(flag == 1) { // 사진이 존재 할 경우 알람창 보여줌 아니면 그냥 앨범으로
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    ProfileSettingActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View alertView = inflater.inflate(R.layout.album_dialog, null);
            alertDialogBuilder.setView(alertView);
            // 다이얼로그 생성
            final AlertDialog alertDialog = alertDialogBuilder.create();
            final TextView textView_album = alertView.findViewById(R.id.album);
            textView_album.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent, PICK_FROM_ALBUM);
                    alertDialog.dismiss();
                }
            });
            final TextView textView_remove = alertView.findViewById(R.id.photoremove);
            textView_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteDatabase(position);
                    alertDialog.cancel();
                }
            });
            // 다이얼로그 보여주기
            alertDialog.show();
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_ALBUM);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode ==RESULT_OK){



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


            int updatePosition;
            int flag;
            if(photoGlide.size() > photo_number){
                photoGlide.remove(photo_number);
                photoGlide.add(photo_number, data.getData().toString());
                updatePosition = photo_number; // 중간에 끼어넣어라
                flag = 0;
            }
            else {
                photoGlide.add(data.getData().toString());
                updatePosition = photoGlide.size()-1; // 맨뒤에 업데이트해라
                flag = 1;
            }

            for(int i=0; i<photoGlide.size(); i++) {
                Glide.with(this)
                        .load(photoGlide.get(i))
                        .apply(new RequestOptions().centerCrop())
                        .into(imageViews[i]);
            }
            for(int i = photoGlide.size() ; i<5; i++)
                imageViews[i].setImageResource(R.drawable.border1);
            String realpath = getRealpath(data.getData());

            if(android.os.Build.VERSION.SDK_INT >= 29){
                uploadDatabase_29(data.getData(), updatePosition, flag, realpath);
                Log.e("yoontaeho","29실행");
            } else{
                uploadDatabase(data.getData(), updatePosition, flag, realpath);// 업로드 하기 29이하
                Log.e("yoontaeho","29 미만 실행");
            }
        }
    }


    void sendGcmAndroid() {

        Gson gson = new Gson();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = masterPushToken;
        notificationModel.data.title = "심사요청";
        notificationModel.data.text = "기존고객님이 사진심사를 요청하셨습니다.";
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


    public void uploadDatabase_29(final Uri tempimageUri, final int updatePosition, final int flag, final String realpath){
        final String newHashCode;
        String tempHashCode;
        Log.e("uri9", String.valueOf(tempimageUri));

        long nowTime = System.currentTimeMillis();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        Date nowDate = new Date(nowTime);
        newHashCode = simpleDateFormat.format(nowDate); // 사진을 업로드한 시간을 받는다.

        customProgressDialog.show(); // 로딩

        if(flag == 0) // 끼워넣기 할때 그 자리에 탬프가 존재하면 탬프를 교체
            if(Objects.requireNonNull(photos.get(photoids.get(updatePosition))).tempHashCode != null)
                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).child(Objects.requireNonNull(photos.get(photoids.get(updatePosition))).tempHashCode).delete();

        tempHashCode = newHashCode; // 새로운 사진을 추가하고자 하니깐 템프를 만들어준다.
        FirebaseStorage.getInstance().getReference().child("userImages").child(uid).
                child(tempHashCode).putFile(tempimageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Task<Uri> imageUrl = task.getResult().getStorage().getDownloadUrl();
                        while (!imageUrl.isComplete()) ;

                        UserModel userModel = new UserModel();
                        userModel.photoInfo.temp = imageUrl.getResult().toString();
                        userModel.photoInfo.tempHashCode = newHashCode;
                        if(flag == 0){ // 끼어넣기

                            Objects.requireNonNull(photos.get(photoids.get(updatePosition))).temp = userModel.photoInfo.temp; // 생성된 사진 클래스 넣기
                            Objects.requireNonNull(photos.get(photoids.get(updatePosition))).tempHashCode = userModel.photoInfo.tempHashCode; // 생성된 사진 클래스 넣기
                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").child(photoids.get(updatePosition)).child("temp").setValue(imageUrl.getResult().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Log.d("namgung","photo temp 저장완료");
                                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").child(photoids.get(updatePosition)).child("tempHashCode").setValue(newHashCode).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // Log.d("namgung","photo tempHashCode 저장완료");

                                            customProgressDialog.dismiss();
                                            //Log.d("namgung","로딩끝");

                                            FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(uid).setValue(true); ////요청에도 업로드
                                        }
                                    });

                                }
                            });

                        }else{ // 맨뒤에 새로 쓰기
                            String key = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").push().getKey();
                            photos.put(key,userModel.photoInfo); // 생성된 사진 클래스 넣기
                            photoids.add(key); // 생성된 key 넣기
                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").child(key).setValue(userModel.photoInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Log.d("namgung","photo 저장완료");
                                    customProgressDialog.dismiss();
                                    //Log.d("namgung","로딩끝");
                                    FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(uid).setValue(true); //요청에도 업로드
                                }
                            });
                        }
                    }
                });

    }


    public void uploadDatabase(final Uri tempimageUri, final int updatePosition, final int flag, final String realpath) {

        final String newHashCode;
        String tempHashCode;

        long nowTime = System.currentTimeMillis();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        Date nowDate = new Date(nowTime);
        newHashCode = simpleDateFormat.format(nowDate); // 사진을 업로드한 시간을 받는다.

        customProgressDialog.show(); // 로딩


        Bitmap bmp = null;
        byte[] data = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), tempimageUri);
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

        if(flag == 0) // 끼워넣기 할때 그 자리에 탬프가 존재하면 탬프를 교체
            if(Objects.requireNonNull(photos.get(photoids.get(updatePosition))).tempHashCode != null)
                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).child(Objects.requireNonNull(photos.get(photoids.get(updatePosition))).tempHashCode).delete();

        tempHashCode = newHashCode; // 새로운 사진을 추가하고자 하니깐 템프를 만들어준다.
        FirebaseStorage.getInstance().getReference().child("userImages").child(uid)
                .child(tempHashCode).putBytes(data)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Task<Uri> imageUrl = task.getResult().getStorage().getDownloadUrl();
                        while (!imageUrl.isComplete()) ;

                        UserModel userModel = new UserModel();
                        userModel.photoInfo.temp = imageUrl.getResult().toString();
                        userModel.photoInfo.tempHashCode = newHashCode;
                        if(flag == 0){ // 끼어넣기

                            Objects.requireNonNull(photos.get(photoids.get(updatePosition))).temp = userModel.photoInfo.temp; // 생성된 사진 클래스 넣기
                            Objects.requireNonNull(photos.get(photoids.get(updatePosition))).tempHashCode = userModel.photoInfo.tempHashCode; // 생성된 사진 클래스 넣기
                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").child(photoids.get(updatePosition)).child("temp").setValue(imageUrl.getResult().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Log.d("namgung","photo temp 저장완료");
                                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").child(photoids.get(updatePosition)).child("tempHashCode").setValue(newHashCode).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // Log.d("namgung","photo tempHashCode 저장완료");

                                            customProgressDialog.dismiss();
                                            //Log.d("namgung","로딩끝");

                                            FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(uid).setValue(true); ////요청에도 업로드
                                        }
                                    });

                                }
                            });

                        }else{ // 맨뒤에 새로 쓰기
                            String key = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").push().getKey();
                            photos.put(key,userModel.photoInfo); // 생성된 사진 클래스 넣기
                            photoids.add(key); // 생성된 key 넣기
                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").child(key).setValue(userModel.photoInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Log.d("namgung","photo 저장완료");
                                    customProgressDialog.dismiss();
                                    //Log.d("namgung","로딩끝");
                                    FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(uid).setValue(true); //요청에도 업로드
                                }
                            });
                        }


                    }
                });

    }

    public void deleteDatabase(final int position){

        if(photoGlide.size() > 1){
            customProgressDialog.show();
            photoGlide.remove(position);
            for(int i=0; i<photoGlide.size(); i++) {
                Glide.with(this)
                        .load(photoGlide.get(i))
                        .apply(new RequestOptions().centerCrop())
                        .into(imageViews[i]);
            }
            for(int i = photoGlide.size() ; i<5; i++)
                imageViews[i].setImageResource(R.drawable.border1);


            if(Objects.requireNonNull(photos.get(photoids.get(position))).image != null) {
                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).child(Objects.requireNonNull(photos.get(photoids.get(position))).imageHashCode).delete();
            }if(Objects.requireNonNull(photos.get(photoids.get(position))).temp != null) {
                //Log.d("namgung","템프해쉬코드 존재하므로 스토리지 삭제 "+ Objects.requireNonNull(photos.get(photoids.get(position))).tempHashCode);
                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).child(Objects.requireNonNull(photos.get(photoids.get(position))).tempHashCode).delete();
            }
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").child(photoids.get(position)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    customProgressDialog.dismiss();

                    photos.remove(photoids.get(position));
                    photoids.remove(position); // 지워주기

                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            requestCheck = 0;
                            for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if(snapshot.child("temp").exists()){
                                    requestCheck = 1;
                                    break;
                                }
                            }

                            if(requestCheck == 0)
                                FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(uid).removeValue(); // 요청 지워주기
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    }); // 템프가 존재하지 않으면 요청에서 관리자 요청 지워주기
                }
            });

        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    ProfileSettingActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View alertView = inflater.inflate(R.layout.photo_notice_dialog, null);
            alertDialogBuilder.setView(alertView);
            // 다이얼로그 생성
            final AlertDialog alertDialog = alertDialogBuilder.create();
            final TextView textView_yes = alertView.findViewById(R.id.noticeyes);
            textView_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                }
            });
            // 다이얼로그 보여주기
            alertDialog.show();
        }


    } // 해당 위치 사진 삭제



    @Override
    public void onBackPressed() {

        if(editText_comment.getText().toString().length()>0) {
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("comment").setValue(editText_comment.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                }
            });
        }
        if(valueEventListener!=null)
            databaseReference.removeEventListener(valueEventListener);
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:

                if(editText_comment.getText().toString().length()>0) {
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("comment").setValue(editText_comment.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
                }
                if(valueEventListener!=null)
                    databaseReference.removeEventListener(valueEventListener);
                finish();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void startPermissionRequest() {
        ActivityCompat.requestPermissions(ProfileSettingActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE} , 1);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettingActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.height_dialog, null);
        builder.setView(view);

        final ListView listview = (ListView)view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSettingActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                textView_tall.setText(text[position]+"cm");
                mDatabase.child("users").child(user.getUid()).child("tall").setValue(text[position]+"cm");
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettingActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.body_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSettingActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                textView_body.setText(text[position]);
                mDatabase.child("users").child(user.getUid()).child("body").setValue(text[position]);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettingActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.body_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSettingActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                textView_body.setText(text[position]);
                mDatabase.child("users").child(user.getUid()).child("body").setValue(text[position]);
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void manPersonDialog(){
        dialogItemList = new ArrayList<>();

        final String text[] = {"지적인","차분한", "유머있는" ,
                "낙천적인" , "내향적인", "외향적인", "감성적인",
                "상냥한", "귀여운", "열정적인","듬직한","개성있는"};


        for(int i=0; i<text.length;i++)
        {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put(TAG_TEXT, text[i]);
            dialogItemList.add(itemMap);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettingActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.personality_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSettingActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                textView_personality.setText(text[position]);
                mDatabase.child("users").child(user.getUid()).child("personality").setValue(text[position]);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettingActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.personality_dialog, null);
        builder.setView(view);

        final ListView listview = view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileSettingActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_TEXT},
                new int[]{R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                textView_personality.setText(text[position]);
                mDatabase.child("users").child(user.getUid()).child("personality").setValue(text[position]);
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();

    }

    public String getRealpath(Uri uri) {
        Log.e("uri", uri.toString());
        String[] proj = {MediaStore.Images.Media.DATA};
        @SuppressLint("Recycle") Cursor c = getContentResolver().query(uri, proj, null, null, null);
        assert c != null;
        int index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        c.moveToFirst();
        String path = c.getString(index);

        return path;
    }

}