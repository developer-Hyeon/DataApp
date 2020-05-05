package sinabro.today.signup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sinabro.today.R;
import sinabro.today.custom.CustomProgressDialog;
import sinabro.today.main.MainActivity;
import sinabro.today.start.FirstActivity;
import sinabro.today.start.MemberCheckActivity;

public class EmailSignupActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Button  nextButton;
    private EditText email;
    private EditText password;
    private EditText passwordCheck;
    private TextView emailCheck_Textview;
    private ImageView emailCheck_ImageView;
    private TextView passwordCheck_Textview;
    private ImageView passwordCheck_ImageView;
    private TextView rePasswordCheck_Textview;
    private ImageView rePpasswordCheck_ImageView;
    private LinearLayout linearLayout_email;
    private LinearLayout linearLayout_password;
    private LinearLayout linearLayout_repassword;
    private String  sex;
    private CustomProgressDialog customProgressDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_signup);

        toolbar = findViewById(R.id.email_toolbar);
        nextButton = findViewById(R.id.email_button);
        email = findViewById(R.id.email_edittext_email);
        password = findViewById(R.id.email_edittext_password);
        passwordCheck = findViewById(R.id.email_edittext_password_check);
        emailCheck_Textview = findViewById(R.id.email_check_textview);
        emailCheck_ImageView = findViewById(R.id.email_check_imageview);
        passwordCheck_Textview = findViewById(R.id.password_check_textview);
        passwordCheck_ImageView = findViewById(R.id.password_check_imageview);
        rePasswordCheck_Textview = findViewById(R.id.repassword_check_textview);
        rePpasswordCheck_ImageView = findViewById(R.id.repassword_check_imageview);
        linearLayout_email = findViewById(R.id.email_linear_email);
        linearLayout_password = findViewById(R.id.email_linear_password);
        linearLayout_repassword = findViewById(R.id.email_linear_repassword);

        password.setClickable(false);
        password.setFocusable(false);
        passwordCheck.setClickable(false);
        passwordCheck.setFocusable(false);

        customProgressDialog = new CustomProgressDialog(EmailSignupActivity.this);
        customProgressDialog .getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        SharedPreferences sf = getSharedPreferences("sinabro",MODE_PRIVATE);
        sex = sf.getString("sex","");
        email.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    emailCheck_ImageView.setImageResource(R.drawable.id);
                    emailCheck_Textview.setVisibility(View.GONE);
                    linearLayout_email.setBackgroundResource(R.drawable.border1);
                    linearLayout_password.setBackgroundResource(R.drawable.border);
                    linearLayout_repassword.setBackgroundResource(R.drawable.border);
                }
                return false;
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { // 입력하기 전에
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { // 입력되는 텍스트에 변화가 있을떄

                emailCheck_ImageView.setImageResource(R.drawable.id);
                emailCheck_Textview.setVisibility(View.GONE);
                linearLayout_email.setBackgroundResource(R.drawable.border1);
                linearLayout_password.setBackgroundResource(R.drawable.border);
                linearLayout_repassword.setBackgroundResource(R.drawable.border);
            }
            @Override
            public void afterTextChanged(final Editable editable) { // 입력이 끝났을때

            } // afterText
        }); // etx

        password.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(email.getText().toString().length()>0 && checkEmail(email.getText().toString())){
                        linearLayout_email.setBackgroundResource(R.drawable.border);
                        linearLayout_repassword.setBackgroundResource(R.drawable.border);
                        linearLayout_password.setBackgroundResource(R.drawable.border1);
                        password.setClickable(true);
                        password.setFocusableInTouchMode(true);
                        password.setFocusable(true);

                    }else {
                        emailCheck_Textview.setVisibility(View.VISIBLE);
                        emailCheck_Textview.setText("이메일 형식이 올바르지 않습니다.");
                        emailCheck_ImageView.setImageResource(R.drawable.warning);
                        password.setClickable(false);
                        password.setFocusable(false);
                    }//클릭했을 경우 발생할 이벤트 작성
                }
                return false;
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { // 입력하기 전에
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { // 입력되는 텍스트에 변화가 있을떄

                passwordCheck_ImageView.setImageResource(R.drawable.password);
                passwordCheck_Textview.setVisibility(View.GONE);
                linearLayout_email.setBackgroundResource(R.drawable.border);
                linearLayout_password.setBackgroundResource(R.drawable.border1);
                linearLayout_repassword.setBackgroundResource(R.drawable.border);
            }
            @Override
            public void afterTextChanged(final Editable editable) { // 입력이 끝났을때
            } // afterText
        }); // etx


        passwordCheck.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(password.getText().toString().length() >5){
                        linearLayout_email.setBackgroundResource(R.drawable.border);
                        linearLayout_password.setBackgroundResource(R.drawable.border);
                        linearLayout_repassword.setBackgroundResource(R.drawable.border1);
                        passwordCheck.setClickable(true);
                        passwordCheck.setFocusableInTouchMode(true);
                        passwordCheck.setFocusable(true);

                    }else {
                        passwordCheck_Textview.setVisibility(View.VISIBLE);
                        passwordCheck_ImageView.setImageResource(R.drawable.warning);
                        passwordCheck.setClickable(false);
                        passwordCheck.setFocusable(false);
                    }//클릭했을 경우 발생할 이벤트 작성

                }
                return false;
            }
        });

        passwordCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { // 입력하기 전에
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { // 입력되는 텍스트에 변화가 있을떄

                rePpasswordCheck_ImageView.setImageResource(R.drawable.password);
                rePasswordCheck_Textview.setVisibility(View.GONE);
                linearLayout_email.setBackgroundResource(R.drawable.border);
                linearLayout_password.setBackgroundResource(R.drawable.border);
                linearLayout_repassword.setBackgroundResource(R.drawable.border1);
            }
            @Override
            public void afterTextChanged(final Editable editable) { // 입력이 끝났을때
            } // afterText
        }); // etx



        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().toString().length() == 0 )
                {
                    startToast("이메일 입력해 주세요.");
                    emailCheck_Textview.setVisibility(View.VISIBLE);
                    emailCheck_Textview.setText("이메일 형식이 올바르지 않습니다.");
                    emailCheck_ImageView.setImageResource(R.drawable.warning);
                }else if(!checkEmail(email.getText().toString())){
                    emailCheck_Textview.setVisibility(View.VISIBLE);
                    emailCheck_Textview.setText("이메일 형식이 올바르지 않습니다.");
                    emailCheck_ImageView.setImageResource(R.drawable.warning);
                }
                else if( password.getText().toString().length() == 0)
                {
                    startToast("비밀번호를 입력해 주세요.");
                    passwordCheck_Textview.setVisibility(View.VISIBLE);
                    passwordCheck_ImageView.setImageResource(R.drawable.warning);

                }
                else if(passwordCheck.getText().toString().length() == 0)
                {
                    startToast("비밀번호를 확인해주세요.");
                    rePasswordCheck_Textview.setVisibility(View.VISIBLE);
                    rePpasswordCheck_ImageView.setImageResource(R.drawable.warning);

                }else if(!password.getText().toString().equals(passwordCheck.getText().toString()))
                {
                    rePasswordCheck_Textview.setVisibility(View.VISIBLE);
                    rePpasswordCheck_ImageView.setImageResource(R.drawable.warning);
                }
                else{
                    customProgressDialog.show();
                    signUp();
                }


            }
        }); // 다음 버튼 클릭

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(EmailSignupActivity.this, FirstActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signUp() {

                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(EmailSignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    startToast("이메일 등록 성공");
                                    final String uid;
                                    uid = task.getResult().getUser().getUid();
                                    SharedPreferences sharedPreferences = getSharedPreferences("sinabro", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("password", password.getText().toString()); // key, value를 이용하여 저장하는 형태
                                    editor.putString("email", email.getText().toString());
                                    editor.putString("uid",uid);
                                    editor.putInt("join", 1); // 프로필로 갈때 쉐어드 1으로 저장 프로필에서 메인으로 가면 쉐어드 2
                                    editor.commit();

                                    Intent intent = new Intent(EmailSignupActivity.this, ProfileSignupActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fromright, R.anim.toleft);
                                    EmailSignupActivity.this.finish();
                                    customProgressDialog.dismiss();


                                } else {
                                    if (task.getException() != null) {
                                        startToast("이미 등록된 이메일입니다. 다시 입력해주세요.");
                                        emailCheck_Textview.setVisibility(View.VISIBLE);
                                        emailCheck_Textview.setText("이미 등록된 이메일입니다. 다시 입력해주세요.");
                                        emailCheck_ImageView.setImageResource(R.drawable.warning);
                                        customProgressDialog.dismiss();
                                    }
                                }
                            }
                        });

    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static boolean checkEmail(String email){

        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        boolean isNormal = m.matches();
        return isNormal;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EmailSignupActivity.this, FirstActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }





}
