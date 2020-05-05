package sinabro.today.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;

import sinabro.today.PictureZoom;
import sinabro.today.R;
import sinabro.today.model.UserModel;


public class ProfileActivity extends AppCompatActivity {
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss a");
    private RecyclerView recyclerView;
    private Button backButton;
    private LinearLayout linearLayout_editButton;
    private TextView myprofile_name;
    private TextView myprofile_age;
    private TextView myprofile_info;
    private TextView myprofile_comment;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private String uid;
    private UserModel userModel = new UserModel();
    private List<String> keySet;
    Map<String, UserModel.Photo> photoMap = new TreeMap<>(Collections.reverseOrder());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        myprofile_name = findViewById(R.id.myprofile_name);
        myprofile_age = findViewById(R.id.myprofile_age);
        myprofile_info = findViewById(R.id.myprofile_info);
        myprofile_comment = findViewById(R.id.myprofile_comment);
        backButton = findViewById(R.id.myprofile_backbutton);
        linearLayout_editButton = findViewById(R.id.myprofile_linear_editButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
               // startActivity(intent);

                if(valueEventListener!=null)
                    databaseReference.removeEventListener(valueEventListener);

                ProfileActivity.this.finish();
                //overridePendingTransition(R.anim.fromleft, R.anim.toright);
                overridePendingTransition(R.anim.fromright, R.anim.toleft);
            }
        });

        linearLayout_editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        ProfileActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View alertView = inflater.inflate(R.layout.profilesetting_dialog, null);
                alertDialogBuilder.setView(alertView);
                // 다이얼로그 생성
                final AlertDialog alertDialog = alertDialogBuilder.create();
                final TextView textView_no = alertView.findViewById(R.id.settingNo);
                textView_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();
                    }
                });
                final TextView textView_yes = alertView.findViewById(R.id.settingYes);
                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ProfileActivity.this, ProfileSettingActivity.class);
                        intent.putExtra("profileName", userModel.userName);
                        intent.putExtra("profileAge", userModel.age);
                        intent.putExtra("profileSex", userModel.sex);
                        intent.putExtra("profileBody", userModel.body);
                        intent.putExtra("profilePersonality", userModel.personality);
                        intent.putExtra("profileTall", userModel.tall);
                        if(userModel.comment != null)
                            intent.putExtra("profileComment", userModel.comment);

                        intent.putExtra("profilePhotos", (Serializable) userModel.photo);
                        intent.putExtra("profilePhotosKey", (Serializable) keySet);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fromright, R.anim.toleft);
                        alertDialog.dismiss();
                    }
                });

                // 다이얼로그 보여주기
                alertDialog.show();
            }
        });


        recyclerView = findViewById(R.id.myprofile_reclclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProfileActivity.this,LinearLayoutManager.HORIZONTAL,false));
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(new RecyclerViewAdapter());

    }



    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public RecyclerViewAdapter() {

            uid = FirebaseAuth.getInstance().getUid();
            keySet = new ArrayList<>();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    keySet.clear();
                    photoMap.clear();
                    userModel = dataSnapshot.getValue(UserModel.class);
                    assert userModel != null;
                    photoMap.putAll(userModel.photo);
                    for(int i=userModel.photo.keySet().size()-1; i>=0 ; i--){
                        keySet.add((String) photoMap.keySet().toArray()[i]);
                    }
                    myprofile_name.setText(userModel.userName);

                    long nowTime = System.currentTimeMillis();
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                    Date nowDate = new Date(nowTime);
                    String getTime = simpleDateFormat.format(nowDate);
                    int nowYear= Integer.parseInt(getTime.substring(0,4));

                    if( (nowYear- Integer.parseInt(userModel.age)+1) < 20 )
                        myprofile_age.setText("10대");
                    else if( (nowYear- Integer.parseInt(userModel.age)+1) < 24 )
                        myprofile_age.setText("20대 초반");
                    else if( (nowYear- Integer.parseInt(userModel.age)+1) < 27 )
                        myprofile_age.setText("20대 중반");
                    else if( (nowYear- Integer.parseInt(userModel.age)+1) < 30 )
                        myprofile_age.setText("20대 후반");
                    else if( (nowYear- Integer.parseInt(userModel.age)+1) < 34 )
                        myprofile_age.setText("30대 초반");
                    else if( (nowYear- Integer.parseInt(userModel.age)+1) < 37 )
                        myprofile_age.setText("30대 중반");
                    else if( (nowYear- Integer.parseInt(userModel.age)+1) < 40 )
                        myprofile_age.setText("30대 후반");
                    else
                        myprofile_age.setText("40대 이상");

                    myprofile_info.setText(userModel.tall+", "+userModel.body+" 체형과 "+userModel.personality+" 성격을 가진 "+userModel.sex+"입니다.");
                    if(userModel.comment != null) {
                        myprofile_comment.setVisibility(View.VISIBLE);
                        myprofile_comment.setText(userModel.comment);
                    }else{
                        myprofile_comment.setVisibility(View.INVISIBLE);
                    }

                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
            return new ProfileViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            ProfileViewHolder profileViewHolder = (ProfileViewHolder) holder;

            profileViewHolder.textView_administer.setVisibility(View.INVISIBLE);
            profileViewHolder.imageView.setImageResource(R.drawable.border1);
            if(userModel.photo.size() >1) {
                profileViewHolder.textView_count.setVisibility(View.VISIBLE);
                profileViewHolder.textView_count.setText(""+(position+1)+"/"+""+userModel.photo.size());
            }else{
                profileViewHolder.textView_count.setVisibility(View.INVISIBLE);
            }


            if(Objects.requireNonNull(userModel.photo.get(keySet.get(position))).temp != null)
            {
                profileViewHolder.textView_administer.setVisibility(View.VISIBLE);
                Glide.with(holder.itemView.getContext())
                        .load(Objects.requireNonNull(userModel.photo.get(keySet.get(position))).temp)
                        .apply(new RequestOptions().centerCrop())
                        .into(profileViewHolder.imageView);
            }else{
                if(Objects.requireNonNull(userModel.photo.get(keySet.get(position))).image != null) {
                    Glide.with(holder.itemView.getContext())
                            .load(Objects.requireNonNull(userModel.photo.get(keySet.get(position))).image)
                            .apply(new RequestOptions().centerCrop())
                            .into(profileViewHolder.imageView);
                }
            }

            profileViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Objects.requireNonNull(userModel.photo.get(keySet.get(position))).temp != null) {
                        Intent intent = new Intent(getApplicationContext(), PictureZoom.class);
                        intent.putExtra("photourl", Objects.requireNonNull(Objects.requireNonNull(userModel.photo.get(keySet.get(position))).temp));
                        startActivity(intent);
                    } else{
                        Intent intent = new Intent(getApplicationContext(), PictureZoom.class);
                        intent.putExtra("photourl", Objects.requireNonNull(userModel.photo.get(keySet.get(position))).image);
                        startActivity(intent);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return userModel.photo.size();
        }

        private class ProfileViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView  textView_administer;
            public TextView  textView_count;

            public ProfileViewHolder(View view) {
                super(view);
                textView_administer = view.findViewById(R.id.profile_item_administer);
                textView_count = view.findViewById(R.id.profile_item_count);
                imageView = view.findViewById(R.id.profile_item_imageview);
            }
        }

    } // recycler

    @Override
    public void onBackPressed() {
        if(valueEventListener!=null)
            databaseReference.removeEventListener(valueEventListener);
        finish();
        overridePendingTransition(R.anim.fromright, R.anim.toleft);
    }




}

