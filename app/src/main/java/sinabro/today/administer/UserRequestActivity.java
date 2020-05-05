package sinabro.today.administer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import java.util.ArrayList;
import java.util.List;
import sinabro.today.R;
import sinabro.today.model.UserModel;

public class UserRequestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private String requestUid;
    private Button agree;
    private Button disagree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request);

        requestUid = getIntent().getStringExtra("requestUid");
        recyclerView = findViewById(R.id.administer_reclclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(UserRequestActivity.this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(new RecyclerViewAdapter());
    }


    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<String>  requestId;
        List<UserModel.Photo> photos;
        public RecyclerViewAdapter() {
            photos = new ArrayList<>();
            requestId = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("users").child(requestUid).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    photos.clear();
                    requestId.clear();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        if(item.getValue(UserModel.Photo.class).tempHashCode != null) {
                            photos.add(item.getValue(UserModel.Photo.class));
                            requestId.add(item.getKey());
                        }
                    }
                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
           RequestViewHolder requestViewHolder = ((RequestViewHolder) holder);

           if(photos.size() >0) {
               Glide.with(holder.itemView.getContext())
                       .load(photos.get(position).temp)
                       .apply(new RequestOptions().centerCrop())
                       .into(requestViewHolder.imageView);

               agree = findViewById(R.id.agree);
               agree.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       if (photos.get(position).imageHashCode != null) { // 이미지 원본이 존재할경우 스토리지에서 지워준다.
                           Log.d("namgung","이미지 원본이 존재하므로 삭제해준다.");
                           FirebaseStorage.getInstance().getReference().child("userImages").child(requestUid).child(photos.get(position).imageHashCode).delete();
                       }else{
                           Log.d("namgung","이미지 원본이 존재하지 않다.");
                       }

                       UserModel.Photo requestModel = new UserModel.Photo();
                       requestModel.image = photos.get(position).temp;
                       requestModel.imageHashCode = photos.get(position).tempHashCode;
                       FirebaseDatabase.getInstance().getReference().child("users").child(requestUid).child("photo").child(requestId.get(position)).setValue(requestModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               requestId.remove(position); // 지워주기
                               photos.remove(position);
                               Log.d("namgung", "수락 버튼 클릭시 이미지로 승격시키고 포토 정보 지우기");
                               if(photos.size() < 1) {
                                   FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(requestUid).removeValue();
                                   Intent intent = new Intent(UserRequestActivity.this, AdministerActivity.class);
                                   startActivity(intent);
                                   finish();
                                   overridePendingTransition(R.anim.fromleft, R.anim.toright);
                               }

                               notifyDataSetChanged();// 갱신
                           }
                       });

                   }

               }); // 수락 버튼 클릭

               disagree = findViewById(R.id.disagree);
               disagree.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       final String request = requestId.get(position);
                       FirebaseStorage.getInstance().getReference().child("userImages").child(requestUid).child(photos.get(position).tempHashCode).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if (photos.get(position).imageHashCode == null) { // 원본이 존재하지 않는 경우 키 자체를 지워버린다.
                                   FirebaseDatabase.getInstance().getReference().child("users").child(requestUid).child("photo").child(request).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {

                                           photos.remove(position);
                                           requestId.remove(position); // 지워주기
                                           if(photos.size() < 1) {
                                               FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(requestUid).removeValue();
                                               Intent intent = new Intent(UserRequestActivity.this, AdministerActivity.class);
                                               startActivity(intent);
                                               finish();
                                               overridePendingTransition(R.anim.fromleft, R.anim.toright);
                                           }
                                           notifyDataSetChanged();// 갱신
                                       }
                                   });
                               } else {// 원본이 존재하면 템프와 템프해쉬코드만 지워준다.
                                   FirebaseDatabase.getInstance().getReference().child("users").child(requestUid).child("photo").child(request).child("temp").removeValue();
                                   FirebaseDatabase.getInstance().getReference().child("users").child(requestUid).child("photo").child(request).child("tempHashCode").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           photos.remove(position);
                                           requestId.remove(position); // 지워주기
                                           if(photos.size() < 1) {
                                               FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(requestUid).removeValue();
                                               Intent intent = new Intent(UserRequestActivity.this, AdministerActivity.class);
                                               startActivity(intent);
                                               finish();
                                               overridePendingTransition(R.anim.fromleft, R.anim.toright);
                                           }
                                           notifyDataSetChanged();// 갱신
                                       }
                                   });
                               }
                           }
                       }); // 스토리지 지우기

                   }
               }); // 거절 버튼
           } // 포토 크기가 0보다 클 경우 로직 동작
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        private class RequestViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public RequestViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.request_imageview);
            }
        }
    } // recyclerview


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
