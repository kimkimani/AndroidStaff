package ydkim2110.com.androidbarberstaffapp.Interface;

import java.util.List;

import ydkim2110.com.androidbarberstaffapp.Model.ShoppingItem;

public interface IShoppingDataLoadListener {
    void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList);
    void onShoppingDataLoadFailed(String message);
}
