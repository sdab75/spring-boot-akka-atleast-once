package com.ms.common;

/**
 * Created by davenkat on 12/24/2015.
 */
public class RecoverableServiceException extends RuntimeException{
    public RecoverableServiceException(String error){
        super(error);
    }
}
