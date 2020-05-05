package sinabro.today.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import sinabro.today.R;
import sinabro.today.model.BuyModel;
import sinabro.today.model.UserModel;

public class HeartAdapter extends RecyclerView.Adapter<HeartAdapter.ViewHolder> {

    private BuyModel buyModel;
    private Context context;
    private Activity activity;
    private List<BuyModel> buy_item;

    public HeartAdapter(List<BuyModel> buy_item, Context mContext, Activity activity) {
        this.buy_item = buy_item;
        this.context = mContext;
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        TextView date_heart;
        TextView detail_heart;
        TextView change_heart;
        TextView current_heart;

        public ViewHolder(final View itemView) {
            super(itemView);

            date_heart = itemView.findViewById(R.id.date_heart);
            detail_heart = itemView.findViewById(R.id.detail_heart);
            change_heart = itemView.findViewById(R.id.change_heart);
            current_heart = itemView.findViewById(R.id.current_heart);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_heart,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        buyModel = buy_item.get(position);

        final String date = buyModel.getBuys_date();
        final String detail = buyModel.getBuys_comment();
        final String change = buyModel.getBuys_change();
        final String current = buyModel.getCurrent_heart();

        holder.date_heart.setText(date.substring(0,10));
        holder.detail_heart.setText(detail);
        holder.change_heart.setText(change);
        holder.current_heart.setText(current);

    }

    @Override
    public int getItemCount() {
        return buy_item.size();
    }


}