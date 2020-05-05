package sinabro.today.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

public class CustomImageView2  extends AppCompatImageView {

    public static float radius = 50.0f;

    public CustomImageView2(Context context) {
        super(context);
    }

    public CustomImageView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        @SuppressLint("DrawAllocation") Path clipPath = new Path();
        @SuppressLint("DrawAllocation") RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}