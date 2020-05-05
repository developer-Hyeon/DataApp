package sinabro.today;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Objects;

public class PictureZoom extends AppCompatActivity {

    private PhotoView imageView;
    private String photourl;
    private Button photo_zoom_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_zoom);

        imageView = findViewById(R.id.imageView);
        photo_zoom_back = findViewById(R.id.photo_zoom_back);

        Intent intent = getIntent(); /*데이터 수신*/
        photourl = Objects.requireNonNull(intent.getExtras()).getString("photourl"); /*String형*/
        Glide.with(this).load(photourl).centerCrop().into(imageView);



        photo_zoom_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    } // oncreate()



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
        finish();
    }
}
