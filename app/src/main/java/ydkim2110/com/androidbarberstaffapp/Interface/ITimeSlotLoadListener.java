package ydkim2110.com.androidbarberstaffapp.Interface;

import java.util.List;

import ydkim2110.com.androidbarberstaffapp.Model.BookingInfomation;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<BookingInfomation> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();
}
