package com.ms.def.service;

import com.ms.common.AtleastOnceEventSender;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 11/30/2015.
 */
@Component
@Scope("prototype")
public class DefToAbcEventSender extends AtleastOnceEventSender {
    @Override
    public String persistenceId() {
        return "DefToAbcEventSender-" + getContext().parent().path().name();
    }

    @Override
    protected String destinationActorPath() {
        return "/user/defToAbcDistEventSender";
    }
}
