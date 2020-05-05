package sinabro.today.administer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import sinabro.today.R;
import sinabro.today.custom.CustomProgressDialog;
import sinabro.today.information.DeviceInfo;
import sinabro.today.information.TimerInfo;
import sinabro.today.model.UserModel;

public class ExpulsionActivity extends AppCompatActivity {

    private Button exButton;
    private Button exSearchButton;
    private TextView textView1;
    private TextView textView2;
    private EditText editText;
    private String name="";
    private Query databaseReference;
    private List<String> photourl = new ArrayList<>();
    private UserModel userModel = new UserModel();
    private String expulsionUid;
    private TextView textView3;
    private DeviceInfo deviceInfo = new DeviceInfo();
    private Toolbar toolbar;
    private CustomProgressDialog customProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expulsion);

        exButton = findViewById(R.id.ex_ex_button);
        exSearchButton = findViewById(R.id.ex_search_button);
        textView1 = findViewById(R.id.ex_text1);
        textView2 = findViewById(R.id.ex_text2);
        textView3 = findViewById(R.id.ex_text3);
        editText = findViewById(R.id.ex_edittext_name);

        toolbar = findViewById(R.id.ex_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        customProgressDialog = new CustomProgressDialog(ExpulsionActivity.this);
        customProgressDialog .getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        exSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editText.getText().toString();
                databaseReference = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("userName").equalTo(name);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                userModel = item.getValue(UserModel.class);
                                expulsionUid = item.getKey();
                                Log.d("namgung",userModel.email);
                            }

                            textView3.setText(userModel.email);
                            textView3.setVisibility(View.VISIBLE);
                            textView1.setVisibility(View.VISIBLE);
                            textView2.setVisibility(View.VISIBLE);
                            exButton.setVisibility(View.VISIBLE);

                        }else{
                            textView1.setVisibility(View.INVISIBLE);
                            textView2.setVisibility(View.INVISIBLE);
                            exButton.setVisibility(View.INVISIBLE);
                            textView3.setVisibility(View.INVISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        exButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            ExpulsionActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View alertView = inflater.inflate(R.layout.expulsion_dialog, null);
                    alertDialogBuilder.setView(alertView);

                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    final TextView textView_no = alertView.findViewById(R.id.exNo);
                    textView_no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.cancel();
                        }
                    });
                    final TextView textView_yes = alertView.findViewById(R.id.exYes);
                    textView_yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            delete();
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
            }
        });
    }


    public void delete() {
        final String uid = FirebaseAuth.getInstance().getUid();
        assert uid != null;

        FirebaseDatabase.getInstance().getReference().child("administer").child("users").child(expulsionUid).removeValue(); // 관리자 계정 사진 요청 삭제
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" +expulsionUid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    if(item.child("users").child(expulsionUid).exists()){
                        Log.d("namgung","방 키값 "+item.getKey());
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(Objects.requireNonNull(item.getKey())).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); // 채팅방이 존재하면 삭제해준다

        FirebaseDatabase.getInstance().getReference().child("Buys").child(uid).removeValue();// 구매목록 삭제

        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();

        FirebaseDatabase.getInstance().getReference().child("users").child(expulsionUid).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("imageHashCode").exists()) {
                        photourl.add(snapshot.child("imageHashCode").getValue(String.class));
                    } else if(snapshot.child("tempHashCode").exists()) {
                        photourl.add(snapshot.child("tempHashCode").getValue(String.class));
                    }
                }

                databaseReference = FirebaseDatabase.getInstance().getReference().child("deviceinfo").orderByChild("email").equalTo(userModel.email);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            deviceInfo = snapshot.getValue(DeviceInfo.class);
                            String today = null;
                            Date date = new Date();

                            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Java 시간 더하기

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);

                            cal.add(Calendar.DATE, 30); //30 일후
                            today = sdformat.format(cal.getTime());

                            TimerInfo timerInfo = new TimerInfo(today, deviceInfo.getDeviceID()); //
                            FirebaseDatabase.getInstance().getReference().child("TimeInfo").child(deviceInfo.getDeviceID()).setValue(timerInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    FirebaseDatabase.getInstance().getReference().child("deviceinfo").child(Objects.requireNonNull(snapshot.getKey())).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                        }
                                    });
                                }
                            });

                        }
                    }
                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                    }
                });

                FirebaseDatabase.getInstance().getReference().child("users").child(expulsionUid).removeValue();
                FirebaseDatabase.getInstance().getReference().child("man_location").child(expulsionUid).removeValue();
                FirebaseDatabase.getInstance().getReference().child("woman_location").child(expulsionUid).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Buys").child(expulsionUid).removeValue();

                for(int i =0; i< photourl.size(); i++) {
                    StorageReference deleteRef = storageRef.child("userImages/" + expulsionUid + "/" + photourl.get(i));
                    deleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            customProgressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                        }
                    });
                }

            } // onDataChange()

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        SharedPreferences pref = getSharedPreferences("sinabro", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();


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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }
}
