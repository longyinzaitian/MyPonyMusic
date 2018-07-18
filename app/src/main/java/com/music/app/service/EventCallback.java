package com.music.app.service;


public interface EventCallback<T> {
    void onEvent(T t);
}
