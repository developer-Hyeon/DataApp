package sinabro.today.start;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import sinabro.today.R;
import sinabro.today.custom.CustomProgressDialog;
import sinabro.today.information.DeviceInfo;
import sinabro.today.login.SimpleLoginActivity;
import sinabro.today.main.MainActivity;
import sinabro.today.model.AdministerModel;
import sinabro.today.signup.EmailSignupActivity;

public class MemberCheckActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String androidId;
    private DatabaseReference mDatabase;
    private CustomProgressDialog customProgressDialog;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_check);

        customProgressDialog = new CustomProgressDialog(MemberCheckActivity.this);
        customProgressDialog .getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.show();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user!=null){
            Log.e("moment", user.getUid());
        }
        androidId = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);

        SharedPreferences sf = getSharedPreferences("sinabro",MODE_PRIVATE);
        int joinCheck = sf.getInt("join",0);

        mDatabase.orderByChild("users/" + "sex").equalTo("남자").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    Log.e("testt", String.valueOf(item));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if(joinCheck == 1)
        {
            Intent intent = new Intent(MemberCheckActivity.this, FirstActivity.class);
            startActivity(intent);
            finish();
            customProgressDialog.dismiss();

        }else{

            if (user != null) { // user값 있으면 지도 메인  페이지로
                Intent intent = new Intent(MemberCheckActivity.this, MainActivity.class);
                startActivity(intent);
                customProgressDialog.dismiss();
                Log.e("users", String.valueOf(user.getIdToken(false)));
            } else { // 아니면 회원가입 or 간편로그인

                Query databaseReference = FirebaseDatabase.getInstance().getReference().child("deviceinfo").orderByChild("deviceID").equalTo(androidId);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            myStartActivity(SimpleLoginActivity.class);
                            finish();
                            customProgressDialog.dismiss();
                        } else {
                            myStartActivity(FirstActivity.class);
                            finish();
                            customProgressDialog.dismiss();
                        }
                    }
                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                    }
                });
            } // else
        }

    } // oncreate

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}