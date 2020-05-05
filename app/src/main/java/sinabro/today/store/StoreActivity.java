package sinabro.today.store;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import sinabro.today.R;
import sinabro.today.model.BuyModel;


public class StoreActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private Toolbar toolbar;
    private String heart;
    private LinearLayout heart_120, heart_240, heart_660, heart_1380;
    private LinearLayout heart_write;
    private TextView textview_heart;
    private BillingClient mBillingClient;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String db_heart;
    private int db_heart_int;
    private String heart_number;


    SkuDetails skuDetails120 , skuDetails240, skuDetails600, skuDetails1320;
    String heart120 = "120heart",
            heart240 = "240heart",
            heart660 = "660heart",
            heart1380 = "1380heart";  //제품 ID


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        toolbar = findViewById(R.id.store_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        heart_120 = findViewById(R.id.heart_120);
        heart_240 = findViewById(R.id.heart_240);
        heart_660 = findViewById(R.id.heart_660);
        heart_1380 = findViewById(R.id.heart_1380);

        heart_write = findViewById(R.id.heart_write);
        heart_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(HeartwriteActivity.class);
            }
        });

        heart_120.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartWhat(120);
            }
        });

        heart_240.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartWhat(240);
            }
        });

        heart_660.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartWhat(660);
            }
        });

        heart_1380.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartWhat(1380);
            }
        });

        Intent intent = getIntent();
        heart = intent.getStringExtra("heart");

        mBillingClient = BillingClient.newBuilder(StoreActivity.this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    List<String> skuList = new ArrayList<>();
                    skuList.add(heart120);
                    skuList.add(heart240);
                    skuList.add(heart660);
                    skuList.add(heart1380);

                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                            // Process the result.
                            if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                                for (SkuDetails skuDetails : skuDetailsList) {
                                    String sku = skuDetails.getSku(); // 제품 id
                                    String price = skuDetails.getPrice(); // 제품 가격

                                    if(heart120.equals(sku)) {
                                        skuDetails120 = skuDetails;
                                    } else if(heart240.equals(sku)) {
                                        skuDetails240 = skuDetails;
                                    } else if(heart660.equals(sku)) {
                                        skuDetails600 = skuDetails;
                                    } else if(heart1380.equals(sku)) {
                                        skuDetails1320 = skuDetails;
                                    }
                                }
                            }
                        }});
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(StoreActivity.this,"구글 플레이에 접속하지 못하였습니다. 재접속 해 주세요.",Toast.LENGTH_SHORT).show();
            }
        });


    } // onCreate()

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            Toast.makeText(StoreActivity.this,"결제를 완료하였습니다.",Toast.LENGTH_SHORT).show();
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(StoreActivity.this,"결제를 취소하였습니다.",Toast.LENGTH_SHORT).show();
        } else {
            // Handle any other error codes.
        }
    }

    private void handlePurchase(Purchase purchase) {
        final String purchaseToken, orderID;
        purchaseToken = purchase.getPurchaseToken();
        orderID = purchase.getOrderId();

        mBillingClient.consumeAsync(purchaseToken, consumeListener);


        mDatabase.child("users").child(user.getUid()).child("heart").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                db_heart  = dataSnapshot.getValue(String.class);

                db_heart_int = Integer.parseInt(Objects.requireNonNull(db_heart));
                if(heart_number.equals("120")){
                    db_heart_int = db_heart_int + 120;
                    db_heart = Integer.toString(db_heart_int);
                } else if(heart_number.equals("240")){
                    db_heart_int = db_heart_int + 240;
                    db_heart = Integer.toString(db_heart_int);
                } else if(heart_number.equals("660")){
                    db_heart_int = db_heart_int + 660;
                    db_heart = Integer.toString(db_heart_int);
                } else if(heart_number.equals("1380")){
                    db_heart_int = db_heart_int + 1380;
                    db_heart = Integer.toString(db_heart_int);
                }

                SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
                Date time = new Date();
                String time1 = format.format(time);


                String buys_date = time1;
                String buys_comment = "스토어충전";
                String buys_change = "+" + heart_number;
                String current_heart = db_heart;
                String buys_id = orderID;

                BuyModel buyModel = new BuyModel(buys_date,buys_comment,buys_change,current_heart,buys_id);
                FirebaseDatabase.getInstance().getReference().child("Buys").child(user.getUid()).push().setValue(buyModel);
                mDatabase.child("users").child(user.getUid()).child("heart").setValue(current_heart);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    ConsumeResponseListener consumeListener = new ConsumeResponseListener() {
        @Override
        public void onConsumeResponse(@BillingClient.BillingResponse int responseCode, String outToken) {
            if (responseCode == BillingClient.BillingResponse.OK) {
                // Handle the success of the consume operation.
                // For example, increase the number of coins inside the user's basket.
                // 결제하고 소비를 하면서 여기서 db에 넣어줌
                //Toast.makeText(StoreActivity.this,"소비됨",Toast.LENGTH_SHORT).show();


            }
        }
    };

    private void doBillingFlow(SkuDetails skuDetails) {
        BillingFlowParams flowParams;
        int responseCode;

        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        flowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
        mBillingClient.launchBillingFlow(StoreActivity.this, flowParams);


        // 구매 완료후 consume 를 제대로 하지 못한 경우
        /*
        if(responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
            onPurchasesUpdated(BillingClient.BillingResponse.OK, purchasesResult.getPurchasesList());
        }*/

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:

                finish();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    public void heartWhat(int heart){
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(
                StoreActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.store_dialog, null);
        alertDialogBuilder.setView(alertView);
        // 다이얼로그 생성
        final androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
        final TextView textView_no = alertView.findViewById(R.id.store_cancel);
        final TextView textView_yes = alertView.findViewById(R.id.store_buy);
        final TextView textView_store_heart_what = alertView.findViewById(R.id.store_heart_what);
        final TextView textView_store_heart_money=  alertView.findViewById(R.id.store_heart_money);

        // 다이얼로그 보여주기
        alertDialog.show();

        switch (heart){

            case 120:
                textView_store_heart_what.setText("하트 120개 상품을 구매하시겠습니까?");
                textView_store_heart_money.setText("금액 ￦6,800");
                textView_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doBillingFlow(skuDetails120);
                        heart_number = "120";
                        alertDialog.dismiss();
                    }
                });

                break;


            case 240:
                textView_store_heart_what.setText("하트 240개 상품을 구매하시겠습니까?");
                textView_store_heart_money.setText("금액 ￦10,800");
                textView_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 확인했을때 처리
                        doBillingFlow(skuDetails240);
                        heart_number = "240";
                        alertDialog.dismiss();
                    }
                });

                break;

            case 660:
                textView_store_heart_what.setText("하트 660개 상품을 구매하시겠습니까?");
                textView_store_heart_money.setText("금액 ￦27,000");
                textView_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 확인했을때 처리
                        doBillingFlow(skuDetails600);
                        heart_number = "660";
                        alertDialog.dismiss();
                    }
                });

                break;

            case 1380:
                textView_store_heart_what.setText("하트 1,380개 상품을 구매하시겠습니까?");
                textView_store_heart_money.setText("금액 ￦50,000");
                textView_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 확인했을때 처리
                        doBillingFlow(skuDetails1320);
                        heart_number = "1380";
                        alertDialog.dismiss();
                    }
                });
                break;

        }
    }

    private void startToast(String msg) {
        Toast.makeText(StoreActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

}