package sinabro.today.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.Window;

import sinabro.today.R;


public class CustomProgressDialog extends Dialog {

    public CustomProgressDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);

    }
    @Override public void onBackPressed() {
        //super.onBackPressed(); }
        return;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

}
