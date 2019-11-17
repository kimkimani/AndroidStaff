package ydkim2110.com.androidbarberstaffapp.Interface;

import java.util.List;

import ydkim2110.com.androidbarberstaffapp.Model.City;

public interface IOnAllStateLoadListener {
    void onAllStateLoadSuccess(List<City> cityList);
    void onAllStateFailed(String message);
}
