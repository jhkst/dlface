package cz.activecode.dl.services;

import cz.activecode.dl.to.AlertMessage;

import java.util.List;

/**
 * Service for application alert messages.
 */
public interface AlertMessageService {

    /**
     * Adds message to be reported.
     * @param alertMessage the message to be reported.
     */
    void addMessage(AlertMessage alertMessage);

    /**
     * Returns all messages for reporting.
     * All returned messages are dismissed and are no more returned by this method.
     * @return all messages for reporting
     */
    List<AlertMessage> readMessages();
}
