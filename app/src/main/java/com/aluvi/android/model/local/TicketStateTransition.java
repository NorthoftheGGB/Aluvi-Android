package com.aluvi.android.model.local;

/**
 * Created by matthewxi on 7/22/15.
 */
public class TicketStateTransition {

    private Integer ticketId;
    private String oldState;
    private String newState;

    public TicketStateTransition(Integer ticketId, String oldState, String newState) {
        this.ticketId = ticketId;
        this.oldState = oldState;
        this.newState = newState;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public String getOldState() {
        return oldState;
    }

    public void setOldState(String oldState) {
        this.oldState = oldState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }
}
