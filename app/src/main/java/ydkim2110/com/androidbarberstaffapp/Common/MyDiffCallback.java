package ydkim2110.com.androidbarberstaffapp.Common;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import ydkim2110.com.androidbarberstaffapp.Model.MyNotification;

public class MyDiffCallback extends DiffUtil.Callback {

    private List<MyNotification> oldList;
    private List<MyNotification> newList;

    public MyDiffCallback(List<MyNotification> oldList, List<MyNotification> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getUid() == newList.get(newItemPosition).getUid();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition) == newList.get(newItemPosition);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
