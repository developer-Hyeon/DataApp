package sinabro.today.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import sinabro.today.R;
import sinabro.today.model.ChatModel;
import sinabro.today.model.NotificationModel;
import sinabro.today.popup.PopupActivity;
import sinabro.today.propose.ProposeProfileActivity;

public class MessageActivity extends AppCompatActivity {

    private String destinatonUid;
    private Button button;
    private EditText editText;
    private String uid;
    private String chatRoomUid;
    private RecyclerView recyclerView;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private SimpleDateFormat simpleDateFormatDate   = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN);
    private String destinationUserPushToken;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private DatabaseReference blockdatabaseReference;
    private ValueEventListener blockvalueEventListener;
    private Toolbar toolbar;
    private String destinationImage;
    private String destinationName;
    private DatabaseReference def;
    private ValueEventListener valueEvent;
    private DatabaseReference defout;
    private ValueEventListener valueEventout;
    int peopleCount = 0;
    int readCounter=0;
    int interval=0;
    int intervalTime=0;
    int checkDestinationUser=0;
    private TextView title;
    private String myImage="";
    private String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();  //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID
        destinatonUid = getIntent().getStringExtra("destinationUid"); // 상대방 아이디
        chatRoomUid = getIntent().getStringExtra("chatroomId"); // 채팅방 id
        destinationName = getIntent().getStringExtra("destinationName");
        destinationImage = getIntent().getStringExtra("destinationImage");
        title = findViewById(R.id.message_toolbar_title);
        title.setText(destinationName);

        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
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

        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(""+destinationName, 1);
        editor.apply();

        button =        findViewById(R.id.messageActivity_button);
        editText =      findViewById(R.id.messageActivity_editText);
        recyclerView =  findViewById(R.id.messageActivity_reclclerview);
        toolbar = findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel.Comment comment = new ChatModel.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                message = editText.getText().toString();
                Log.d("namgung"," 채팅 "+message);
                comment.timestamp = ServerValue.TIMESTAMP;
                if(!comment.message.equals("")) {
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseDatabase.getInstance().getReference().child("users").child(destinatonUid).child("platform").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String platform = dataSnapshot.getValue(String.class);
                                    if(platform.equals("android")){
                                        Log.d("namgung","안드로이드에게 푸시보냅니다.");
                                        sendGcmAndroid();
                                    }else{
                                        Log.d("namgung","ios에게 푸시보냅니다.");
                                        Log.d("namgung"," 채팅 "+message);
                                        sendGcmIos();
                                    }

                                    editText.setText("");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    });
                }

            }
        }); // 전송 버튼 클릭

        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
        recyclerView.setAdapter(new RecyclerViewAdapter());
    }

    //버튼
    public void mOnPopupClick(View v){
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("data", "채팅방에서 나가기를 하면 채팅 목록에서 삭제됩니다. 채팅방에서 나가시겠습니까?");
        startActivityForResult(intent, 1);
    }

    void sendGcmAndroid() {

        Gson gson = new Gson();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = destinationUserPushToken;
        notificationModel.data.title = userName;
        notificationModel.data.text = message;
        notificationModel.data.click_action = "m"+myImage;

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

        FirebaseDatabase.getInstance().getReference().child("users").child(destinatonUid).child("flagMessage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(Objects.equals(dataSnapshot.getValue(int.class), 0)){

                        Gson gson = new Gson();
                        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                        NotificationModel notificationModel = new NotificationModel();
                        notificationModel.to = destinationUserPushToken;
                        notificationModel.notification.title = userName;
                        notificationModel.notification.text = message;
                        notificationModel.notification.click_action = "m";
                        notificationModel.data.title = userName;
                        notificationModel.data.text = editText.getText().toString();
                        notificationModel.data.click_action = "m";

                        Log.d("namgung","푸시보냅니다. "+destinationUserPushToken);
                        Log.d("namgung","푸시보냅니다. "+userName);
                        Log.d("namgung","푸시보냅니다. "+message);


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

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatModel.Comment> comments;
        public RecyclerViewAdapter() {

            comments = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("users").child(destinatonUid).child("pushToken").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    destinationUserPushToken = dataSnapshot.getValue(String.class);
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            def = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users");
            valueEvent = def.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        if(destinatonUid.equals(item.getKey()) && item.getValue().toString().equals("false")) {
                            button.setVisibility(View.INVISIBLE);
                            editText.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        void getMessageList() {

            final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    comments.clear();

                    Map<String, Object> readUsersMap = new HashMap<>();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_motify = item.getValue(ChatModel.Comment.class);
                        assert comment_motify != null;
                        comment_motify.readUsers = uid;

                        if (!comment_motify.uid.equals(currentUser)) {
                            comment_motify.readUsers = uid;
                            readUsersMap.put(key, comment_motify);
                        }// 내 메세지가 아닐때

                        comments.add(comment_origin);
                    }


                    if (comments.size() == 0) {
                        return;
                    }

                    if (!comments.get(comments.size() - 1).uid.equals(currentUser)) {
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments")
                                .updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                notifyDataSetChanged();
                                recyclerView.scrollToPosition(comments.size() - 1);
                            }
                        });
                    } else {
                        notifyDataSetChanged();
                        recyclerView.scrollToPosition(comments.size() - 1);
                    }

                    //메세지가 갱신
                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {
                }
            });

            defout = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").child(destinatonUid);
            valueEventout = defout.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        if(!dataSnapshot.getValue(Boolean.class)){
                            checkDestinationUser = 1; // 상대방이 나가서 데이터베이스 호출
                        }else{
                            checkDestinationUser = 0; // 상대방이 들어와서 데이터베이스 호출
                        }
                        notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            }); //상대방이 나갔는지 확인


        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new MessageViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            final MessageViewHolder messageViewHolder = ((MessageViewHolder) holder);

            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            String hourTime = time.substring(11, 13);
            String minTime = time.substring(14);
            int hourTimeInt = Integer.parseInt(hourTime);



            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ViewGroup.LayoutParams paramsOutAndDate = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            setDateTextview(position, messageViewHolder.textView_date, messageViewHolder.linearLayout_date);


            if(position == 0)
                messageViewHolder.linearLayout_main.setVisibility(View.GONE);
            else
                messageViewHolder.linearLayout_main.setVisibility(View.VISIBLE);

            // 채팅방을 나갔는지 대화방 입장할때마다 확인되며 확인 됬으면 마지막 메세지에만 보이게 만들어아함
            if(checkDestinationUser==1) {
                if(position == comments.size()-1) {
                    messageViewHolder.linearLayout_out.setLayoutParams(new LinearLayout.LayoutParams(paramsOutAndDate));
                    messageViewHolder.textView_out.setVisibility(View.VISIBLE);
                    messageViewHolder.textView_out.setText("" + destinationName + "님이 채팅방을 나가셨습니다.");
                }else{
                    messageViewHolder.linearLayout_out.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                    messageViewHolder.textView_out.setVisibility(View.INVISIBLE);
                }
            }else{

                messageViewHolder.textView_out.setVisibility(View.INVISIBLE);
                messageViewHolder.linearLayout_out.setLayoutParams(new LinearLayout.LayoutParams(0,0));
            }

            intervalTime = sameTimeIntervalCheckTime(position);
            //내가보낸 메세지
            if (comments.get(position).uid.equals(uid)) {

                if(intervalTime == 1) {
                    messageViewHolder.textView_right_timestamp.setVisibility(View.VISIBLE);
                    if (hourTimeInt < 12)
                        messageViewHolder.textView_right_timestamp.setText("오전 " + hourTimeInt + ":" + minTime);
                    else if (hourTimeInt == 12)
                        messageViewHolder.textView_right_timestamp.setText("오후 " + hourTimeInt + ":" + minTime);
                    else
                        messageViewHolder.textView_right_timestamp.setText("오후 " + (hourTimeInt-12) + ":" + minTime);
                }else{
                    messageViewHolder.textView_right_timestamp.setVisibility(View.GONE);
                }

                messageViewHolder.textView_right_message.setText(comments.get(position).message);
                messageViewHolder.linearLayout_left_destination.setVisibility(View.GONE);
                messageViewHolder.linearLayout_right_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_right_message.setTextSize(15);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                setReadCounter(position, messageViewHolder.textView_readCounter_right);
                //상대방이 보낸 메세지
            } else {

                messageViewHolder.imageView_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        messageViewHolder.imageView_profile.setEnabled(false);
                        final Intent intent = new Intent(MessageActivity.this, ProposeProfileActivity.class);
                        intent.putExtra("destinationUid", destinatonUid);
                        intent.putExtra("where", "m");
                        startActivity(intent);
                        messageViewHolder.imageView_profile.setEnabled(true);

                        blockdatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("blockList");
                        blockvalueEventListener = blockdatabaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    if(item.getKey().equals(destinatonUid)){
                                        if(valueEventListener!=null)
                                            databaseReference.removeEventListener(valueEventListener);

                                        if(valueEvent != null)
                                            def.removeEventListener(valueEvent);

                                        if(valueEventout != null)
                                            defout.removeEventListener(valueEventout);

                                        if(blockvalueEventListener != null)
                                            blockdatabaseReference.removeEventListener(blockvalueEventListener);

                                        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+destinatonUid).equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot item: dataSnapshot.getChildren()) {
                                                    item.getRef().removeValue();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        }); // 로그인된 uid가 채팅방을 나갔는데 상대방도 나간 상태라면 전체 삭제


                                        finish();
                                        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.remove(destinationName);
                                        editor.apply();
                                        overridePendingTransition(R.anim.fromleft, R.anim.toright);
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
                if(intervalTime == 1) {
                    messageViewHolder.textView_left_timestamp.setVisibility(View.VISIBLE);
                    if (hourTimeInt < 12)
                        messageViewHolder.textView_left_timestamp.setText("오전 " + hourTimeInt + ":" + minTime);
                    else if (hourTimeInt == 12)
                        messageViewHolder.textView_left_timestamp.setText("오후 " + hourTimeInt + ":" + minTime);
                    else
                        messageViewHolder.textView_left_timestamp.setText("오후 " + (hourTimeInt - 12) + ":" + minTime);
                }else{
                    messageViewHolder.textView_left_timestamp.setVisibility(View.INVISIBLE);
                }

                interval = sameTimeIntervalCheck(position);
                if(interval == 1) {
                    messageViewHolder.textview_name.setVisibility(View.VISIBLE);
                    messageViewHolder.imageView_profile.setVisibility(View.VISIBLE);
                    messageViewHolder.imageView_profile.setLayoutParams(new LinearLayout.LayoutParams(135,135));

                    if(!destinationImage.equals("")){
                        Glide.with(holder.itemView.getContext())
                                .load(destinationImage)
                                .apply(new RequestOptions().centerCrop())
                                .into(messageViewHolder.imageView_profile);
                    }else{
                        messageViewHolder.imageView_profile.setImageResource(R.drawable.usernonimage); // 원본이 존재하지 않으면 기본이미지
                    }

                    messageViewHolder.textview_name.setText(destinationName);
                }else{
                    if(position == 1){
                        messageViewHolder.imageView_profile.setVisibility(View.VISIBLE);
                        messageViewHolder.textview_name.setVisibility(View.VISIBLE);
                    }else{
                        messageViewHolder.textview_name.setVisibility(View.GONE);
                        messageViewHolder.imageView_profile.setVisibility(View.INVISIBLE);
                        messageViewHolder.imageView_profile.setLayoutParams(new LinearLayout.LayoutParams( 135,0));
                    }

                }

                messageViewHolder.linearLayout_right_destination.setVisibility(View.GONE);
                messageViewHolder.linearLayout_left_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_left_message.setText(comments.get(position).message);
                messageViewHolder.textView_left_message.setTextSize(15);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
                setReadCounter(position, messageViewHolder.textView_readCounter_left);

            }


        }

        void setDateTextview(final int position, final TextView textView, final LinearLayout linearLayout){

            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            String dateTime = time.substring(0, 10);

            ViewGroup.LayoutParams paramsOutAndDate = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if(position < 1) {
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(paramsOutAndDate));
                textView.setText( simpleDateFormatDate.format(date));
            }else{
                long prevunixTime = (long) comments.get(position-1).timestamp;
                Date prevdate = new Date(prevunixTime);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String prevtime = simpleDateFormat.format(prevdate);
                String prevdateTime = prevtime.substring(0, 10);

                if(!dateTime.equals(prevdateTime))
                {
                    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(paramsOutAndDate));
                    textView.setText( simpleDateFormatDate.format(date));
                }else{
                    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                }
            }
        }

        int sameTimeIntervalCheck(final int position) {
            // 현재 메세지와 바로 위 메세지를 비교하는 메소드
            if(position == 0)
                return 0;

            if(position == 1) {
                return 1;// 현재 메세지가 맨위에 메세지이면 이름, 사진 모두 찍는다.
            }else{
                long unixTime = (long) comments.get(position).timestamp;
                Date date = new Date(unixTime);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String time = simpleDateFormat.format(date);

                long prevunixTime = (long) comments.get((position - 1)).timestamp;
                Date prevdate = new Date(prevunixTime);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String prevtime = simpleDateFormat.format(prevdate);

                if(comments.get(position).uid.equals(comments.get(position-1).uid) && time.equals(prevtime))
                    return 0; // 바로 위에 메세지가 내 메세지이고 시간대가 같으면 사진, 이름을 찍지 않는다.
                else
                    return 1;  // 바로 위에 메세지가 내 메세지이고 시간대가 다르면 사진, 이름을 찍는다.
                // 바로 위에 메세지가 내 메세지가 아닐 경우 사진 이름을 찍는다.
            }

        }

        int sameTimeIntervalCheckTime(final int position) {

            // 현재 메세지와 바로 위 메세지를 비교하는 메소드

            // 현재 메세지가 맨위에 있는 메세지이면 아래 메세지가 있는지 확인한다.
            // 현재 메세지보다 아래 메세지가 존재하면 아래 메세지가 내 메세지 인지 확인한다.
            // 아래 메세지가 내 메세지이면서 시간대가 같으면 스탬프를 찍지 않는다.
            // 아래 메세지가 내 메세지가 아니면 스탬프를 찍는다.
            // 아래 메세지와 시간대가 다르면 스탬프를 찍는다.
            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);

            if(comments.size() < 2)
            {
                return 1; // 메세지가 하나 밖에 없을땐 스탬프를 찍는다.

            }else {

                if (position == comments.size() - 1) {
                    return 1;
                } // 마지막 메세지 이면 스탬프를 찍는다.
                else {
                    long prevunixTime = (long) comments.get((position + 1)).timestamp;
                    Date prevdate = new Date(prevunixTime);
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                    String prevtime = simpleDateFormat.format(prevdate);

                    if (comments.get(position).uid.equals(comments.get(position + 1).uid) && time.equals(prevtime))
                        return 0; // 바로 아래 메세지가 내 메세지이고 시간대가 같으면 스탬프를 찍지 않는다.
                    else
                        return 1;  // 바로 아래 메세지가 내 메세지이고 시간대가 다르면 스탬프를 찍는다.
                    // 바로 아래 메세지가 내 메세지가 아닐 경우 사진 스탬프를 찍는다.
                }
            }

        }

        void setReadCounter(final int position, final TextView textView) {

            if (peopleCount == 0) {
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount = users.size();

                        if(comments.get(position).readUsers == null)
                            readCounter = 0;
                        else
                            readCounter = 1;

                        int count = 1 - readCounter;
                        if (count > 0)
                            textView.setVisibility(View.VISIBLE);
                        else
                            textView.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {

                    }
                });
            }else{
                if(comments.get(position).readUsers == null)
                    readCounter = 0;
                else
                    readCounter = 1;

                int count = 1 - readCounter;
                if (count > 0)
                    textView.setVisibility(View.VISIBLE);
                else
                    textView.setVisibility(View.INVISIBLE);

            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {

            public TextView textView_left_message;
            public TextView textView_right_message;
            public TextView textview_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_left_destination;
            public LinearLayout linearLayout_right_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_left_timestamp;
            public TextView textView_right_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;
            public LinearLayout linearLayout_date;
            public LinearLayout linearLayout_out;
            public TextView textView_date;
            public TextView textView_out;

            public MessageViewHolder(View view) {
                super(view);

                textView_left_message = view.findViewById(R.id.messageItem_textView_left_message);
                textView_right_message =  view.findViewById(R.id.messageItem_textView_right_message);
                textview_name =  view.findViewById(R.id.messageItem_textview_name);
                imageView_profile =  view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_left_destination = view.findViewById(R.id.messageItem_linearlayout_left_destination);
                linearLayout_right_destination =  view.findViewById(R.id.messageItem_linearlayout_right_destination);
                linearLayout_main = view.findViewById(R.id.messageItem_linearlayout_main);
                textView_left_timestamp = view.findViewById(R.id.messageItem_textview_left_timestamp);
                textView_right_timestamp = view.findViewById(R.id.messageItem_textview_right_timestamp);
                textView_readCounter_left =view.findViewById(R.id.messageItem_textview_readCounter_left);
                textView_readCounter_right =view.findViewById(R.id.messageItem_textview_readCounter_right);

                linearLayout_date = view.findViewById(R.id.messageItem_linearlayout_date);
                linearLayout_out = view.findViewById(R.id.messageItem_linearlayout_out);
                textView_date =  view.findViewById(R.id.messageItem_date);
                textView_out = view.findViewById(R.id.messageItem_out);

            }
        }
    }

    @Override
    public void onBackPressed() {
        if(valueEventListener!=null)
            databaseReference.removeEventListener(valueEventListener);

        if(valueEvent != null)
            def.removeEventListener(valueEvent);

        if(valueEventout != null)
            defout.removeEventListener(valueEventout);

        if(blockvalueEventListener != null)
            blockdatabaseReference.removeEventListener(blockvalueEventListener);


        finish();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(destinationName);
        editor.apply();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(valueEventListener!=null)
            databaseReference.removeEventListener(valueEventListener);

        if(valueEvent != null)
            def.removeEventListener(valueEvent);

        if(valueEventout != null)
            defout.removeEventListener(valueEventout);

        if(blockvalueEventListener != null)
            blockdatabaseReference.removeEventListener(blockvalueEventListener);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(destinationName);
        editor.apply();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(valueEventListener!=null)
            databaseReference.removeEventListener(valueEventListener);

        if(valueEvent != null)
            def.removeEventListener(valueEvent);

        if(valueEventout != null)
            defout.removeEventListener(valueEventout);

        if(blockvalueEventListener != null)
            blockdatabaseReference.removeEventListener(blockvalueEventListener);

        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(destinationName, 0);
        editor.apply();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(destinationName, 1);
        editor.apply();

        finish();
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("destinationUid",destinatonUid);
        intent.putExtra("chatroomId",chatRoomUid);
        intent.putExtra("destinationName",destinationName);
        intent.putExtra("destinationImage",destinationImage);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(destinationName, 1);
        editor.apply();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:

                if(valueEventListener!=null)
                    databaseReference.removeEventListener(valueEventListener);

                if(valueEvent != null)
                    def.removeEventListener(valueEvent);

                if(valueEventout != null)
                    defout.removeEventListener(valueEventout);

                if(blockvalueEventListener != null)
                    blockdatabaseReference.removeEventListener(blockvalueEventListener);

                finish();
                SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
                SharedPreferences.Editor editor = settings.edit();
                editor.remove(destinationName);
                editor.apply();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                //데이터 받기
                String result = data.getStringExtra("result");
                if(result.equals("Yes")) {
                    Map<String, Object> UsersMap = new HashMap<>();
                    UsersMap.put(uid,false);
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users")
                            .updateChildren(UsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+destinatonUid).equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                                        item.getRef().removeValue();
                                        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.remove(destinationName);
                                        editor.apply();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            }); // 로그인된 uid가 채팅방을 나갔는데 상대방도 나간 상태라면 전체 삭제
                        }
                    });

                    if(valueEventListener!=null)
                        databaseReference.removeEventListener(valueEventListener);

                    if(valueEvent != null)
                        def.removeEventListener(valueEvent);

                    if(valueEventout != null)
                        defout.removeEventListener(valueEventout);

                    if(blockvalueEventListener != null)
                        blockdatabaseReference.removeEventListener(blockvalueEventListener);

                    finish();
                    overridePendingTransition(R.anim.fromleft, R.anim.toright);
                }// 채팅방 나가기 희망합니다

            }
        }
    }
}