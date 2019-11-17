package ydkim2110.com.androidbarberstaffapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ydkim2110.com.androidbarberstaffapp.Common.MyDiffCallback;
import ydkim2110.com.androidbarberstaffapp.Model.MyNotification;
import ydkim2110.com.androidbarberstaffapp.R;

public class MyNotificationAdapter extends RecyclerView.Adapter<MyNotificationAdapter.MyViewHolder> {

    private static final String TAG = MyNotificationAdapter.class.getSimpleName();

    private Context mContext;
    private List<MyNotification> mMyNotificationList;

    public MyNotificationAdapter(Context context, List<MyNotification> myNotificationList) {
        mContext = context;
        mMyNotificationList = myNotificationList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_notification_item, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_notification_title.setText(mMyNotificationList.get(position).getTitle());
        holder.txt_notification_content.setText(mMyNotificationList.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return mMyNotificationList.size();
    }

    public void updateList(List<MyNotification> newList) {
        Log.d(TAG, "updateList: called!!");

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffCallback(this.mMyNotificationList, newList));
        mMyNotificationList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_notification_title)
        TextView txt_notification_title;
        @BindView(R.id.txt_notification_content)
        TextView txt_notification_content;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
