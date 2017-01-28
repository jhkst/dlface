package cz.activecode.dl.rest.v1;

import cz.activecode.dl.ibridge.actions.ActionRequestData;
import cz.activecode.dl.ibridge.actions.ActionResponseData;
import cz.activecode.dl.services.ActionService;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * REST entry-point for all action related operations.
 */
@Path("/v1/actionRequests")
@Component
public class ActionRequests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionRequests.class);

    private ActionService actionService;

    @Autowired
    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * Returns all currently valid action requests.
     * @return all currently valid action requests
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<ActionRequestData> getActionRequests() {
        return actionService.getActionRequests();
    }

    /**
     * Responses for an action request.
     * @param response response for request. Including pairing id.
     */
    @POST @Path("response")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void actionRequestResponse(Map<String, Object> response) {
        ActionResponseData responseData = createResponse(response);
        actionService.processResponse(responseData);
    }

    /**
     * Creates response object from response map.
     * @param responseMap response map
     * @return response object
     */
    private ActionResponseData createResponse(Map<String, Object> responseMap) {
        Class<? extends ActionResponseData> responseClass = actionService.lookupResponseClass((String) responseMap.get("id"));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(responseMap, responseClass);
    }

}
