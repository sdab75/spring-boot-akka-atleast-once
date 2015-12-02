package com.ms.event;

import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;

/**
 * this is a placeholder for the actual event. this will likely reside in a place common to both assignment and edf-flow
 * @author adtaylor
 *
 */
public class AssignmentEvent  extends  AppEDFEvent{

    //private String stepId;
    public static enum EVENTTYPE {
        NONE,
        TADONE,
        ASSIGN,
        ACQUIRE,
        CLOSE,
        COMPLETE,
        APPROVED,
        REASSIGN,
        POSTASSIGN,
        REVISION,
        POSTCLOSE,
        POSTCOMPLETE,
        UPDATEASSIGN;

    }

    public AssignmentEvent() {

    }

    @NotBlank
    private String assigneeId;
    private String userRole;
    @NotBlank
    private String assignedById;
    private int priority;
    private Date dueDate;
    private boolean isApproval;
    private EVENTTYPE eventtype;

    //@Pattern(regexp = "ASSIGNED|INPROGRESS|CLOSED") // TODO: should we do this?


    public String getAssigneeId() {
        return assigneeId;
    }
    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }
    public String getAssignedById() {
        return assignedById;
    }
    public void setAssignedById(String assignedById) {
        this.assignedById = assignedById;
    }

    //@Override //TODO: fix later
    public String getEventType() {
        return eventtype.toString();
    }

    public void setEventtype(EVENTTYPE eventtype) {
        this.eventtype = eventtype;
    }


    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getUserRole() {
        return userRole;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AssignmentEvent{");
        sb.append("assignedById='").append(assignedById).append('\'');
        sb.append(", assigneeId='").append(assigneeId).append('\'');
        sb.append(", userRole='").append(userRole).append('\'');
        sb.append(", priority=").append(priority);
        sb.append(", dueDate=").append(dueDate);
        sb.append(", isApproval=").append(isApproval);
        sb.append(", eventtype=").append(eventtype);
        sb.append('}');
        return sb.toString();
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public boolean isApproval() {
		return isApproval;
	}
	public void setApproval(boolean isApproval) {
		this.isApproval = isApproval;
	}

}
