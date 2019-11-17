package ydkim2110.com.androidbarberstaffapp.Adapter;

import android.content.Context;
import android.database.DatabaseUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ydkim2110.com.androidbarberstaffapp.Common.Common;
import ydkim2110.com.androidbarberstaffapp.Interface.IOnShoppingItemSelected;
import ydkim2110.com.androidbarberstaffapp.Interface.IRecyclerItemSelectedListener;
import ydkim2110.com.androidbarberstaffapp.Model.ShoppingItem;
import ydkim2110.com.androidbarberstaffapp.R;

public class MyShoppingItemAdapter extends RecyclerView.Adapter<MyShoppingItemAdapter.MyViewHolder> {

    private static final String TAG = MyShoppingItemAdapter.class.getSimpleName();

    private Context mContext;
    private List<ShoppingItem> mShoppingItemList;
    private IOnShoppingItemSelected mIOnShoppingItemSelected;

    public MyShoppingItemAdapter(Context context, List<ShoppingItem> shoppingItemList, IOnShoppingItemSelected iOnShoppingItemSelected) {
        mContext = context;
        mShoppingItemList = shoppingItemList;
        this.mIOnShoppingItemSelected = iOnShoppingItemSelected;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_shopping_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(mShoppingItemList.get(position).getImage()).into(holder.img_shopping_item);

        holder.txt_shopping_item_name.setText(
                Common.formatShoppingItemName(mShoppingItemList.get(position).getName()));
        holder.txt_shopping_item_price.setText(
                new StringBuilder("$").append(mShoppingItemList.get(position).getPrice().toString()));

        // Add to cart item Staff app
        holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {
                mIOnShoppingItemSelected.onShoppingItemSelected(mShoppingItemList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mShoppingItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txt_shopping_item_name;
        private TextView txt_shopping_item_price;
        private TextView txt_add_to_cart;
        private ImageView img_shopping_item;

        private IRecyclerItemSelectedListener mIRecyclerItemSelectedListener;

        public void setIRecyclerItemSelectedListener(IRecyclerItemSelectedListener IRecyclerItemSelectedListener) {
            mIRecyclerItemSelectedListener = IRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            img_shopping_item = itemView.findViewById(R.id.img_shopping_item);
            txt_shopping_item_name = itemView.findViewById(R.id.txt_name_shopping_item);
            txt_shopping_item_price = itemView.findViewById(R.id.txt_price_shopping_item);
            txt_add_to_cart = itemView.findViewById(R.id.txt_add_to_cart);

            txt_add_to_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mIRecyclerItemSelectedListener.onItemSelected(v, getAdapterPosition());
        }
    }
}
