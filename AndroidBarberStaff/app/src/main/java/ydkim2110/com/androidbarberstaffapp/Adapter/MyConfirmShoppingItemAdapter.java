package ydkim2110.com.androidbarberstaffapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ydkim2110.com.androidbarberstaffapp.Model.CartItem;
import ydkim2110.com.androidbarberstaffapp.Model.ShoppingItem;
import ydkim2110.com.androidbarberstaffapp.R;

public class MyConfirmShoppingItemAdapter extends RecyclerView.Adapter<MyConfirmShoppingItemAdapter.MyViewHolder> {

    private static final String TAG = MyConfirmShoppingItemAdapter.class.getSimpleName();

    private Context mContext;
    private List<CartItem> mShoppingItemList;

    public MyConfirmShoppingItemAdapter(Context context, List<CartItem> shoppingItemList) {
        mContext = context;
        mShoppingItemList = shoppingItemList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_confirm_shopping, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get()
                .load(mShoppingItemList.get(position).getProductImage())
                .into(holder.item_image);
        holder.txt_name.setText(new StringBuilder(mShoppingItemList.get(position).getProductName())
                .append(" x")
                .append(mShoppingItemList.get(position).getProductQuantity()));
    }

    @Override
    public int getItemCount() {
        return mShoppingItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_image)
        ImageView item_image;
        @BindView(R.id.txt_name)
        TextView txt_name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
