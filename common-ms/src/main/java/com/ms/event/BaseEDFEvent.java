package com.ms.event;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Base event class for EDF.
 */
public abstract class BaseEDFEvent implements EDFEvent {


    private String correlationId;
    private UUID eventId;
    private String tenantId;
    private String eventName; //the next step value, this could be current step or next step.
    private Date created;
    private String createdBy;
    private String sessionId;
    private String status;
    private APPSTATUS appStatus;
    private Map<?,?> params;

    public BaseEDFEvent() {

    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }


    @Override
    public Date getCreated() {
        return created;
    }


    @Override
    public void setCreated(Date created) {
        this.created = created;
    }


    @Override
    public String getCreatedBy() {
        return createdBy;
    }


    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    @Override
    public UUID getEventId() {
        return eventId;
    }


    @Override
    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }


    @Override
    public String getEventName() {
        return eventName;
    }


    @Override
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }


    @Override
    public String getTenantId() {
        return tenantId;
    }


    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public APPSTATUS getAppStatus() {
        return appStatus;
    }

    @Override
    public void setAppStatus(APPSTATUS appStatus) {
        this.appStatus = appStatus;
    }

    public Map<?, ?> getParams() {
        return params;
    }

    public void setParams(Map<?, ?> params) {
        this.params = params;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BaseEDFEvent{");
        sb.append("appStatus=").append(appStatus);
        sb.append(", correlationId='").append(correlationId).append('\'');
        sb.append(", eventId=").append(eventId);
        sb.append(", tenantId='").append(tenantId).append('\'');
        sb.append(", eventName='").append(eventName).append('\'');
        sb.append(", created=").append(created);
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }
}
