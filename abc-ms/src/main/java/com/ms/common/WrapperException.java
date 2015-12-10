package com.ms.common;

import java.util.Objects;

/**
 * Created by davenkat on 10/25/2015.
 */
public class WrapperException extends RuntimeException {

    Object obj;
    Throwable throwable;
    public WrapperException(String msg, Object obj, Throwable throwable) {
        super(msg,throwable);
        this.obj=obj;
    }
    public Object getObj(){
        return this.obj;
    }
}
