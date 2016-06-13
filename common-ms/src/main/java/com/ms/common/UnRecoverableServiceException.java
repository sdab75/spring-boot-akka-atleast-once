package com.ms.common;

/**
 * Created by davenkat on 12/24/2015.
 */
public class UnRecoverableServiceException extends RuntimeException{
    public UnRecoverableServiceException(String error){
        super(error);
    }
}
