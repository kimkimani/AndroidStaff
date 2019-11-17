package ydkim2110.com.androidbarberstaffapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Interface.IRecyclerItemSelectedListener;
import ydkim2110.com.androidbarberstaffapp.Model.City;
import ydkim2110.com.androidbarberstaffapp.R;
import ydkim2110.com.androidbarberstaffapp.SalonListActivity;

public class MyStateAdapter extends RecyclerView.Adapter<MyStateAdapter.MyViewHolder> {

    private static final String TAG = MyStateAdapter.class.getSimpleName();

    private Context mContext;
    private List<City> mCityList;

    public MyStateAdapter(Context context, List<City> cityList) {
        mContext = context;
        mCityList = cityList;
    }

    int lastPosition = -1;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_state, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_state_name.setText(mCityList.get(position).getName());

        setAnimation(holder.itemView, position);

        holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {
                Common.state_name = mCityList.get(position).getName();
                mContext.startActivity(new Intent(mContext, SalonListActivity.class));
            }
        });
    }

    private void setAnimation(View itemView, int position) {
        Log.d(TAG, "setAnimation: called!!");

        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext,
                    android.R.anim.slide_in_left);
            itemView.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return (mCityList != null) ? mCityList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_state_name)
        TextView txt_state_name;

        private IRecyclerItemSelectedListener mIRecyclerItemSelectedListener;

        public void setIRecyclerItemSelectedListener(IRecyclerItemSelectedListener IRecyclerItemSelectedListener) {
            mIRecyclerItemSelectedListener = IRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mIRecyclerItemSelectedListener.onItemSelected(v, getAdapterPosition());
        }
    }
}
