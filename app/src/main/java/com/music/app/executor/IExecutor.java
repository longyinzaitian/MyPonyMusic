package com.music.app.executor;

/**
 * @author .
 * @param <T>
 */
public interface IExecutor<T> {
    /**
     * 执行
     */
    void execute();

    /**
     * 准备
     */
    void onPrepare();

    /**
     * 成功
     * @param t T
     */
    void onExecuteSuccess(T t);

    /**
     * 失败
     * @param e Exception
     */
    void onExecuteFail(Exception e);
}
