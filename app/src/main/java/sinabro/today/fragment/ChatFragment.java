package sinabro.today.fragment;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;
import sinabro.today.R;
import sinabro.today.chat.MessageActivity;
import sinabro.today.model.ChatModel;

public class ChatFragment extends Fragment {

    private Query databaseReference;
    private ValueEventListener valueEventListener;
    private TextView textView1;
    private TextView textView2;
    private RecyclerView recyclerView;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss a");
    private int photoflag;
    private int badgeCount=0;
    private int photoindex=0, nameindex=0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,container,false);

        Log.d("namgungfragment","ChatFragment 실행됨");
        textView1 = view.findViewById(R.id.chat_text1);
        textView2 = view.findViewById(R.id.chat_text2);
        recyclerView  = view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));


        return view;
    }


    class ChatRecyclerViewAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels = new ArrayList<>();
        private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<>();
        private List<String> chatroomIds = new ArrayList<>();
        private List<String> lastMessageKeys = new ArrayList<>();
        private List<String> destinationUserNames = new ArrayList<>();
        private List<String> destinationUserImages = new ArrayList<>();
        public ChatRecyclerViewAdapter() {
            getChatroomList();
        }
        public void getChatroomList() {

            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + uid).equalTo(true);
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    lastMessageKeys.clear();
                    chatroomIds.clear();
                    destinationUserImages.clear();
                    destinationUserNames.clear();
                    destinationUsers.clear();
                    badgeCount = 0;
                    photoindex =0;
                    nameindex=0;

                    int i = 0;
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        chatModels.add(item.getValue(ChatModel.class));
                        chatroomIds.add(item.getKey());
                        if (item.child("comments").exists()) {
                            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
                            commentMap.putAll(chatModels.get(i).comments);
                            lastMessageKeys.add((String) commentMap.keySet().toArray()[0]);
                        }
                        i++;
                    }


                    //정렬
                    ChatModel copyModel;
                    String  tmpmessage;
                    String  roomid;
                    int size = chatModels.size();
                    for (int m = 0; m < size - 1; m++) {
                        for (int k = (m + 1); k < size; k++) {

                            if(Objects.requireNonNull(chatModels.get(m).comments.get(lastMessageKeys.get(m))).timestamp.toString()
                                    .compareTo(Objects.requireNonNull(chatModels.get(k).comments.get(lastMessageKeys.get(k))).timestamp.toString()) < 0){
                                copyModel = chatModels.get(m);
                                chatModels.add(m, chatModels.get(k));
                                chatModels.remove(m+1);
                                chatModels.add(k, copyModel);
                                chatModels.remove(k+1);
                                tmpmessage = lastMessageKeys.get(m);
                                lastMessageKeys.add(m, lastMessageKeys.get(k));
                                lastMessageKeys.remove(m+1);
                                lastMessageKeys.add(k, tmpmessage);
                                lastMessageKeys.remove(k+1);
                                roomid = chatroomIds.get(m);
                                chatroomIds.add(m, chatroomIds.get(k));
                                chatroomIds.remove(m+1);
                                chatroomIds.add(k, roomid);
                                chatroomIds.remove(k+1);

                            }
                        }
                    }


                    if(chatModels.size() == 0){
                        textView1.setVisibility(View.VISIBLE);
                        textView2.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }else{
                        textView1.setVisibility(View.GONE);
                        textView2.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);
            return new CustomViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final CustomViewHolder customViewHolder = (CustomViewHolder)holder;

            if(position >= chatModels.size()){
                return;
            } // 예외처리

            String destinationUid = null;
            // 일일 챗방에 있는 유저를 체크
            for(String user: chatModels.get(position).users.keySet()){
                if(!user.equals(uid)){
                    destinationUid = user;
                    destinationUsers.add(position,destinationUid);
                    destinationUserImages.add(position,"");
                    destinationUserNames.add(position,"");

                }
            }

            if(destinationUserNames.size() == destinationUsers.size()){
                FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(position < destinationUserNames.size()){
                            String name = dataSnapshot.getValue(String.class);
                            customViewHolder.textView_title.setText(name);
                            destinationUserNames.remove(position);
                            destinationUserNames.add(position, name);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            if(destinationUserImages.size() == destinationUsers.size()){
                assert destinationUid != null;
                FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        photoflag = 0;
                        if(!dataSnapshot.exists()){
                            if(position < destinationUserImages.size()){
                                destinationUserImages.remove(position);
                                destinationUserImages.add(position, "");// 원본 이미지가 존재하지 않은 유저이다.
                                customViewHolder.imageView.setImageResource(R.drawable.usernonimage);
                            }
                        }else{
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                if(item.child("image").exists()){
                                    photoflag = 1;
                                    if(position < destinationUserImages.size()){
                                        String imageUrl = item.child("image").getValue(String.class);

                                        Glide.with(customViewHolder.itemView.getContext())
                                                .load(imageUrl)
                                                .apply(new RequestOptions().centerCrop())
                                                .into(customViewHolder.imageView);
                                        destinationUserImages.remove(position);
                                        destinationUserImages.add(position, imageUrl);
                                        // photoindex++;
                                        break;
                                    }

                                }
                            }

                            if(photoflag == 0) {
                                if(position < destinationUserImages.size()){
                                    destinationUserImages.remove(position);
                                    destinationUserImages.add(position, "");// 원본 이미지가 존재하지 않은 유저이다.
                                    customViewHolder.imageView.setImageResource(R.drawable.usernonimage);

                                }
                            }
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            //메시지를 내림 차순으로 정렬 후 마지막 메세지의 키값을 가져옴
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);
            int nuReadCounter = 0;
            if (commentMap.keySet().toArray().length > 0) {

                for (int i = 0; i < commentMap.keySet().toArray().length; i++) {
                    if (!Objects.requireNonNull(chatModels.get(position).comments.get(commentMap.keySet().toArray()[i])).message.equals("축하드립니다! 채팅방이 개설되었습니다."))
                        if (Objects.requireNonNull(chatModels.get(position).comments.get(commentMap.keySet().toArray()[i])).readUsers == null && !Objects.requireNonNull(chatModels.get(position).comments.get(commentMap.keySet().toArray()[i])).uid.equals(uid)) {
                            nuReadCounter++;
                        }
                }
                badgeCount += nuReadCounter;
                if(position == chatModels.size()-1){
                    SharedPreferences settings = Objects.requireNonNull(getContext()).getSharedPreferences("sinabro",0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("badgeMessage", badgeCount);
                    editor.apply(); // 뱃지 쉐어드 생성
                }


                if (nuReadCounter == 0)
                    customViewHolder.textView_counter.setVisibility(View.INVISIBLE);
                else if (nuReadCounter > 100) {
                    customViewHolder.textView_counter.setText("" + nuReadCounter + "+");
                    customViewHolder.textView_counter.setVisibility(View.VISIBLE);
                } else {
                    customViewHolder.textView_counter.setText("" + nuReadCounter);
                    customViewHolder.textView_counter.setVisibility(View.VISIBLE);
                }

                String lastMessageKey = (String) commentMap.keySet().toArray()[0];

                if(Objects.requireNonNull(chatModels.get(position).comments.get(lastMessageKey)).message.length() >18){
                    String message = Objects.requireNonNull(chatModels.get(position).comments.get(lastMessageKey)).message.substring(0, 16)+"...";
                    customViewHolder.textView_last_message.setText(message);
                }
                else
                    customViewHolder.textView_last_message.setText(Objects.requireNonNull(chatModels.get(position).comments.get(lastMessageKey)).message);

                long nowTime = System.currentTimeMillis();
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                Date nowDate = new Date(nowTime);
                String getTime = simpleDateFormat.format(nowDate);

                String nowYear = getTime.substring(0, 4);
                String nowMonth = getTime.substring(5, 7);
                String nowDay = getTime.substring(8, 10);
                String nowHour = getTime.substring(11, 13);
                String nowAMPM = getTime.substring(20, 22);
                int nowDayInt = Integer.parseInt(nowDay);

                long unixTime = (long) Objects.requireNonNull(chatModels.get(position).comments.get(lastMessageKey)).timestamp;
                Date date = new Date(unixTime);
                String lastTime = simpleDateFormat.format(date);
                String lastYear = lastTime.substring(0, 4);
                String lastMonth = lastTime.substring(5, 7);
                String lastDay = lastTime.substring(8, 10);
                String lastHour = lastTime.substring(11, 13);
                String lastAMPM = lastTime.substring(20, 22);
                String lastMin = lastTime.substring(14, 16);
                int hourInt = Integer.parseInt(lastHour);
                int minInt = Integer.parseInt(lastMin);
                int dayInt = Integer.parseInt(lastDay);
                int monthInt = Integer.parseInt(lastMonth);

                if (nowYear.equals(lastYear)) {
                    if (nowMonth.equals(lastMonth) && nowDay.equals(lastDay)) {
                        if (lastAMPM.equals("AM"))
                            if(minInt < 10)
                                customViewHolder.textView_timestamp.setText("오전 " + hourInt + ":0" + minInt);
                            else
                                customViewHolder.textView_timestamp.setText("오전 " + hourInt + ":" + minInt);
                        else
                        if(minInt < 10)
                            customViewHolder.textView_timestamp.setText("오후 " + hourInt + ":0" + minInt);
                        else
                            customViewHolder.textView_timestamp.setText("오후 " + hourInt + ":" + minInt);

                    } else if (nowMonth.equals(lastMonth) && (dayInt + 1 == nowDayInt))
                        customViewHolder.textView_timestamp.setText("어제");
                    else
                        customViewHolder.textView_timestamp.setText(monthInt + "월 " + dayInt + "일");
                } else {
                    customViewHolder.textView_timestamp.setText(lastTime.substring(0, 10));
                }
            }

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = null;
                    if (chatModels.get(position).users.size() > 2) {
                        return;
                    } else {
                        intent = new Intent(view.getContext(), MessageActivity.class);
                        intent.putExtra("destinationUid", destinationUsers.get(position));
                        intent.putExtra("chatroomId", chatroomIds.get(position));
                        intent.putExtra("destinationName", destinationUserNames.get(position));
                        intent.putExtra("destinationImage", destinationUserImages.get(position));
                    }

                    ActivityOptions activityOptions = null;
                    activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());

                }

            });

        }
        @Override
        public int getItemCount() {

            return chatModels.size();
        }
        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView_title;
            public TextView textView_last_message;
            public TextView textView_timestamp;
            public TextView textView_counter;

            public CustomViewHolder(View view) {
                super(view);

                imageView = view.findViewById(R.id.chatitem_imageview);
                textView_title = view.findViewById(R.id.chatitem_textview_title);
                textView_last_message = view.findViewById(R.id.chatitem_textview_lastMessage);
                textView_timestamp = view.findViewById(R.id.chatitem_textview_timestamp);
                textView_counter = view.findViewById(R.id.chatitem_textview_counter);
            }
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
    }

}