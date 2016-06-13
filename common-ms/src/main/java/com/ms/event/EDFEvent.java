package com.ms.event;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by davenkat on 11/13/2015.
 */
public interface EDFEvent extends Serializable {
    public enum STATUS {
        OPEN,
        INPROGRESS,
        CLOSED;

        public static STATUS toStatus(String value) {
            if (value.equals("OPEN")){
                return STATUS.OPEN;
            } else if (value.equals("INPROGRESS")){
                return STATUS.INPROGRESS;
            } else if (value.equals("CLOSED")){
                return STATUS.CLOSED;
            } else {
                return null;
            }
        }
    }

    public enum APPSTATUS {
        ASSIGNED,
        DRAFT,
        COMPLETE,
        PENDINGAPPROVAL,
        APPROVED,
        NOTSTARTED,
        ACQUIRED,
        NEEDSREVISION;

        public static APPSTATUS toAppStatus(String appStatus) {
            if (appStatus.equals("ASSIGNED")){
                return APPSTATUS.ASSIGNED;
            } else if (appStatus.equals("DRAFT")){
                return APPSTATUS.DRAFT;
            } else if (appStatus.equals("COMPLETE")){
                return APPSTATUS.COMPLETE;
            } else if (appStatus.equals("PENDINGAPPROVAL")){
                return APPSTATUS.PENDINGAPPROVAL;
            } else if (appStatus.equals("APPROVED")){
                return APPSTATUS.APPROVED;
            } else if (appStatus.equals("NOTSTARTED")){
                return APPSTATUS.NOTSTARTED;
            } else if (appStatus.equals("ACQUIRED")){
                return APPSTATUS.ACQUIRED;
            } else if (appStatus.equals("NEEDSREVISION")){
                return APPSTATUS.NEEDSREVISION;
            } else {
                return null;
            }
        }
    }

    String getCorrelationId();

    void setCorrelationId(String correlationId);

    String getEventType();

    Date getCreated();

    void setCreated(Date created);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    UUID getEventId();

    void setEventId(UUID eventId);

    String getEventName();

    void setEventName(String eventName);

    String getTenantId();

    void setTenantId(String tenantId);

    String getStatus();

    void setStatus(String status);

    APPSTATUS getAppStatus();

    void setAppStatus(APPSTATUS appStatus);

    String getSessionId();

    void setSessionId(String sessionId);

    UUID getModuleInstanceId();

    void setModuleInstanceId(UUID moduleInstanceId);

    String getWorkUnitId();

    void setWorkUnitId(String workUnitId);

    String getModuleId();

    void setModuleId(String moduleId);

    String getWorkUnitDescription();

    void setWorkUnitDescription(String workUnitDescription);

    String getModuleNumber();

    void setModuleNumber(String moduleNumber);

    long getEventDeliveryId();

    void setEventDeliveryId(long eventDeliveryId);

}
