package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.ActionHandler;
import cz.activecode.dl.ibridge.actions.ActionRequestData;
import cz.activecode.dl.ibridge.actions.ActionResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ActionServiceImpl implements ActionService, ActionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionServiceImpl.class);

    private Map<String, ActionRequestCallback> actionRequestCallbacks = Collections.synchronizedMap(new LinkedHashMap<>());

    private static class ActionRequestCallback {
        private ActionRequestData actionRequestData;
        private Consumer<ActionResponseData> callback;

        public ActionRequestCallback(ActionRequestData actionRequestData, Consumer<ActionResponseData> callback) {
            this.actionRequestData = actionRequestData;
            this.callback = callback;
        }

        public ActionRequestData getActionRequestData() {
            return actionRequestData;
        }

        public Consumer<ActionResponseData> getCallback() {
            return callback;
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<ActionRequestData> getActionRequests() {
        return actionRequestCallbacks.values().stream().map(ActionRequestCallback::getActionRequestData).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processResponse(ActionResponseData actionResponseData) {
        ActionRequestCallback actionRequestCallback = actionRequestCallbacks.get(actionResponseData.getId());
        if(actionRequestCallback == null) {
            throw new IllegalArgumentException("No request with id " + actionResponseData.getId());
        }
        actionRequestCallback.getCallback().accept(actionResponseData); //TODO: call in thread ?
        actionRequestCallbacks.remove(actionResponseData.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends ActionResponseData> lookupResponseClass(String id) {
        ActionRequestCallback actionRequestData = actionRequestCallbacks.get(id);
        if(actionRequestData != null) {
            return actionRequestData.getActionRequestData().responseClass();
        }
        throw new IllegalStateException("No request with id " + id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addActionRequest(ActionRequestData actionRequestData, Consumer<ActionResponseData> callback) {
        if(callback == null) {
            LOGGER.warn("No callback provided for {}", actionRequestData);
            return;
        }

        actionRequestCallbacks.put(actionRequestData.getId(), new ActionRequestCallback(actionRequestData, callback));
    }

}
