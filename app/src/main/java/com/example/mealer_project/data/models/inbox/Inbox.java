package com.example.mealer_project.data.models.inbox;

/**
 * Generic Inbox Interface to enforce implementation of certain methods
 */
public interface Inbox {
    /**
     * Add a complaint to the inbox
     * @param complaint Complaint object to be added to inbox
     */
    void addComplaint(Complaint complaint);

    /**
     * Remove a complaint by ID
     * @param complaintId ID of complaint to be removed
     */
    void removeComplaint(String complaintId);
}
