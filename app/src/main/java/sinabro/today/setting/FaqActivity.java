package sinabro.today.setting;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Objects;
import sinabro.today.R;

public class FaqActivity extends AppCompatActivity {

    ImageView faq1, faq2,faq3;
    WebView faqweb1, faqweb2,faqweb3;
    int tmp1,tmp2,tmp3 = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        Toolbar toolbar = findViewById(R.id.toolbar_faq);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        faq1 = findViewById(R.id.faq1);
        faq2 = findViewById(R.id.faq2);
        faq3 = findViewById(R.id.faq3);


        faqweb1 = findViewById(R.id.faqweb1);
        faqweb2 = findViewById(R.id.faqweb2);
        faqweb3 = findViewById(R.id.faqweb3);



        faq1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tmp1 == 0){
                    faqweb1.loadUrl("file:///android_asset/faq2.html");
                    faqweb1.setVisibility(View.VISIBLE);
                    faq1.setImageResource(R.drawable.round_keyboard_arrow_up_24);
                    tmp1 = 1;
                }
                else if( tmp1 == 1){
                    faqweb1.setVisibility(View.GONE);
                    faq1.setImageResource(R.drawable.round_keyboard_arrow_down_24);
                    tmp1 = 0;
                }
            }
        });


        faq2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tmp2 == 0){
                    faqweb2.loadUrl("file:///android_asset/faq1.html");
                    faqweb2.setVisibility(View.VISIBLE);
                    faq2.setImageResource(R.drawable.round_keyboard_arrow_up_24);
                    tmp2 = 1;
                }
                else if( tmp2 == 1){
                    faqweb2.setVisibility(View.GONE);
                    faq2.setImageResource(R.drawable.round_keyboard_arrow_down_24);
                    tmp2 = 0;
                }
            }
        });

        faq3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tmp3 == 0){
                    faqweb3.loadUrl("file:///android_asset/faq3.html");
                    faqweb3.setVisibility(View.VISIBLE);
                    faq3.setImageResource(R.drawable.round_keyboard_arrow_up_24);
                    tmp3 = 1;
                }
                else if( tmp3 == 1){
                    faqweb3.setVisibility(View.GONE);
                    faq3.setImageResource(R.drawable.round_keyboard_arrow_down_24);
                    tmp3 = 0;
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