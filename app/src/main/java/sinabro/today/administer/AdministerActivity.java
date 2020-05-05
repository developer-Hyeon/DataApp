package sinabro.today.administer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import sinabro.today.R;
import sinabro.today.main.MainActivity;

public class AdministerActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Button ad_Button;
    private String administerUid;
    private Button re_Button;
    private TextView textView_count;
    private LinearLayout linearLayout;
    private Button love_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer);

        toolbar = findViewById(R.id.administer_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView_count = findViewById(R.id.count);
        linearLayout = findViewById(R.id.linearlayout_administer_all);

        recyclerView = findViewById(R.id.administer_reclclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(AdministerActivity.this));
        recyclerView.setAdapter(new RecyclerViewAdapter());

        FirebaseDatabase.getInstance().getReference().child("administer").child("sinabro").child("hyeon").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                administerUid = dataSnapshot.getValue(String.class);
                if(FirebaseAuth.getInstance().getUid().equals(administerUid)){
                    linearLayout.setVisibility(View.VISIBLE);
                }else
                    linearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ad_Button = findViewById(R.id.ad_Button);
        ad_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdministerActivity.this, ExpulsionActivity.class);
                startActivity(intent);
            }
        });

        re_Button = findViewById(R.id.re_Button);
        re_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdministerActivity.this, RefundActivity.class);
                startActivity(intent);
            }
        });

        love_Button = findViewById(R.id.love_Button);
        love_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdministerActivity.this, GiveLoveActivity.class);
                startActivity(intent);
            }
        });

    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<String> requestUids;
        public RecyclerViewAdapter() {
            requestUids = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("administer").child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    requestUids.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        requestUids.add(snapshot.getKey());
                    }

                    if(requestUids.size() < 1)
                    {
                        Intent intent = new Intent(AdministerActivity.this, MainActivity.class);
                        intent.putExtra("count", 0);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.fromleft, R.anim.toright);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_administer, parent, false);

            return new RecyclerViewAdapter.AdministerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
           RecyclerViewAdapter.AdministerViewHolder administerViewHolder = (( RecyclerViewAdapter.AdministerViewHolder ) holder);

           holder.itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                   Intent intent = new Intent(AdministerActivity.this, UserRequestActivity.class);
                   intent.putExtra("requestUid",requestUids.get(position));
                   startActivity(intent);
               }
           });

        }

        @Override
        public int getItemCount() {
            textView_count.setText("요청 수: "+requestUids.size());
            return requestUids.size();
        }

        private class AdministerViewHolder extends RecyclerView.ViewHolder {

            public AdministerViewHolder(View view) {
                super(view);
            }
        }
    } // recyclerview



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

    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }
}
