package com.music.app.service;


/**
 * @author .
 * @param <T>
 */
public interface EventCallback<T> {
    /**
     * 事件
     * @param t T
     */
    void onEvent(T t);
}
