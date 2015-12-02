package com.ms.def.service;

import com.ms.common.DistEventSender;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 11/30/2015.
 */
@Component
@Scope("prototype")
public class DefToAbcDistEventSender extends DistEventSender {
    @Override
    protected String destinationPath() {
        return "/user/abcEventListener";
    }

    @Override
    protected String senderAckPath() {
        return "/user/defToAbcEventSender";
    }
}
