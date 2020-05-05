package sinabro.today.popup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import sinabro.today.R;
import sinabro.today.model.BuyModel;

public class ProposePopupActivity extends Activity {

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String current_heart;
    private String recommend_heart_str;
    private int recommend_heart_int;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_propose_popup);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("users").child(user.getUid()).child("heart").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recommend_heart_str = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //확인 버튼 클릭
    public void mOnYes(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", 1);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
        startToast("요청완료");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
        Date time = new Date();
        String time1 = format.format(time);

        recommend_heart_int = Integer.parseInt(Objects.requireNonNull(recommend_heart_str));
        recommend_heart_int = recommend_heart_int - 30;
        recommend_heart_str = Integer.toString(recommend_heart_int);

        String buys_date = time1;
        String buys_comment = "대화요청";
        String buys_change = "-30";
        String current_heart = recommend_heart_str;
        String buys_id = "temp";

        BuyModel buyModel = new BuyModel(buys_date,buys_comment,buys_change,current_heart,buys_id);
        FirebaseDatabase.getInstance().getReference().child("Buys").child(user.getUid()).push().setValue(buyModel);

    }

    //취소 버튼 클릭
    public void mOnNo(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", 2);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}