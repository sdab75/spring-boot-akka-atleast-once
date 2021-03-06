package com.ms.abc.service;

import com.ms.common.AtleastOnceEventSender;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 11/30/2015.
 */
@Component
@Scope("prototype")
public class AbcToDefEventSender extends AtleastOnceEventSender {
    @Override
    public String persistenceId() { return "AbcToDefEventSender"; }

    @Override
    protected String[] getShardingRegion() {
        String[] regions={"abcToDefEventSenderShardRegion"};
        return regions ;
    }


    @Override
    protected String getEventSenderCB() {
        return "abcToDefAtleastOnceEventSenderCB";
    }

    @Override
    protected String actorName() {
        return "AbcToDefEventSender";
    }
}
