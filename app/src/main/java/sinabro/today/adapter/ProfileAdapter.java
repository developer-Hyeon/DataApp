package sinabro.today.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import sinabro.today.R;
import sinabro.today.model.MyItem;
import sinabro.today.profile.ProfileActivity;
import sinabro.today.propose.ProposeProfileActivity;
import sinabro.today.setting.MapSettingActivity;
import static android.content.Context.MODE_PRIVATE;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private List<MyItem> Item;
    private Context context;
    private Activity activity;
    private MyItem myItem;
    private int what_switch;
    private String myuid;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    public ProfileAdapter(List<MyItem> myItem, Context mContext, Activity activity) {
        this.Item = myItem; // title, snippit , photo
        this.context = mContext;
        this.activity = activity;

    }

    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_list, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        myItem = Item.get(position);
        final String url =  myItem.getPhoto();
        final String uid = myItem.getUid();
        LatLng they_location = myItem.getPosition();
        final Double they_latitude = they_location.latitude;
        final Double they_longitude = they_location.longitude;

        if(url != null){
            Glide.with(context).asBitmap().load(url).into(holder.image);
        } else{
            holder.image.setImageResource(R.drawable.usernonimage);
        }

        SharedPreferences sf = context.getSharedPreferences("sinabro",MODE_PRIVATE);
        what_switch = sf.getInt("switch",2);
        Log.e("what_switch", String.valueOf(what_switch));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String myUid = FirebaseAuth.getInstance().getUid();

                if(what_switch==1) {
                    if (uid.equals(myUid)) { // 내 자신을 클릭
                        Intent intent = new Intent(view.getContext(), ProfileActivity.class);
                        ActivityOptions activityOptions = null;
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        activity.startActivity(intent, activityOptions.toBundle());

                    } else { // 다른 사람 클릭
                        Intent intent = new Intent(view.getContext(), ProposeProfileActivity.class);
                        intent.putExtra("destinationUid", uid);
                        intent.putExtra("where", "map");
                        intent.putExtra("latitude", they_latitude);
                        intent.putExtra("longitude", they_longitude);

                        ActivityOptions activityOptions = null;
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        activity.startActivity(intent, activityOptions.toBundle());
                    }
                } else{
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View alertView = inflater.inflate(R.layout.switchsetting_dialog, null);
                    alertDialogBuilder.setView(alertView);

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
                            intent = new Intent(context, MapSettingActivity.class);
                            ActivityOptions activityOptions = null;
                            activityOptions = ActivityOptions.makeCustomAnimation(context, R.anim.fromright, R.anim.toleft);
                            context.startActivity(intent, activityOptions.toBundle());

                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();


                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return Item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        CircleImageView image;

        public ViewHolder(final View itemView) {
            super(itemView);
            image =  itemView.findViewById(R.id.image_view);
        }
    }
}