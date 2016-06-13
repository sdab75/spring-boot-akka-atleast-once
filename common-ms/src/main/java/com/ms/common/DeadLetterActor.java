package com.ms.common;

import akka.actor.DeadLetter;
import akka.actor.UntypedActor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DeadLetterActor extends UntypedActor {
    public void onReceive(Object message) {
        if (message instanceof DeadLetter) {
            System.out.println("ABC ==>DeadLetterActor Message ===>"+message.toString());
        }
    }
}