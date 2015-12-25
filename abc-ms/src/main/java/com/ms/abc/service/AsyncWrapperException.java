package com.ms.abc.service;

/**
 * Created by davenkat on 10/25/2015.
 */
public class AsyncWrapperException extends RuntimeException {

    Object obj;
    Throwable throwable;
    public AsyncWrapperException(String msg, Object obj, Throwable throwable) {
        super(msg,throwable);
        this.obj=obj;
    }
    public Object getObj(){
        return this.obj;
    }
}
