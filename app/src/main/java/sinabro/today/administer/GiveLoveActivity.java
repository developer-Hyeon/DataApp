package sinabro.today.administer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import sinabro.today.R;
import sinabro.today.custom.CustomProgressDialog;
import sinabro.today.model.BuyModel;
import sinabro.today.model.UserModel;

public class GiveLoveActivity extends AppCompatActivity {

    private EditText editText_name;
    private EditText editText_give;
    private Button love_search_button;
    private Button love_love_button;
    private LinearLayout linearLayout;
    private Toolbar toolbar;
    private CustomProgressDialog customProgressDialog;
    private String name;
    private String loveUid;
    private String count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_love);

        linearLayout = findViewById(R.id.linearlayout_love);
        love_love_button = findViewById(R.id.love_love_button);
        love_search_button = findViewById(R.id.love_search_button);
        editText_name = findViewById(R.id.love_edittext_name);
        editText_give = findViewById(R.id.lovegive_edittext);

        toolbar = findViewById(R.id.love_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        customProgressDialog = new CustomProgressDialog(GiveLoveActivity.this);
        customProgressDialog .getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        love_search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editText_name.getText().toString();
                FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userName").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                loveUid = item.getKey();
                            }
                            love_love_button.setVisibility(View.VISIBLE);
                            linearLayout.setVisibility(View.VISIBLE);

                        }else{
                            love_love_button.setVisibility(View.INVISIBLE);
                            linearLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }
        });

        love_love_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        GiveLoveActivity.this);
                final LayoutInflater inflater = getLayoutInflater();
                View alertView = inflater.inflate(R.layout.givelove_dialog, null);
                alertDialogBuilder.setView(alertView);

                final AlertDialog alertDialog = alertDialogBuilder.create();
                final TextView textView_no = alertView.findViewById(R.id.loveNo);
                textView_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();
                    }
                });
                final TextView textView_yes = alertView.findViewById(R.id.loveYes);
                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 회원 탈퇴 절차 넣기
                        if(editText_give.getText().toString().length() > 0){
                            customProgressDialog.show();
                            FirebaseDatabase.getInstance().getReference().child("users").child(loveUid).child("heart").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    count = dataSnapshot.getValue(String.class);
                                    final int newCount;
                                    newCount = Integer.parseInt(count) + Integer.parseInt(editText_give.getText().toString());
                                    FirebaseDatabase.getInstance().getReference().child("users").child(loveUid).child("heart").setValue(""+newCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            customProgressDialog.dismiss();
                                            editText_give.setText("");

                                            SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
                                            Date time = new Date();
                                            String time1 = format.format(time);
                                            String buys_date = time1;
                                            String buys_comment = "관리자 지급";
                                            String current_heart = Integer.toString(newCount);
                                            String buys_change = "+"+ editText_give.getText().toString();
                                            String buys_id = "temp";

                                            BuyModel buyModel = new BuyModel(buys_date,buys_comment,buys_change,current_heart,buys_id);
                                            FirebaseDatabase.getInstance().getReference().child("Buys").child(loveUid).push().setValue(buyModel);
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                //    if(valueEventListener!=null)
                //         databaseReference.removeEventListener(valueEventListener);
                finish();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // if(valueEventListener!=null)
        //     databaseReference.removeEventListener(valueEventListener);
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }
}