package sinabro.today.setting;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import sinabro.today.R;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class MapSettingActivity extends AppCompatActivity {

    int what_switch,what_sex;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private String my_sex;
    private CheckBox location_switch, all_switch, man_switch, woman_switch;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_setting);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar_mapsetting);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        location_switch = findViewById(R.id.location_switch);
        all_switch = findViewById(R.id.all_switch);
        man_switch = findViewById(R.id.man_switch);
        woman_switch = findViewById(R.id.woman_switch);


        mDatabase.child("users").child(user.getUid()).child("sex").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                my_sex = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        SharedPreferences sf = getSharedPreferences("sinabro",MODE_PRIVATE);
        what_switch = sf.getInt("switch",0);

        if(what_switch == 0) {   // 스위치 오프
            location_switch.setChecked(false);
        } else if(what_switch == 1){
            location_switch.setChecked(true);
        }

        location_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(location_switch.isChecked()){
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("switch", 1); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐
                    editor.putInt("mapsetting",1);
                    editor.apply();

                } else{
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("switch", 0); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐)
                    editor.putInt("mapsetting",1);
                    editor.apply();

                    if(my_sex.equals("남자")){
                        mDatabase.child("man_location").child(user.getUid()).removeValue();
                    } else if(my_sex.equals("여자")){
                        mDatabase.child("woman_location").child(user.getUid()).removeValue();
                    }

                }
            }

        });
        ///////////////////////////////////////////// 스위치 온오프

        SharedPreferences sf2 = getSharedPreferences("sinabro",MODE_PRIVATE);
        what_sex = sf2.getInt("who",0);

        if(what_sex == 0){
            all_switch.setChecked(true);
            man_switch.setChecked(false);
            woman_switch.setChecked(false);
        } else if(what_sex == 1){
            man_switch.setChecked(true);
            all_switch.setChecked(false);
            woman_switch.setChecked(false);
        } else if(what_sex == 2){
            woman_switch.setChecked(true);
            all_switch.setChecked(false);
            man_switch.setChecked(false);
        }

        all_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(all_switch.isChecked()){
                    man_switch.setChecked(false);
                    woman_switch.setChecked(false);
                    SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("who", 0);
                    editor.putInt("mapsetting",1);
                    editor.apply();
                }

            }
        });

        man_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(man_switch.isChecked()) {
                    all_switch.setChecked(false);
                    woman_switch.setChecked(false);
                    SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("who", 1);
                    editor.putInt("mapsetting",1);
                    editor.apply();
                }
            }
        });

        woman_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(woman_switch.isChecked()) {
                    all_switch.setChecked(false);
                    man_switch.setChecked(false);
                    SharedPreferences sharedPreferences = getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("who", 2);
                    editor.putInt("mapsetting",1);
                    editor.apply();
                }
            }
        });
    } // oncreate()


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                overridePendingTransition(R.anim.fromright, R.anim.toleft);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fromright, R.anim.toleft);
    }

}