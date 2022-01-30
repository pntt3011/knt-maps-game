package com.example.maplogin.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class CombinedLiveData<T, K, S> extends MediatorLiveData<S> {
    public interface CombineFunction<T, K, S> {
        S combine(T source1, K source2);
    }

    private T data1;
    private K data2;

    public CombinedLiveData(LiveData<T> source1, LiveData<K> source2, CombineFunction<T, K, S> func) {
        super.addSource(source1, it -> {
            data1 = it;
            new Thread(() -> postValue(func.combine(data1, data2))).start();
        });
        super.addSource(source2, it -> {
            data2 = it;
            new Thread(() -> postValue(func.combine(data1, data2))).start();
        });
    }

    @Override
    public <S1> void addSource(@NonNull LiveData<S1> source, @NonNull Observer<? super S1> onChanged) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S1> void removeSource(@NonNull LiveData<S1> toRemote) {
        throw new UnsupportedOperationException();
    }
}
