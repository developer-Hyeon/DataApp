package sinabro.today.popup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import sinabro.today.R;


public class SexSelectPopupActivity extends Activity {

    private ImageView manImageView;
    private ImageView womanImageView;
    int selectFlag = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sex_select_popup);
        //UI 객체생성
        manImageView = findViewById(R.id.sexpopup_man_button);
        womanImageView = findViewById(R.id.sexpopup_woman_button);


        manImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFlag = 1;
                manImageView.setImageResource(R.drawable.selectmanok);
                womanImageView.setImageResource(R.drawable.selectwoman);
            }
        });

        womanImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFlag = 2;
                manImageView.setImageResource(R.drawable.selectman);
                womanImageView.setImageResource(R.drawable.selectwomanok);

            }
        });


    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", selectFlag);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

}
