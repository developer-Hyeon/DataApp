package sinabro.today.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import sinabro.today.R;
import sinabro.today.propose.ProposeReciveActivity;

public class LoveFragment extends Fragment {
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private int index, flag;
    private GridView gridView;
    private TextView textView;
    private LinearLayout linearLayout;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_love_fragment,container,false);

        Log.d("namgung","LoveFragment 실행됨");
        linearLayout = view.findViewById(R.id.love_list);
        textView  = view.findViewById(R.id.love_text1);
        gridView = view.findViewById(R.id.grid_view);
        gridView.setAdapter(new GridAdapter());
        assert container != null;
        context = container.getContext();
        return view;
    }

    public class GridAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<String> destinationImages;
        String myUid;
        List<String> destinationUids;
        List<String> destinationName;
        List<String> timestemps;
        String destUid;
        public GridAdapter(){
            inflater = (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            destinationImages = new ArrayList<>();
            destinationUids = new ArrayList<>();
            destinationName = new ArrayList<>();
            timestemps = new ArrayList<>();
            myUid = FirebaseAuth.getInstance().getUid();
            FirebaseDatabase.getInstance().getReference().child("users").child(myUid).child("pushList").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    destinationUids.clear();
                    destinationImages.clear();
                    destinationName.clear();
                    timestemps.clear();
                    for (final DataSnapshot item :dataSnapshot.getChildren()) {
                        destinationUids.add(item.getKey()); // 푸시리스트 상대방 uid 받아옴
                        timestemps.add(item.getValue(String.class)); // 시간받아옴
                    }

                    Log.d("namgung","유아이디 크기 "+destinationUids.size());
                    for(int i=0; i<destinationUids.size(); i++){

                        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUids.get(i)).child("photo").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                flag = 0;

                                Log.d("namgung","사진 데이터베이스 접근");
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    if(item.child("image").exists()){
                                        destinationImages.add(item.child("image").getValue(String.class));
                                        flag = 1;
                                        break;
                                    }
                                }
                                if(flag == 0)
                                    destinationImages.add("");

                                sortLovelist();
                                notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        }); // 상대방 사진 받아오기

                        destUid = destinationUids.get(i);
                        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUids.get(i)).child("userName").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.d("namgung","이름 데이터베이스 접근");
                                if(!dataSnapshot.exists()){
                                    FirebaseDatabase.getInstance().getReference().child("users").child(myUid).child("pushList").child(destUid).removeValue();
                                    return;
                                }else{
                                    destinationName.add(dataSnapshot.getValue(String.class));
                                    sortLovelist();
                                    notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        }); // 상대방 이름 받아오기

                    }

                    if(destinationUids.size()>0){
                        //SharedPreferences settings = Objects.requireNonNull(getContext()).getSharedPreferences("sinabro",0);
                        SharedPreferences settings = context.getSharedPreferences("sinabro",0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("badgeLove", destinationUids.size());
                        editor.apply(); // 뱃지 쉐어드 생성
                        textView.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }else{
                        textView.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.GONE);
                    }


                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        public void sortLovelist()
        {
            // 스탬프에 따라 정렬하기
            if(destinationUids.size() == destinationName.size())
                if(destinationUids.size() == destinationImages.size())
                    if(destinationImages.size() == destinationName.size()){
                        String  copyImage;
                        String  copyName;
                        String  copyUid;
                        String  copytimestemp;

                        int size = destinationUids.size();
                        for (int m = 0; m < size - 1; m++) {
                            for (int k = (m + 1); k < size; k++) {
                                if (timestemps.get(m)
                                        .compareTo(timestemps.get(k)) < 0) {
                                    copyImage = destinationImages.get(m);
                                    destinationImages.add(m, destinationImages.get(k));
                                    destinationImages.remove(m + 1);
                                    destinationImages.add(k, copyImage);
                                    destinationImages.remove(k + 1);

                                    copyName = destinationName.get(m);
                                    destinationName.add(m, destinationName.get(k));
                                    destinationName.remove(m + 1);
                                    destinationName.add(k, copyName);
                                    destinationName.remove(k + 1);

                                    copyUid = destinationUids.get(m);
                                    destinationUids.add(m, destinationUids.get(k));
                                    destinationUids.remove(m + 1);
                                    destinationUids.add(k, copyUid);
                                    destinationUids.remove(k + 1);

                                    copytimestemp = timestemps.get(m);
                                    timestemps.add(m, timestemps.get(k));
                                    timestemps.remove(m + 1);
                                    timestemps.add(k, copytimestemp);
                                    timestemps.remove(k + 1);
                                }
                            }
                        }
                    }
        }

        @Override
        public int getCount() {
            return destinationUids.size();
        }

        @Override
        public Object getItem(int position) {
            return destinationUids.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.item_love, parent, false);
            }

            ImageView imageView = convertView.findViewById(R.id.love_imageview);
            TextView  textView  = convertView.findViewById(R.id.love_name);
            TextView  textView_time = convertView.findViewById(R.id.love_time);


            if(timestemps.size() > position){

                long nowTime = System.currentTimeMillis();
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                Date nowDate = new Date(nowTime);
                String getTime = simpleDateFormat.format(nowDate);

                String nowYear = getTime.substring(0, 4);
                String nowMonth = getTime.substring(5, 7);
                String nowDay = getTime.substring(8, 10);
                int nowDayInt = Integer.parseInt(nowDay);

                String lastTime = timestemps.get(position);
                String lastYear = lastTime.substring(0, 4);
                String lastMonth = lastTime.substring(5, 7);
                String lastDay = lastTime.substring(8, 10);
                String lastHour = lastTime.substring(11, 13);
                String lastMin = lastTime.substring(14, 16);
                int hourInt = Integer.parseInt(lastHour);
                int minInt = Integer.parseInt(lastMin);
                int dayInt = Integer.parseInt(lastDay);
                int monthInt = Integer.parseInt(lastMonth);

                if (nowYear.equals(lastYear)) {
                    if (nowMonth.equals(lastMonth) && nowDay.equals(lastDay)) {
                        if (hourInt < 12){
                            if(minInt <10)
                                textView_time.setText("오전 " + hourInt + ":0" + minInt);
                            else
                                textView_time.setText("오전 " + hourInt + ":" + minInt);
                        }
                        else if(hourInt == 12){
                            if(minInt <10)
                                textView_time.setText("오후 " + hourInt + ":0" + minInt);
                            else
                                textView_time.setText("오후 " + hourInt + ":" + minInt);
                        }else{
                            if(minInt <10)
                                textView_time.setText("오후 " + (hourInt -12) + ":0" + minInt);
                            else
                                textView_time.setText("오후 " + (hourInt -12)+ ":" + minInt);
                        }
                    } else if (nowMonth.equals(lastMonth) && (dayInt + 1 == nowDayInt))
                        textView_time.setText("어제");
                    else
                        textView_time.setText(monthInt + "월 " + dayInt + "일");
                } else {
                    textView_time.setText(lastTime.substring(0, 10));
                }
            }

            if(destinationName.size() > position)
                textView.setText(destinationName.get(position));

            if(destinationImages.size() > position){
                if(!destinationImages.get(position).equals("")){
                    Glide.with(Objects.requireNonNull(getContext()))
                            .load(destinationImages.get(position))
                            .apply(new RequestOptions().centerCrop())
                            .into(imageView);
                }else{
                    imageView.setImageResource(R.drawable.usernonimage);
                }
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ProposeReciveActivity.class);
                        intent.putExtra("destinationUid", destinationUids.get(position));
                        startActivity(intent);
                    }

                }); // 개별 아이템 클릭 시 해당 아이템의 users 정보를 넘겨준다.
            return convertView;
        }
    }

}
