package ydkim2110.com.androidbarberstaffapp.Interface;

import java.util.List;

import ydkim2110.com.androidbarberstaffapp.Model.Salon;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> branchList);
    void onBranchLoadFailed(String message);
}
