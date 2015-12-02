package com.ms.event;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 *
 */
public abstract class AppEDFEvent extends BaseEDFEvent{


    @NotNull
    private UUID moduleInstanceId;
    @NotBlank
    private String workUnitId;
    @NotBlank
    private String moduleId;

    @NotBlank
    private String moduleNumber;

    @NotBlank
    private String workUnitDescription;

	private long eventDeliveryId;

	@Override
	public UUID getModuleInstanceId() {
		return moduleInstanceId;
	}
	@Override
	public void setModuleInstanceId(UUID moduleInstanceId) {
		this.moduleInstanceId = moduleInstanceId;
	}
	@Override
	public String getWorkUnitId() {
		return workUnitId;
	}
	@Override
	public void setWorkUnitId(String workUnitId) {
		this.workUnitId = workUnitId;
	}
	@Override
	public String getModuleId() {
		return moduleId;
	}
	@Override
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	@Override
	public String getWorkUnitDescription() {
		return workUnitDescription;
	}
	@Override
	public void setWorkUnitDescription(String workUnitDescription) {
		this.workUnitDescription = workUnitDescription;
	}
	@Override
	public String getModuleNumber() {
		return moduleNumber;
	}
	@Override
	public void setModuleNumber(String moduleNumber) {
		this.moduleNumber = moduleNumber;
	}

	@Override
	public long getEventDeliveryId() {
		return eventDeliveryId;
	}

	@Override
	public void setEventDeliveryId(long eventDeliveryId) {
		this.eventDeliveryId = eventDeliveryId;
	}

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AppEDFEvent{");
        sb.append("eventDeliveryId=").append(eventDeliveryId);
        sb.append(", moduleInstanceId=").append(moduleInstanceId);
        sb.append(", workUnitId='").append(workUnitId).append('\'');
        sb.append(", moduleId='").append(moduleId).append('\'');
        sb.append(", moduleNumber='").append(moduleNumber).append('\'');
        sb.append(", workUnitDescription='").append(workUnitDescription).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
