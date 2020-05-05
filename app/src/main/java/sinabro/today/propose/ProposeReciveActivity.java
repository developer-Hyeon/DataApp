package sinabro.today.propose;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.database.ServerValue;
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
import sinabro.today.model.ChatModel;
import sinabro.today.model.NotificationModel;
import sinabro.today.model.UserModel;
import sinabro.today.popup.AgreePopupActivity;
import sinabro.today.popup.BlockPopupActivity;
import sinabro.today.popup.DisagreePopupActivity;

public class ProposeReciveActivity extends AppCompatActivity {
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss a");
    private RecyclerView recyclerView;
    private String destinationUid;
    private Button backButton;
    private LinearLayout linearLayout_yesButton;
    private LinearLayout linearLayout_noButton;
    private TextView name;
    private TextView age;
    private TextView info;
    private TextView comment;
    private String chatRoomUid;
    private UserModel destinationUserModel;
    private String myImage="";
    private TextView propose_recive_nonphoto;
    private TextView distance_between;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    Double desti_latitude,desti_longitude, my_latitude, my_longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propose_recive);

        Intent intent = getIntent();
        destinationUid = intent.getStringExtra("destinationUid");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    if(item.child("image").exists()){
                        myImage = item.child("image").getValue(String.class);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        name = findViewById(R.id.recive_name);
        age = findViewById(R.id.recive_age);
        info = findViewById(R.id.recive_info);
        comment = findViewById(R.id.recive_comment);
        backButton = findViewById(R.id.revice_backbutton);
        linearLayout_yesButton = findViewById(R.id.recive_yesButton);
        linearLayout_noButton  = findViewById(R.id.recive_noButton);
        propose_recive_nonphoto = findViewById(R.id.propose_recive_nonphoto);
        distance_between = findViewById(R.id.distance_between_receive);

        SharedPreferences sf = getSharedPreferences("sinabro",MODE_PRIVATE);
        String my_lat = sf.getString("my_latitude","127");
        String my_long = sf.getString("my_longitude","37");
        my_latitude = Double.parseDouble(my_lat);
        my_longitude =  Double.parseDouble(my_long);

        mDatabase.child("users").child(destinationUid).child("sex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String desti_sex = dataSnapshot.getValue(String.class);
                if(Objects.equals(desti_sex, "남자")){
                    mDatabase.child("man_location").child(destinationUid).child("latitude").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                desti_latitude = dataSnapshot.getValue(Double.class);
                                mDatabase.child("man_location").child(destinationUid).child("longitude").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        desti_longitude = dataSnapshot.getValue(Double.class);
                                        double distance = getDistance(my_latitude, my_longitude, desti_latitude, desti_longitude);
                                        int distance_int = (int) Math.round(distance);
                                        int distance_1000 = distance_int / 1000;
                                        if (distance_1000 >= 1) {
                                            String distance_str = Integer.toString(distance_1000);
                                            distance_between.setText("- 거리 " + distance_str + "km");
                                        } else {
                                            distance_between.setText("- 거리 0km");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }  else{
                                distance_between.setText("위치 끔");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else if(Objects.equals(desti_sex, "여자")){
                    mDatabase.child("woman_location").child(destinationUid).child("latitude").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            desti_latitude = dataSnapshot.getValue(Double.class);
                            mDatabase.child("woman_location").child(destinationUid).child("longitude").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    desti_longitude = dataSnapshot.getValue(Double.class);
                                    double distance = getDistance(my_latitude,my_longitude, desti_latitude, desti_longitude);
                                    int distance_int = (int) Math.round(distance);
                                    int distance_1000 = distance_int / 1000;
                                    if(distance_1000 >= 1){
                                        String distance_str = Integer.toString(distance_1000);
                                        distance_between.setText("거리 " + distance_str + "km");
                                    } else{
                                        distance_between.setText("거리 0km");
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
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



        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ProposeReciveActivity.this, MainActivity.class);
                // startActivity(intent);
                ProposeReciveActivity.this.finish();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);

            }
        });

        recyclerView = findViewById(R.id.recive_reclclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProposeReciveActivity.this,LinearLayoutManager.HORIZONTAL,false));
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(new RecyclerViewAdapter());

    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<String> photos;
        List<String> keySet;
        RecyclerViewAdapter(){

            photos = new ArrayList<>();
            keySet = new ArrayList<>();
            destinationUserModel = new UserModel();
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    keySet.clear();
                    destinationUserModel = dataSnapshot.getValue(UserModel.class);

                    assert destinationUserModel != null;
                    if(destinationUserModel.photo.size() > 0 ){
                        Map<String, UserModel.Photo> photoMap = new TreeMap<>(Collections.reverseOrder());
                        photoMap.putAll(destinationUserModel.photo);
                        for(int i=destinationUserModel.photo.keySet().size()-1; i>=0 ; i--){
                            keySet.add((String) photoMap.keySet().toArray()[i]);
                        } // 정렬
                        for(int i=0; i<keySet.size(); i++){
                            if(Objects.requireNonNull(destinationUserModel.photo.get(keySet.get(i))).image != null){
                                photos.add(Objects.requireNonNull(destinationUserModel.photo.get(keySet.get(i))).image);
                            }
                        } // 이미지 넣기
                    }


                    name.setText(destinationUserModel.userName);

                    long nowTime = System.currentTimeMillis();
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                    Date nowDate = new Date(nowTime);
                    String getTime = simpleDateFormat.format(nowDate);
                    int nowYear= Integer.parseInt(getTime.substring(0,4));
                    if( (nowYear- Integer.parseInt(destinationUserModel.age)+1) < 20 )
                        age.setText("10대");
                    else if( (nowYear- Integer.parseInt(destinationUserModel.age)+1) < 24 )
                        age.setText("20대 초반");
                    else if( (nowYear- Integer.parseInt(destinationUserModel.age)+1) < 27 )
                        age.setText("20대 중반");
                    else if( (nowYear- Integer.parseInt(destinationUserModel.age)+1) < 30 )
                        age.setText("20대 후반");
                    else if( (nowYear- Integer.parseInt(destinationUserModel.age)+1) < 34 )
                        age.setText("30대 초반");
                    else if( (nowYear- Integer.parseInt(destinationUserModel.age)+1) < 37 )
                        age.setText("30대 중반");
                    else if( (nowYear- Integer.parseInt(destinationUserModel.age)+1) < 40 )
                        age.setText("30대 후반");
                    else
                        age.setText("40대 이상");

                    info.setText(destinationUserModel.tall+", "+ destinationUserModel.body+" 체형과 "+ destinationUserModel.personality+" 성격을 가진 "+destinationUserModel.sex+"입니다.");
                    if(destinationUserModel.comment != null) {
                        comment.setVisibility(View.VISIBLE);
                        comment.setText(destinationUserModel.comment);
                    }else{
                        comment.setVisibility(View.INVISIBLE);
                    }

                    notifyDataSetChanged();
                    if(photos.size() == 0)
                        propose_recive_nonphoto.setVisibility(View.VISIBLE);
                    else
                        propose_recive_nonphoto.setVisibility(View.GONE);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recive, parent, false);
            return new ReciveViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            ReciveViewHolder reciveViewHolder = (ReciveViewHolder) holder;

            if (photos.size() > 1) {
                reciveViewHolder.textView_count.setVisibility(View.VISIBLE);
                reciveViewHolder.textView_count.setText("" + (position + 1) + "/" + "" + photos.size());
            } else {
                reciveViewHolder.textView_count.setVisibility(View.INVISIBLE);
            }

            Glide.with(holder.itemView.getContext())
                    .load(photos.get(position))
                    .apply(new RequestOptions().centerCrop())
                    .into(reciveViewHolder.imageView);


            reciveViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), PictureZoom.class);
                    intent.putExtra("photourl", photos.get(position));
                    startActivity(intent);
                }
            });
        }
        @Override
        public int getItemCount() {
            return photos.size();
        }


        private class ReciveViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView  textView_count;

            public ReciveViewHolder(View view) {
                super(view);

                textView_count = view.findViewById(R.id.recive_item_count);
                imageView = view.findViewById(R.id.recive_item_imageview);
            }
        }
    } // 리사이클러뷰

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
        finish();
    }

    public void mOnBlock(View v){
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, BlockPopupActivity.class);
        intent.putExtra("data", "Test Popup");
        startActivityForResult(intent, 1);
    }

    //버튼
    public void mOnAgree(View v){
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, AgreePopupActivity.class);
        intent.putExtra("data", "Test Popup");
        startActivityForResult(intent, 1);
    }

    //버튼
    public void mOndisAgree(View v){
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, DisagreePopupActivity.class);
        intent.putExtra("data", "Test Popup");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){ // 한잔할래요 수락합니다
                //데이터 받기
                int result = data.getIntExtra("result",0);
                if(result == 1){
                    checkChatRoom(); // 중복검사
                    String uid = FirebaseAuth.getInstance().getUid();
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("pushList").child(destinationUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ProposeReciveActivity.this.finish();
                        }
                    });

                } else if (result == 2){
                    String uid = FirebaseAuth.getInstance().getUid();
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("pushList").child(destinationUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //Intent intent = new Intent(ProposeReciveActivity.this, MainActivity.class);
                            //startActivity(intent);
                            ProposeReciveActivity.this.finish();
                        }
                    });
                }else if(result == 3){ // 차단하겠다는 팝업
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
                }
            }
        }
    }

    void checkChatRoom() {

        Log.d("namgung", "checkChatRoom 진입");
        final String myuid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + myuid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                Log.d("namgung", "true check 진입");
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    chatRoomUid = item.getKey();
                    assert chatModel != null;
                    if (chatModel.users.containsKey(destinationUid)) { // 여기는 무족건 방 생성 안해준다.
                        if(chatModel.users.get(destinationUid)){
                            Log.d("namgung", "나 true  너 true 이니깐 방생성 안함");
                        }else{
                            Log.d("namgung", "나 true  너 false 이니깐 방생성 안함 너를 true 로 바꿔줌");
                            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("platform").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String platform = dataSnapshot.getValue(String.class);
                                    if(platform.equals("android")){
                                        sendGcmAndroid();
                                    }else{
                                        sendGcmIos();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").child(destinationUid).setValue(true);
                        }
                        return; // 더 볼 필요도 없다. 리턴
                    }
                }

                FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + myuid).equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            ChatModel chatModel = item.getValue(ChatModel.class);
                            chatRoomUid = item.getKey();
                            assert chatModel != null;
                            if (chatModel.users.containsKey(destinationUid)) { // 여기는 무족건 방 생성 안해준다.
                                if(chatModel.users.get(destinationUid)){
                                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").child(myuid).setValue(true);
                                    FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("platform").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String platform = dataSnapshot.getValue(String.class);
                                            if(platform.equals("android")){
                                                sendGcmAndroid();
                                            }else{
                                                sendGcmIos();
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                return; // 더 볼 필요도 없다.
                            }
                        }

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").push();
                        DatabaseReference databaseReference1 = databaseReference.child("comments").push();
                        Log.d("namgung","방생성 해준다.");
                        ChatModel chatModel = new ChatModel();
                        chatModel.users.put(myuid, true);
                        chatModel.users.put(destinationUid, true);
                        ChatModel.Comment comment = new ChatModel.Comment();
                        comment.uid =myuid;
                        comment.message = "축하드립니다! 채팅방이 개설되었습니다.";
                        comment.timestamp = ServerValue.TIMESTAMP;
                        chatModel.comments.put(databaseReference1.getKey(), comment);
                        databaseReference.setValue(chatModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("platform").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String platform = dataSnapshot.getValue(String.class);
                                        if(platform.equals("android")){
                                            sendGcmAndroid();
                                        }else{
                                            sendGcmIos();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }@Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }); // 내가 false인 부분을 다 받아옴
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }); // 내가 true인 부분을 다 받아옴
    }


    void sendGcmAndroid() {
        Gson gson = new Gson();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = destinationUserModel.pushToken;
        notificationModel.data.title = "축하합니다!";
        notificationModel.data.text = userName+"님과의 대화방이 생성되었습니다.";
        notificationModel.data.click_action = "r"+myImage;

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

        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("flagRecive").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(Objects.equals(dataSnapshot.getValue(int.class), 0)){

                        Gson gson = new Gson();
                        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                        NotificationModel notificationModel = new NotificationModel();
                        notificationModel.to = destinationUserModel.pushToken;
                        notificationModel.notification.title = "축하합니다!";
                        notificationModel.notification.text = userName+"님과의 대화방이 생성되었습니다.";
                        notificationModel.notification.click_action = "r";
                        notificationModel.data.title = "축하합니다!";
                        notificationModel.data.text = userName+"님과의 대화방이 생성되었습니다.";
                        notificationModel.data.click_action = "r";


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