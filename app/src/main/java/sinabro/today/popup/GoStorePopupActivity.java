package sinabro.today.popup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import sinabro.today.R;


public class GoStorePopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_go_store_popup);

    }

    //확인 버튼 클릭
    public void mOnYes(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", 1);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    //취소 버튼 클릭
    public void mOnNo(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", 2);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

}