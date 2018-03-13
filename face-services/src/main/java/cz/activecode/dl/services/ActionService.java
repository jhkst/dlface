package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.actions.ActionRequestData;
import cz.activecode.dl.ibridge.actions.ActionResponseData;

import java.util.List;

/**
 * Service for manipulating with actions
 */
public interface ActionService {

    /**
     * Returns all currently valid acton requests.
     * @return all currently valid action requests
     */
    List<ActionRequestData> getActionRequests();

    /**
     * Responses for an action request.
     * @param actionResponseData response for request. Including pairing id.
     */
    void processResponse(ActionResponseData actionResponseData);

    /**
     * Lookup for response class for given action request
     * @param id id of request (see {@link ActionRequestData#getId()}
     * @return class representing response
     */
    Class<? extends ActionResponseData> lookupResponseClass(String id);
}
