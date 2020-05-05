package sinabro.today.setting;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Objects;
import sinabro.today.R;

public class PrivacyActivity extends AppCompatActivity {

    private WebView wv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        Toolbar toolbar = findViewById(R.id.toolbar_privacy);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        wv = findViewById(R.id.wvlocal);
        wv.loadUrl("file:///android_asset/privacypolicy.html");

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