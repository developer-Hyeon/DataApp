package sinabro.today.fragment;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import sinabro.today.R;
import sinabro.today.adapter.ProfileAdapter;
import sinabro.today.model.MyItem;
import sinabro.today.model.UserModel;
import sinabro.today.profile.ProfileActivity;
import sinabro.today.propose.ProposeProfileActivity;
import sinabro.today.setting.MapSettingActivity;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.checkSelfPermission;

public class MapingFragment extends Fragment implements OnMapReadyCallback, ClusterManager.OnClusterClickListener<MyItem>, ClusterManager.OnClusterItemClickListener<MyItem>{

    private View v;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FirebaseAuth mAuth;

    private GoogleMap mMap;
    private FirebaseUser user;
    private ClusterManager<MyItem> clusterManager;
    private List<MyItem> items = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private ImageButton fab;
    private DatabaseReference mDatabase;
    private MapView mapView;
    private Context context;
    private List<String> block_user_man;
    private int m_tmp=0;
    private Activity activity;
    private String photourl = null;
    private int what_sex;
    private String my_sex;
    private String my_uid;
    private Double my_latitude;
    private Double my_longtitude;
    private String my_userName;
    private String my_photo;
    private String man_photo;
    private String woman_photo;
    private MyItem my_item;
    private int what_switch;
    private ImageButton SearchView_btn;
    private SearchView searchView;
    private ImageButton search_cancel;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private int mapsetting;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String locationProvider;
    private String myuid;


    public MapingFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("namgungfragment","MapingFragment 실행됨");
        v = inflater.inflate(R.layout.fragment_map, container, false);
        assert container != null;
        context = container.getContext();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        SharedPreferences sf = context.getSharedPreferences("sinabro",MODE_PRIVATE);
        what_switch = sf.getInt("switch",2);

        SharedPreferences sf2 = context.getSharedPreferences("sinabro",MODE_PRIVATE);
        what_sex = sf2.getInt("who",0);


