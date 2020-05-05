package sinabro.today.propose;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sinabro.today.PictureZoom;
import sinabro.today.R;
import sinabro.today.model.NotificationModel;
import sinabro.today.model.UserModel;
import sinabro.today.popup.BlockPopupActivity;
import sinabro.today.popup.FreePopupActivity;
import sinabro.today.popup.GoStorePopupActivity;
import sinabro.today.popup.ProposePopupActivity;
import sinabro.today.store.StoreActivity;
public class ProposeProfileActivity extends AppCompatActivity {

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss a");
    private int   userHeart;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Button backButton;
    private LinearLayout linearLayout_proposeButton;
    private TextView propose_name;
    private TextView propose_age;
    private TextView propose_info;
    private TextView propose_comment;
    private String destinationUid;
    private UserModel destinationUserModel;
    private String myImage;
    private String where;
    private ValueEventListener valueEventListener;
    private ValueEventListener valueEventListener2;
    private ValueEventListener valueEventListener3;
    private ValueEventListener valueEventListener4;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Query databaseReference, databaseReference2, databaseReference3, databaseReference4;
    private TextView propose_profile_nonphoto;
    private String destinationSex;
    private TextView distance_between;
    Double desti_latitude,desti_longitude, my_latitude, my_longitude;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormatTime =  new SimpleDateFormat("yyyy.MM.dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propose_profile);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        propose_name = findViewById(R.id.propose_profile_name);
        propose_age = findViewById(R.id.propose_profile_age);
        propose_info = findViewById(R.id.propose_profile_info);
        propose_comment = findViewById(R.id.propose_profile_comment);
        backButton = findViewById(R.id.propose_profile_backbutton);
        linearLayout_proposeButton = findViewById(R.id.propose_profile_proposeButton);
        propose_profile_nonphoto = findViewById(R.id.propose_profile_nonphoto);
        distance_between = findViewById(R.id.distance_between);

        Intent intent = getIntent();
        destinationUid = intent.getStringExtra("destinationUid");
        desti_latitude = intent.getDoubleExtra("latitude", 0);
        desti_longitude = intent.getDoubleExtra("longitude", 0);

        where = intent.getStringExtra("where");
        if(Objects.equals(where, "m")){
            linearLayout_proposeButton.setVisibility(View.GONE);
        }else{
            linearLayout_proposeButton.setVisibility(View.VISIBLE);

        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.fromright, R.anim.toleft);
                finish();
            }
        });

        recyclerView = findViewById(R.id.propose_profile_reclclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProposeProfileActivity.this,LinearLayoutManager.HORIZONTAL,false));
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(new RecyclerViewAdapter());

        SharedPreferences sf = getSharedPreferences("sinabro",MODE_PRIVATE);
        String my_lat = sf.getString("my_latitude","127");
        String my_long = sf.getString("my_longitude","37");
        my_latitude = Double.parseDouble(my_lat);
        my_longitude =  Double.parseDouble(my_long);

        if(desti_latitude == 0 && desti_longitude == 0){
            distance_between.setVisibility(View.INVISIBLE);
        } else{
            distance_between.setVisibility(View.VISIBLE);
            double distance = getDistance(my_latitude,my_longitude, desti_latitude, desti_longitude);
            int distance_int = (int) Math.round(distance);
            int distance_1000 = distance_int / 1000;
            if(distance_1000 >= 1){
                String distance_str = Integer.toString(distance_1000);
                distance_between.setText("- 거리 " + distance_str + "km");
            } else{
                distance_between.setText("- 거리 0km");
            }

        }

        mDatabase.child("users").child(destinationUid).child("sex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                destinationSex = dataSnapshot.getValue(String.class);

                if(Objects.equals(destinationSex, "남자")){
                    databaseReference4 = mDatabase.child("man_location").child(Objects.requireNonNull(destinationUid));
                    valueEventListener4 = databaseReference4.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!(dataSnapshot.exists())) {
                                Log.e("db4","남자");
                                SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("profile", 4); // 로그아웃 했거나 위치설정 껐을 때
                                editor.apply();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else if(Objects.equals(destinationSex, "여자")){
                    databaseReference4 = mDatabase.child("woman_location").child(Objects.requireNonNull(destinationUid));
                    valueEventListener4 = databaseReference4.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!(dataSnapshot.exists())) {
                                Log.e("db4","여자");
                                SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("profile", 4); // 로그아웃 했거나 위치설정 껐을 때
                                editor.apply();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        databaseReference = mDatabase.child("users").child(Objects.requireNonNull(user.getUid())).child("blockList");
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (Objects.requireNonNull(snapshot.getKey()).equals(destinationUid)) {
                        SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("profile", 1); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐)
                        editor.apply();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference2 = mDatabase.child("users").child(Objects.requireNonNull(user.getUid())).child("blockedList");
        valueEventListener2 = databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (Objects.requireNonNull(snapshot.getKey()).equals(destinationUid)) {
                        SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("profile", 2); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐)
                        editor.apply();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference3 = mDatabase.child("users").child(Objects.requireNonNull(destinationUid));
        valueEventListener3 = databaseReference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.exists())) {
                    SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("profile", 3); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐)
                    editor.apply();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }// oncreate
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<String> keySet;
        List<String> photos;
        public RecyclerViewAdapter() {
            keySet = new ArrayList<>();
            photos = new ArrayList<>();
            destinationUserModel = new UserModel();
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        keySet.clear();
                        destinationUserModel = dataSnapshot.getValue(UserModel.class);
                        assert destinationUserModel != null;
                        Map<String, UserModel.Photo> photoMap = new TreeMap<>(Collections.reverseOrder());
                        photoMap.putAll(destinationUserModel.photo);
                        for (int i = destinationUserModel.photo.keySet().size() - 1; i >= 0; i--) {
                            keySet.add((String) photoMap.keySet().toArray()[i]);
                        } // 정렬
                        for (int i = 0; i < keySet.size(); i++) {
                            if (Objects.requireNonNull(destinationUserModel.photo.get(keySet.get(i))).image != null) {
                                photos.add(Objects.requireNonNull(destinationUserModel.photo.get(keySet.get(i))).image);
                            }
                        } // 이미지 넣기

                        propose_name.setText(destinationUserModel.userName);
                        long nowTime = System.currentTimeMillis();
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                        Date nowDate = new Date(nowTime);
                        String getTime = simpleDateFormat.format(nowDate);
                        int nowYear = Integer.parseInt(getTime.substring(0, 4));
                        if ((nowYear - Integer.parseInt(destinationUserModel.age) + 1) < 20)
                            propose_age.setText("10대");
                        else if ((nowYear - Integer.parseInt(destinationUserModel.age) + 1) < 24)
                            propose_age.setText("20대 초반");
                        else if ((nowYear - Integer.parseInt(destinationUserModel.age) + 1) < 27)
                            propose_age.setText("20대 중반");
                        else if ((nowYear - Integer.parseInt(destinationUserModel.age) + 1) < 30)
                            propose_age.setText("20대 후반");
                        else if ((nowYear - Integer.parseInt(destinationUserModel.age) + 1) < 34)
                            propose_age.setText("30대 초반");
                        else if ((nowYear - Integer.parseInt(destinationUserModel.age) + 1) < 37)
                            propose_age.setText("30대 중반");
                        else if ((nowYear - Integer.parseInt(destinationUserModel.age) + 1) < 40)
                            propose_age.setText("30대 후반");
                        else
                            propose_age.setText("40대 이상");

                        propose_info.setText(destinationUserModel.tall + ", " + destinationUserModel.body + " 체형과 " + destinationUserModel.personality + " 성격을 가진 " + destinationUserModel.sex + "입니다.");
                        if (destinationUserModel.comment != null) {
                            propose_comment.setVisibility(View.VISIBLE);
                            propose_comment.setText(destinationUserModel.comment);
                        } else {
                            propose_comment.setVisibility(View.INVISIBLE);
                        }

                        notifyDataSetChanged();
                        if(photos.size() == 0)
                            propose_profile_nonphoto.setVisibility(View.VISIBLE);
                        else
                            propose_profile_nonphoto.setVisibility(View.GONE);
                    }
                }// onDataChange()

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_propose_profile, parent, false);
            return new RecyclerViewAdapter.ProposeViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            RecyclerViewAdapter.ProposeViewHolder proposeViewHolder = (( RecyclerViewAdapter.ProposeViewHolder ) holder);
            Glide.with(holder.itemView.getContext())
                    .load(photos.get(position))
                    .apply(new RequestOptions().centerCrop())
                    .into(proposeViewHolder.imageView);

            if(photos.size() >1)
            {
                proposeViewHolder.textView.setVisibility(View.VISIBLE);
                proposeViewHolder.textView.setText(""+(position+1)+"/"+""+photos.size());
            }else{
                proposeViewHolder.textView.setVisibility(View.INVISIBLE);
            }


            proposeViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!(where.equals("m"))) {
                        Intent intent = new Intent(getApplicationContext(), PictureZoom.class);
                        intent.putExtra("photourl", photos.get(position));
                        startActivity(intent);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        private class ProposeViewHolder extends RecyclerView.ViewHolder {

            private ImageView imageView;
            private TextView  textView;

            private ProposeViewHolder(View view) {
                super(view);

                imageView = view.findViewById(R.id.propose_profile_item_imageview);
                textView = view.findViewById(R.id.propose_profile_item_count);
            }
        }
    }

    public void mOnBlock(View v){
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, BlockPopupActivity.class);
        intent.putExtra("data", "Test Popup");
        startActivityForResult(intent, 1);
    }


    //버튼
    public void mOnPropose(View v){
        //데이터 담아서 팝업(액티비티) 호출

        final String uid = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(uid)).child("freeCoupon").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String freeCoupon = dataSnapshot.getValue(String.class);
                    if(freeCoupon.equals("1")){ // 쿠폰이 있음

                        Intent intent = new Intent(ProposeProfileActivity.this, FreePopupActivity.class);
                        intent.putExtra("data", "Test Popup");
                        startActivityForResult(intent, 1);

                    }else{
                        FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(uid)).child("heart").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                String value = dataSnapshot.getValue(String.class);
                                userHeart = Integer.parseInt(Objects.requireNonNull(value));
                                if(userHeart >= 30)
                                {
                                    Intent intent = new Intent(ProposeProfileActivity.this, ProposePopupActivity.class);
                                    intent.putExtra("data", "Test Popup");
                                    startActivityForResult(intent, 1);
                                }else{
                                    Intent intent = new Intent(ProposeProfileActivity.this, GoStorePopupActivity.class);
                                    intent.putExtra("data", "Test Popup");
                                    startActivityForResult(intent, 1);
                                }

                            }
                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.w("namgung", "Failed to read value.", error.toException());
                            }
                        });
                    } // 쿠폰이 없음
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                //데이터 받기
                int result = data.getIntExtra("result",0);
                if(result == 1)
                {
                    if(userHeart >=30) {
                        userHeart -=30;
                        final String uid = FirebaseAuth.getInstance().getUid();
                        Map<String, Object> updateHeart = new HashMap<>();
                        updateHeart.put("heart",""+userHeart);
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(updateHeart).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                                            if(item.child("image").exists()){
                                                myImage = item.child("image").getValue(String.class);
                                                break;
                                            }else
                                                myImage = "";
                                        }
                                        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("platform").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String platform = dataSnapshot.getValue(String.class);
                                                if(Objects.equals(platform, "android")){
                                                    sendGcmAndroid();
                                                }else{
                                                    sendGcmIos();
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        long nowTime = System.currentTimeMillis();
                                        simpleDateFormatTime.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                                        Date nowDate = new Date(nowTime);
                                        String getTime = simpleDateFormatTime.format(nowDate);
                                        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("pushList").child(uid).setValue(getTime);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                    }else {
                        Intent intent = new Intent(ProposeProfileActivity.this, StoreActivity.class);
                        intent.putExtra("heart", "0");
                        overridePendingTransition(R.anim.fromright, R.anim.toleft);
                        startActivity(intent); // 스토어로 이동
                    }

                } // 확인버튼 클릭시 두가지 케이스 존재
                else if(result == 3){ // 차단하겠다는 팝업
                    // 차단 로직 설정
                    final String uid = FirebaseAuth.getInstance().getUid();
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("blockList").child(destinationUid).setValue(true); // 내가 차단한 사람 저장
                    FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("blockedList").child(uid).setValue(true); // 차단당한 사람한테 나를 저
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("pushList").child(destinationUid).removeValue(); // 푸시리스트 삭제
                    FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("pushList").child(uid).removeValue(); // 푸시리스트 삭제
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                if(item.child("users").child(destinationUid).exists()){
                                    Log.d("namgung",""+item.getKey());
                                    Map<String, Object> UsersMap = new HashMap<>();
                                    UsersMap.put(uid,false);
                                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(Objects.requireNonNull(item.getKey())).child("users")
                                            .updateChildren(UsersMap);
                                }
                            }

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    }); // 채팅방 지우기

                    finish();
                } // 차단
                else if(result == 4){
                    final String uid = FirebaseAuth.getInstance().getUid();
                    FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(uid)).child("freeCoupon").setValue("0");
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                if(item.child("image").exists()){
                                    myImage = item.child("image").getValue(String.class);
                                    break;
                                }else
                                    myImage = "";
                            }
                            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("platform").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String platform = dataSnapshot.getValue(String.class);
                                    if(Objects.equals(platform, "android")){
                                        sendGcmAndroid();
                                    }else{
                                        sendGcmIos();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            long nowTime = System.currentTimeMillis();
                            simpleDateFormatTime.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                            Date nowDate = new Date(nowTime);
                            String getTime = simpleDateFormatTime.format(nowDate);
                            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("pushList").child(uid).setValue(getTime);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } // 무료하트 사용
            }
        }
    } //


    void sendGcmAndroid() {

        Gson gson = new Gson();
        String userName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = destinationUserModel.pushToken;
        notificationModel.data.title = "대화요청";
        notificationModel.data.text = userName+"님이 대화를 요청하셨습니다.";
        notificationModel.data.click_action = "p"+myImage;

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

    void sendGcmIos() {

        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("flagPropose").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(Objects.equals(dataSnapshot.getValue(int.class), 0)){

                        Gson gson = new Gson();
                        String userName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
                        NotificationModel notificationModel = new NotificationModel();
                        notificationModel.to = destinationUserModel.pushToken;
                        notificationModel.notification.title = "대화요청";
                        notificationModel.notification.text = userName+"님이 대화를 요청하셨습니다.";
                        notificationModel.notification.click_action = "p";
                        notificationModel.data.title = "대화요청";
                        notificationModel.data.text = userName+"님이 대화를 요청하셨습니다.";
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
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fromright, R.anim.toleft);
        finish();
    }



    private void startToast(String msg) {
        Toast.makeText(ProposeProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(valueEventListener!=null) {
            databaseReference.removeEventListener(valueEventListener);
        }
        if(valueEventListener2!=null) {
            databaseReference2.removeEventListener(valueEventListener2);
        }
        if(valueEventListener3!=null) {
            databaseReference3.removeEventListener(valueEventListener3);
        }
        if(valueEventListener4!=null) {
            databaseReference4.removeEventListener(valueEventListener4);
        }
        overridePendingTransition(R.anim.fromright, R.anim.toleft);
        finish();
    }

    public double getDistance(double lat1 , double lng1 , double lat2 , double lng2 ){
        double distance;

        Location locationA = new Location("point A");
        locationA.setLatitude(lat1);
        locationA.setLongitude(lng1);

        Location locationB = new Location("point B");
        locationB.setLatitude(lat2);
        locationB.setLongitude(lng2);

        distance = locationA.distanceTo(locationB);

        return distance;
    }
}