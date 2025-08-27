package com.example.stattube;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChannelViewModel extends ViewModel {
    private final MutableLiveData<DeepAnalysis.ChannelData> channelData = new MutableLiveData<>();

    public void setChannelData(DeepAnalysis.ChannelData data) {
        channelData.setValue(data);
    }

    public LiveData<DeepAnalysis.ChannelData> getChannelData() {
        return channelData;
    }
}