        mapView = v.findViewById(R.id.googleMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        fab = v.findViewById(R.id.fab); // 새로고침
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLastLocationButtonClicked(); // 위치 갱신
                Log.e("눌림","눌림");
            }
        });

        SearchView_btn = v.findViewById(R.id.searchViewButton);
        searchView = v.findViewById(R.id.searchView2);
        search_cancel  = v.findViewById(R.id.search_cancel);

        search_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setVisibility(View.VISIBLE);
                SearchView_btn.setVisibility(View.VISIBLE);
                searchView.setVisibility(View.INVISIBLE);
                search_cancel.setVisibility(View.INVISIBLE);
            }
        });

        SearchView_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setVisibility(View.INVISIBLE);
                SearchView_btn.setVisibility(View.INVISIBLE);
                searchView.setVisibility(View.VISIBLE);
                search_cancel.setVisibility(View.VISIBLE);

                searchView.setSubmitButtonEnabled(true);
                searchView.onActionViewExpanded();

                //SearchView의 검색 이벤트
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                    //검색버튼을 눌렀을 경우
                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        String address = query;
                        List<Address> addressList = null;

                        if(!TextUtils.isEmpty(address)){
                            Geocoder geocoder = new Geocoder(getContext());
                            try {
                                addressList = geocoder.getFromLocationName(address,6);
                                if(addressList != null){
                                    for(int i=0; i<addressList.size(); i++){
                                        fab.setVisibility(View.VISIBLE);
                                        SearchView_btn.setVisibility(View.VISIBLE);
                                        searchView.setVisibility(View.INVISIBLE);
                                        search_cancel.setVisibility(View.INVISIBLE);
                                        Address userAddress = addressList.get(i);
                                        LatLng latLng = new LatLng(userAddress.getLatitude(),userAddress.getLongitude());
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
                                    }
                                }else{
                                    startToast("위치 정보가 잘못되었습니다.");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            //위치를 입력하지 않음
                        }
                        return true;
                    }

                    //텍스트가 바뀔때마다 호출
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return true;
                    }
                });
            }
        });
        DBloading2();
        return v;
    } // onCreateView();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if (context instanceof Activity)
            activity = (Activity) context;
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onPause() {
        super.onPause();
        if(locationListener!=null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // 사용자의 위치 수신을 위한 세팅 //
        settingGPS();
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
            mDatabase.child("man_location").child(user.getUid()).removeValue();
            mDatabase.child("woman_location").child(user.getUid()).removeValue();
            SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("switch", 0);
            editor.apply();

        }else {
            if (ContextCompat. checkSelfPermission(Objects.requireNonNull(getActivity()),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
                        , LOCATION_PERMISSION_REQUEST_CODE);  // 권한 승낙/거부 다이얼로그 뜨게
            } else{
                DBloading();
            }
        }
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myuid = user.getUid();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) { // 초기 화면

        mMap  = googleMap;
        mMap.setMaxZoomPreference(14.0f);

        clusterManager = new ClusterManager<>(Objects.requireNonNull(getContext()), mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        clusterManager.setRenderer(new CustomCluster(getContext(),mMap,clusterManager)); // 커스텀 클러스터링
        clusterManager.setOnClusterClickListener(this); // 클러스트 눌렀을때 리스너
        clusterManager.setOnClusterItemClickListener(this); // 개별 마커 눌렀을때 리스너
    }

    private void locate_setting(){ // 나의 위치 업로드!!
        ClusterInit();


        SharedPreferences sf = context.getSharedPreferences("sinabro",MODE_PRIVATE);
        what_sex = sf.getInt("who",0);


        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel myModel = dataSnapshot.getValue(UserModel.class);
                assert myModel != null;
                my_sex = myModel.getSex();
                my_userName = user.getDisplayName();
                my_uid = user.getUid();

                mDatabase.child("users").child(Objects.requireNonNull(user.getUid())).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.child("image").exists()) {
                                        my_photo = snapshot.child("image").getValue(String.class);
                                    } else {
                                        my_photo = null;
                                    }
                                    Location location = getMyLocation();
                                    if(location != null ) {
                                        final LatLng myLocation = new LatLng(Objects.requireNonNull(location).getLatitude(), location.getLongitude()); // 현재 나의 위치값
                                        final UserModel userModel = new UserModel();
                                        userModel.setLatitude(location.getLatitude());
                                        userModel.setLongitude(location.getLongitude());
                                        userModel.setUserName(user.getDisplayName());
                                        my_latitude = location.getLatitude();
                                        my_longtitude = location.getLongitude();
                                        LatLng my_lat = new LatLng(my_latitude, my_longtitude);
                                        my_item = new MyItem(my_lat, my_sex, my_userName, my_uid, my_photo);


                                        if (my_sex.equals("남자")) {
                                            mDatabase.child("man_location").child(user.getUid()).setValue(userModel)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation)); // 사용자 중심으로 카메라 이동
                                                            mMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));

                                                            SharedPreferences sharedPreferences =  context.getSharedPreferences("sinabro",MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.putString("my_latitude", String.valueOf(userModel.getLatitude())); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐
                                                            editor.putString("my_longitude", String.valueOf(userModel.getLongitude()));
                                                            editor.putString("my_sex", "남자");
                                                            editor.apply();

                                                            if (what_sex == 0) { // 전체 보여주기
                                                                man_download();
                                                                woman_download();
                                                                items.add(my_item);
                                                                clusterManager.addItems(items);
                                                                clusterManager.cluster();
                                                            } else if (what_sex == 1) { // 남자만 보여주기
                                                                man_download();
                                                                items.add(my_item);
                                                                clusterManager.addItems(items);
                                                                clusterManager.cluster();
                                                            } else if (what_sex == 2) { // 여자만 보여주기
                                                                woman_download();
                                                                items.add(my_item);
                                                                clusterManager.addItems(items);
                                                                clusterManager.cluster();
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Write failed
                                                            // ...
                                                        }
                                                    });
                                        } else { // 내가 여자일 때
                                            mDatabase.child("woman_location").child(user.getUid()).setValue(userModel)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation)); // 사용자 중심으로 카메라 이동
                                                            mMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));

                                                            SharedPreferences sharedPreferences =  context.getSharedPreferences("sinabro",MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.putString("my_latitude", String.valueOf(userModel.getLatitude())); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐
                                                            editor.putString("my_longitude", String.valueOf(userModel.getLongitude()));
                                                            editor.putString("my_sex", "여자");
                                                            editor.apply();

                                                            if (what_sex == 0) { // 전체 보여주기
                                                                man_download();
                                                                woman_download();
                                                                items.add(my_item);
                                                                clusterManager.addItems(items);
                                                                clusterManager.cluster();
                                                            } else if (what_sex == 1) { // 남자만 보여주기
                                                                man_download();
                                                                items.add(my_item);
                                                                clusterManager.addItems(items);
                                                                clusterManager.cluster();
                                                            } else if (what_sex == 2) { // 여자만 보여주기
                                                                woman_download();
                                                                items.add(my_item);
                                                                clusterManager.addItems(items);
                                                                clusterManager.cluster();
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Write failed
                                                            // ...
                                                        }
                                                    });
                                        }

                                    } else{
                                        startToast("GPS정보를 불러오지 못합니다. 잠시후 재시도해주세요.");
                                        fab.setEnabled(true);
                                    }
                                    break;
                                } // for
                            }
                        } else{
                            my_photo = null;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    public void man_download(){
        ClusterInit();
        block_user_man = new ArrayList<>();


        mDatabase.child("users").child(user.getUid()).child("blockedList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    block_user_man.add(snapshot.getKey());
                }

                mDatabase.child("users").child(user.getUid()).child("blockList").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            block_user_man.add(snapshot.getKey());
                        }

                        mDatabase.child("man_location").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    for (int i = 0; i < block_user_man.size(); i++) {
                                        if (Objects.equals(snapshot.getKey(), block_user_man.get(i))) {
                                            m_tmp = 1;

                                            break;
                                        }
                                    } // for

                                    if (m_tmp == 0) {
                                        final UserModel userModel = snapshot.getValue(UserModel.class);
                                        final LatLng lat = new LatLng(Objects.requireNonNull(userModel).getLatitude(),userModel.getLongitude());
                                        Objects.requireNonNull(userModel).setUid(snapshot.getKey());
                                        userModel.setSex("남자");

                                        mDatabase.child("users").child(Objects.requireNonNull(snapshot.getKey())).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        if (snapshot.child("image").exists()) {
                                                            man_photo = snapshot.child("image").getValue(String.class);

                                                        } else {
                                                            man_photo = null;
                                                        }
                                                        break;
                                                    } // for
                                                } else {
                                                    man_photo = null;
                                                }

                                                if(!(snapshot.getKey().equals(user.getUid()))){ // 나일 때
                                                    MyItem myItem = new MyItem(lat, userModel.getSex(), userModel.getUserName(),userModel.getUid(),man_photo);
                                                    items.add(myItem);
                                                    clusterManager.addItems(items);
                                                    clusterManager.cluster();
                                                    man_photo = null;
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    m_tmp = 0;
                                    fab.setEnabled(true);
                                } // for
                            } // onDataChange()

                            @Override
                            public void onCancelled(@NotNull DatabaseError databaseError) {

                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });
    } // download()

    public void woman_download(){
        ClusterInit();
        block_user_man = new ArrayList<>();

        mDatabase.child("users").child(user.getUid()).child("blockedList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    block_user_man.add(snapshot.getKey());
                }

                mDatabase.child("users").child(user.getUid()).child("blockList").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            block_user_man.add(snapshot.getKey());
                        }

                        mDatabase.child("woman_location").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    for (int i = 0; i < block_user_man.size(); i++) {
                                        if (Objects.equals(snapshot.getKey(), block_user_man.get(i))) {
                                            m_tmp = 1;
                                            break;
                                        }
                                    } // for

                                    if (m_tmp == 0) {
                                        final UserModel userModel = snapshot.getValue(UserModel.class);
                                        final LatLng lat = new LatLng(Objects.requireNonNull(userModel).getLatitude(),userModel.getLongitude());
                                        Objects.requireNonNull(userModel).setUid(snapshot.getKey());
                                        userModel.setSex("여자");

                                        mDatabase.child("users").child(Objects.requireNonNull(snapshot.getKey())).child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        if (snapshot.child("image").exists()) {
                                                            woman_photo = snapshot.child("image").getValue(String.class);

                                                        } else {
                                                            woman_photo = null;
                                                        }
                                                        break;
                                                    } // for
                                                } else {
                                                    woman_photo = null;
                                                }

                                                if(!(snapshot.getKey().equals(user.getUid()))){ // 나일 때
                                                    MyItem myItem = new MyItem(lat, userModel.getSex(), userModel.getUserName(),userModel.getUid(),woman_photo);
                                                    items.add(myItem);
                                                    clusterManager.addItems(items);
                                                    clusterManager.cluster();
                                                    woman_photo = null;
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    m_tmp = 0;
                                    fab.setEnabled(true);
                                } // for
                            } // onDataChange()

                            @Override
                            public void onCancelled(@NotNull DatabaseError databaseError) {

                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });

    } // download()

    @Override
    public boolean onClusterClick(Cluster<MyItem> cluster) { // 1. 클러스터 누를떄 프로필 이미지 뜨게 처리하는 함수 -> 리사이클러뷰 -> 프로필 창
        Log.e("onClusterClick", String.valueOf(cluster.getSize()));

        List<MyItem> clusterList = new ArrayList<>(cluster.getItems());

        recyclerView = v.findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        recyclerView.setVisibility(View.VISIBLE);
        profileAdapter = new ProfileAdapter(clusterList,getContext(),getActivity());
        recyclerView.setAdapter(profileAdapter);

        return false;
    }

    @Override
    public boolean onClusterItemClick(final MyItem myItem) {    // -> 2. 개별 마커 누를때 이벤트 함수 -> 프로필 창

        LatLng they_location = myItem.getPosition();
        final Double they_latitude = they_location.latitude;
        final Double they_longitude = they_location.longitude;

        user = mAuth.getCurrentUser();
        if(myItem.getUid().equals(user.getUid())){
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            activity.startActivity(intent);
        }
        else{
            if(what_switch == 1){
                Intent intent = new Intent(getContext(), ProposeProfileActivity.class);
                intent.putExtra("destinationUid", myItem.getUid());
                intent.putExtra("latitude", they_latitude);
                intent.putExtra("longitude", they_longitude);

                ActivityOptions activityOptions = null;
                activityOptions = ActivityOptions.makeCustomAnimation(getContext(), R.anim.fromright, R.anim.toleft);
                activity.startActivity(intent,activityOptions.toBundle());

            }else{
                Log.d("namgung","나의 위치를 정보를 키셔야 상대방 프로필을 볼 수 있습니다.");
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getContext());
                LayoutInflater inflater = getLayoutInflater();
                View alertView = inflater.inflate(R.layout.switchsetting_dialog, null);
                alertDialogBuilder.setView(alertView);
                // 다이얼로그 생성
                final AlertDialog alertDialog = alertDialogBuilder.create();
                final TextView textView_no = alertView.findViewById(R.id.noswitchsetting);
                textView_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();
                    }
                });
                final TextView textView_yes = alertView.findViewById(R.id.goswitchsetting);
                textView_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = null;
                        intent = new Intent(getContext(), MapSettingActivity.class);
                        ActivityOptions activityOptions = null;
                        activityOptions = ActivityOptions.makeCustomAnimation(getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                        alertDialog.dismiss();
                    }
                });
                // 다이얼로그 보여주기
                alertDialog.show();
            }

        }
        return false;
    }


    public class CustomCluster extends  DefaultClusterRenderer<MyItem>{ // 커스텀 마커 클러스터링

        private CustomCluster(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) { // 마커 옵션
//       super.onBeforeClusterItemRendered(item, markerOptions);

            if(item.getUid().equals(user.getUid())){
                markerOptions.icon(getMarkerIcon("#FF0000"));

            } else{
                if(item.getSex().equals("남자")){
                    markerOptions.icon(getMarkerIcon("#865FF8"));
                }
                else{
                    markerOptions.icon(getMarkerIcon("#865FF8"));
                }
            }
        }

        BitmapDescriptor getMarkerIcon(String color) {
            float[] hsv = new float[3];
            Color.colorToHSV(Color.parseColor(color), hsv);
            return BitmapDescriptorFactory.defaultMarker(hsv[0]);
        }


        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }

        @Override
        protected int getColor(int clusterSize) { // 클러스터 색상

            return Color. parseColor ("#865FF8");    // Set the colour of the cluster.

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_ENABLE_REQUEST_CODE) {//사용자가 GPS 활성 시켰는지 검사
            if (checkLocationServicesStatus()) {
                if (checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
                            , LOCATION_PERMISSION_REQUEST_CODE);  // 권한 승낙/거부 다이얼로그 뜨게

                    SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("switch", 0);
                    editor.apply();
                } else {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("switch", 1);
                    editor.apply();
                    DBloading2();
                }
            } else {
                startToast("GPS를 키셔야 이용 가능합니다.");
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "권한을 허락하셔야 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("switch", 0);
                editor.apply();
            } else {
                if (checkLocationServicesStatus()) {
                    DBloading2();
                    SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("switch", 1);
                    editor.apply();
                } else {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("switch", 0);
                    editor.apply();
                }
            }
        }
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(getContext(), c);
        startActivity(intent);
    }

    private void startToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void ClusterInit(){
        items.removeAll(items);
        if(mMap !=null) {
            mMap.clear();
            clusterManager = new ClusterManager<>(Objects.requireNonNull(getContext()), mMap);
            mMap.setOnCameraIdleListener(clusterManager);
            mMap.setOnMarkerClickListener(clusterManager);
            clusterManager.setRenderer(new CustomCluster(getActivity(),mMap,clusterManager)); // 커스텀 클러스터링
            clusterManager.setOnClusterClickListener(MapingFragment.this); // 클러스트 눌렀을때 리스너
            clusterManager.setOnClusterItemClickListener(MapingFragment.this); // 개별 마커 눌렀을때 리스너
        }
    }

    // 버튼 누를때마다 위치 갱신하는 로직
    private void onLastLocationButtonClicked() {

        ClusterInit();
        recyclerView = v.findViewById(R.id.profileRecyclerView);
        recyclerView.setVisibility(View.INVISIBLE);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
            mDatabase.child("man_location").child(user.getUid()).removeValue();
            mDatabase.child("woman_location").child(user.getUid()).removeValue();
            SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("switch", 0);
            editor.apply();
        }else {
            if (ContextCompat. checkSelfPermission(Objects.requireNonNull(getActivity()),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
                        , LOCATION_PERMISSION_REQUEST_CODE);  // 권한 승낙/거부 다이얼로그 뜨게
            } else{
                DBloading2();
                Log.e("권한","DBloading2 실행");
            }
        }
    }




    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        return Objects.requireNonNull(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }



    private void showDialogForLocationServiceSetting() { // onStart시 호출
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                Objects.requireNonNull(getContext()));
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.location_dialog, null);
        alertDialogBuilder.setView(alertView);
        // 다이얼로그 생성
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final TextView textView_no = alertView.findViewById(R.id.location_cancel);
        textView_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        final TextView textView_yes = alertView.findViewById(R.id.location_ok);
        textView_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callGPSSettingIntent
                        = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                alertDialog.dismiss();
            }
        });
        // 다이얼로그 보여주기
        alertDialog.show();
    }

    private void DBloading(){


        SharedPreferences sf = context.getSharedPreferences("sinabro",MODE_PRIVATE);
        what_switch = sf.getInt("switch",2);

        SharedPreferences sf2 = context.getSharedPreferences("sinabro",MODE_PRIVATE);
        what_sex = sf2.getInt("who",0);


        SharedPreferences sf3 = context.getSharedPreferences("sinabro",MODE_PRIVATE);
        mapsetting = sf3.getInt("mapsetting",0);

        if(mapsetting==1) {
            Log.e("맵세팅", "바꿈");

            if (what_switch == 1) {
                fab.setEnabled(false);
                locate_setting();
            } else if (what_switch == 0) {
                if (mMap != null) {
                    LatLng latLng = new LatLng(37.498032, 127.028053);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); // 사용자 중심으로 카메라 이동
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
                }

                if (what_sex == 0) {
                    fab.setEnabled(false);
                    man_download();
                    woman_download();
                } else if (what_sex == 1) {
                    fab.setEnabled(false);
                    man_download();
                } else if (what_sex == 2) {
                    fab.setEnabled(false);
                    woman_download();
                }
            } else {
                SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("switch", 1); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐)
                editor.apply();
                fab.setEnabled(false);
                locate_setting();
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("mapsetting", 0);
            editor.apply();
            recyclerView = v.findViewById(R.id.profileRecyclerView);
            recyclerView.setVisibility(View.INVISIBLE);
        } // mapsetting을 갔다와서 스위치나 성별을 바꿨을때

        else{ // 안바꿨을때는 그대로 냅둠
            Log.e("맵세팅","안바꿈");
        }

    }

    private void DBloading2(){


        SharedPreferences sf = context.getSharedPreferences("sinabro",MODE_PRIVATE);
        what_switch = sf.getInt("switch",2);

        SharedPreferences sf2 = context.getSharedPreferences("sinabro",MODE_PRIVATE);
        what_sex = sf2.getInt("who",0);


        if(what_switch==1){
            fab.setEnabled(false);
            locate_setting();
            Log.e("switch","1, locate_Setting");
        } else if(what_switch==0){
            if(mMap!=null) {
                LatLng latLng = new LatLng(37.498032, 127.028053);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); // 사용자 중심으로 카메라 이동
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
            }
            if(what_sex == 0){
                fab.setEnabled(false);
                man_download();
                woman_download();
                Log.e("switch","0, man,woman");
            } else if(what_sex ==1){
                fab.setEnabled(false);
                man_download();
                Log.e("switch","0, man");
            } else if(what_sex == 2){
                fab.setEnabled(false);
                woman_download();
                Log.e("switch","0, woman");
            }
        }else{
            SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("switch", 1); // 켜짐 , 0은 꺼짐 -> 권한 허용하면 스위치 온으로, dafault는 0(꺼짐)
            editor.apply();
            fab.setEnabled(false);
            locate_setting();

        }
    }

    /**
     * 사용자의 위치를 수신
     */
    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            // 수동으로 위치 구하기
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationProvider = LocationManager.NETWORK_PROVIDER;
                Log.e("test1","network_provider");
            } else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                locationProvider = LocationManager.GPS_PROVIDER;
                Log.e("test1","gps_provider");
            }
            currentLocation = locationManager.getLastKnownLocation(locationProvider);

        }
        return currentLocation;
    }



    /**
     * GPS 를 받기 위한 매니저와 리스너 설정
     */
    private void settingGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.e("location","onLocationChanged");
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
                Log.e("test1", provider);
                if(provider.equals("gps")){
                    SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("switch", 1);
                    editor.apply();
                } else if(provider.equals("network")){
                    SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("switch", 1);
                    editor.apply();
                }
            }

            public void onProviderDisabled(String provider) {
                Log.e("test2",provider);
                if(provider.equals("gps")){
                    mDatabase.child("man_location").child(user.getUid()).removeValue();
                    mDatabase.child("woman_location").child(user.getUid()).removeValue();
                    SharedPreferences sharedPreferences = context.getSharedPreferences("sinabro",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("switch", 0);
                    editor.apply();
                    recyclerView = v.findViewById(R.id.profileRecyclerView);
                    recyclerView.setVisibility(View.INVISIBLE);
                    ClusterInit();
                    showDialogForLocationServiceSetting();
                }
            }
        };
    }




}