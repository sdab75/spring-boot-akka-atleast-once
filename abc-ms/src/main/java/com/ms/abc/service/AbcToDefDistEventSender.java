package com.ms.abc.service;

import com.ms.common.DistEventSender;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 11/30/2015.
 */
@Component
@Scope("prototype")
public class AbcToDefDistEventSender extends DistEventSender {
    @Override
    protected String destinationPath() {
        return "/user/defEventListener";
    }

    @Override
    protected String senderAckPath() {
        return "/user/abcToDefEventSender";
    }
}
