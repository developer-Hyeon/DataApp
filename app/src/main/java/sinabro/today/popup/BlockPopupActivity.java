package sinabro.today.popup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import sinabro.today.R;

public class BlockPopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_block_popup);
    }

    //확인 버튼 클릭
    public void mOnblockYes(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", 3);
        setResult(RESULT_OK, intent);


        //액티비티(팝업) 닫기
        finish();
        startToast("블랙리스트에 추가되었습니다.");
    }
    //취소 버튼 클릭
    public void mOnblockNo(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", 0);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
