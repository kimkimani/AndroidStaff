package ydkim2110.com.androidbarberstaffapp.Interface;

import java.util.List;

import ydkim2110.com.androidbarberstaffapp.Model.BarberServices;

public interface IBarberServicesLoadListener {

    void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList);
    void onBarberServicesLoadFailed(String message);

}
