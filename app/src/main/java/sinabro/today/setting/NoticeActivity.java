package sinabro.today.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import sinabro.today.R;

public class NoticeActivity extends AppCompatActivity {


    private CheckBox message, propose, chatroom, event;
    private int message_flag, propose_flag, chatroom_flag, event_flag;
    private String uid;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        Toolbar toolbar = findViewById(R.id.toolbar_notice);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        uid = FirebaseAuth.getInstance().getUid();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("sinabro",0);
        message_flag = settings.getInt("message", 0);
        propose_flag = settings.getInt("propose", 0);
        chatroom_flag = settings.getInt("chatroom", 0);
        event_flag = settings.getInt("event", 0);
        // 0이면 켜짐, 1이면 꺼짐으로 구현함


        message = findViewById(R.id.message);
        propose = findViewById(R.id.propose);
        chatroom = findViewById(R.id.chatroom);
        event = findViewById(R.id.event);

        if(message_flag == 0)
            message.setChecked(true);
        else
            message.setChecked(false);

        if(propose_flag == 0)
            propose.setChecked(true);
        else
            propose.setChecked(false);


        if(chatroom_flag == 0)
            chatroom.setChecked(true);
        else
            chatroom.setChecked(false);


        if(event_flag == 0)
            event.setChecked(true);
        else
            event.setChecked(false);


        message.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(message.isChecked()){
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("message", 0);
                    editor.apply();

                } else{
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("message", 1);
                    editor.apply();

                }
            }
        });

        propose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(propose.isChecked()){
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("propose", 0);
                    editor.apply();

                } else{
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("propose", 1);
                    editor.apply();

                }
            }
        });

        chatroom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(chatroom.isChecked()){
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("chatroom", 0);
                    editor.apply();
                } else{
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("chatroom", 1);
                    editor.apply();

                }
            }
        });

        event.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(event.isChecked()){
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("event", 1); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐)
                    editor.apply();
                } else{
                    SharedPreferences sharedPreferences =  getSharedPreferences("sinabro",0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("event", 0); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐)
                    editor.apply();
                }
            }
        });

    }

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