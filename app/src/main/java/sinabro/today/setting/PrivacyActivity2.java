package sinabro.today.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Objects;

import sinabro.today.R;

public class PrivacyActivity2 extends AppCompatActivity {

    private WebView wv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy2);

        Toolbar toolbar = findViewById(R.id.toolbar_privacy2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        final Button service_btn = findViewById(R.id.service_btn);
        final Button location_btn = findViewById(R.id.location_btn);

        wv = findViewById(R.id.wvlocal2);
        wv.loadUrl("file:///android_asset/servicepolicy.html");
        service_btn.setTextColor(Color.BLUE);


        service_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wv.loadUrl("file:///android_asset/servicepolicy.html");
                service_btn.setTextColor(Color.BLUE);
                location_btn.setTextColor(Color.BLACK);
            }
        });

        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wv.loadUrl("file:///android_asset/locationpolicy.html");
                service_btn.setTextColor(Color.BLACK);
                location_btn.setTextColor(Color.BLUE);
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
        //super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fromright, R.anim.toleft);
    }
}