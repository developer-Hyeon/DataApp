package sinabro.today.block;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import sinabro.today.R;

public class BlockActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private  Toolbar toolbar;
    private int index, flag;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        recyclerView = findViewById(R.id.block_recyclerview);
        toolbar = findViewById(R.id.toolbar_block);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView.setAdapter(new RecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(BlockActivity.this));
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<String> destinationImages;
        String myUid;
        List<String> destinationUids;
        List<String> destinationName;

        public RecyclerViewAdapter(){
            destinationImages = new ArrayList<>();
            destinationUids = new ArrayList<>();
            destinationName = new ArrayList<>();
            myUid = FirebaseAuth.getInstance().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(myUid).child("blockList");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    destinationUids.clear();
                    destinationImages.clear();
                    destinationName.clear();
                    index = 0;
                    for (final DataSnapshot item :dataSnapshot.getChildren()) {
                        destinationUids.add(index, item.getKey()); // 블랙리스트 상대방 uid 받아옴

                        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUids.get(index)).child("photo").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                flag = 0;
                                Log.d("namgung","포토 필드 없음 "+dataSnapshot.toString());
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    if(item.child("image").exists()){
                                        destinationImages.add(item.child("image").getValue(String.class));
                                        flag = 1;
                                        break;
                                    }
                                }
                                if(flag == 0)
                                    destinationImages.add("");

                                notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        }); // 상대방 사진 받아오기

                        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUids.get(index)).child("userName").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                destinationName.add(dataSnapshot.getValue(String.class));
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        }); // 상대방 이름 받아오기
                        index++;

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_block, parent, false);
            return new BlockViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            final BlockViewHolder blockViewHolder = ((BlockViewHolder) holder);

            if(destinationName.size() > position)
                blockViewHolder.textView.setText(destinationName.get(position));

            if(destinationImages.size() > position){
                if(!destinationImages.get(position).equals("")){
                    Glide.with(holder.itemView.getContext())
                            .load(destinationImages.get(position))
                            .apply(new RequestOptions().centerCrop())
                            .into(blockViewHolder.imageView);
                }else
                    blockViewHolder.imageView.setImageResource(R.drawable.usernonimage);
            }

            blockViewHolder.blockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(((BlockViewHolder) holder).imageView.getContext());
                    alert_confirm.setMessage("차단을 해제하시겠습니까?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseDatabase.getInstance().getReference().child("users").child(myUid).child("blockList").child(destinationUids.get(position)).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("users").child(destinationUids.get(position)).child("blockedList").child(myUid).removeValue();
                                }
                            }
                    );
                    alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alert_confirm.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return destinationUids.size();
        }

        private class BlockViewHolder extends RecyclerView.ViewHolder {

           public ImageView imageView;
           public TextView  textView;
           public Button    blockButton;

            public BlockViewHolder(View view) {
                super(view);

                imageView = view.findViewById(R.id.block_photoUrl);
                textView = view.findViewById(R.id.block_name);
                blockButton = view.findViewById(R.id.block_btn);

            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (valueEventListener != null)
            databaseReference.removeEventListener(valueEventListener);


        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(valueEventListener!=null)
                    databaseReference.removeEventListener(valueEventListener);

                finish();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}