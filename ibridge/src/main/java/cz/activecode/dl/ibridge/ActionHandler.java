package cz.activecode.dl.ibridge;

import cz.activecode.dl.ibridge.actions.ActionRequestData;
import cz.activecode.dl.ibridge.actions.ActionResponseData;

import java.util.function.Consumer;

/**
 * User actions handler.
 */
public interface ActionHandler {

    /**
     * Adds request for user action.
     * @param actionRequestData
     * @param callback
     */
    void addActionRequest(ActionRequestData actionRequestData, Consumer<ActionResponseData> callback);

}
