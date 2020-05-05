package sinabro.today.administer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import sinabro.today.R;
import sinabro.today.model.BuyModel;

public class RefundActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editText;
    private Button searchButton;
    private Toolbar toolbar;
    private String name="";
    private String refundUserUid="";
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund);

        searchButton = findViewById(R.id.refund_search_button);
        editText = findViewById(R.id.refund_edittext_name);
        linearLayout = findViewById(R.id.refund_linearlayout);

        toolbar = findViewById(R.id.re_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.refund_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(RefundActivity.this));
        recyclerView.setAdapter(new RecyclerViewAdapter());

    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<BuyModel> buy_item = new ArrayList<>();
        BuyModel buyModel;

        public RecyclerViewAdapter() {

            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    name = editText.getText().toString();
                    FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userName").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    refundUserUid = item.getKey();
                                    linearLayout.setVisibility(View.VISIBLE);

                                    buy_item.clear();
                                    FirebaseDatabase.getInstance().getReference().child("Buys").child(refundUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                                buyModel = item.getValue(BuyModel.class);
                                                buy_item.add(buyModel);
                                            }

                                            notifyDataSetChanged();
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }else{
                                startToast("해당 유저가 존재하지 않습니다.");
                                linearLayout.setVisibility(View.INVISIBLE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            });
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_refund, parent, false);
            return new RefundViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            RefundViewHolder refundViewHolder = ((RefundViewHolder) holder);

            refundViewHolder.buys_id.setText(buy_item.get(position).buys_id);
            refundViewHolder.date_heart.setText(buy_item.get(position).buys_date);
            refundViewHolder.current_heart.setText(buy_item.get(position).current_heart);
            refundViewHolder.change_heart.setText(buy_item.get(position).buys_change);
        }

        @Override
        public int getItemCount() {
            return buy_item.size();
        }
        private class RefundViewHolder extends RecyclerView.ViewHolder {

            TextView date_heart;
            TextView buys_id;
            TextView change_heart;
            TextView current_heart;

            public RefundViewHolder(View view) {
                super(view);

                date_heart = itemView.findViewById(R.id.date_heart);
                buys_id = itemView.findViewById(R.id.buys_id);
                change_heart = itemView.findViewById(R.id.change_heart);
                current_heart = itemView.findViewById(R.id.current_heart);
            }
        }
    }


    private void startToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

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
