package cz.activecode.dl.rest;

import cz.activecode.dl.ibridge.UserAction;
import cz.activecode.dl.ibridge.UserActionListener;
import cz.activecode.dl.to.ActionRequestData;
import cz.activecode.dl.to.ActionResponseData;
import cz.activecode.dl.to.actionrequests.CaptchaActionRequestData;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Path("/actionRequests")
@Component
public class ActionRequests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionRequests.class);

    private Map<String, ActionRequestData> actionRequests = Collections.synchronizedMap(new LinkedHashMap<>());

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<ActionRequestData> getActionRequests() {
        return new ArrayList<>(actionRequests.values());
    }

    @POST
    @Path("response")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void actionRequestResponse(Map<String, Object> response) {
        if(true) {
            return;
        }
        Class<? extends ActionResponseData> responseClass = lookupResponseClass((String) response.get("id"));
        ObjectMapper mapper = new ObjectMapper();
        ActionResponseData actionResponseData = mapper.convertValue(response, responseClass);
        LOGGER.info(String.valueOf(actionResponseData));


        //service.response(actionResponseData);
        //ar.remove(actionResponseData.getId());

        CaptchaActionRequestData c = new CaptchaActionRequestData();
        try {
            c.setImage(Files.readAllBytes(Paths.get("src/main/webapp/captcha.gif")));
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        //ar.put(c.getId(), c);
    }

    private Class<? extends ActionResponseData> lookupResponseClass(String id) {
        //LOGGER.info(String.valueOf(ar));
        //ActionRequestData actionRequestData = ar.get(id);
        //if(actionRequestData != null) {
        //    return actionRequestData.reponseClass();
        //}
        //throw new IllegalStateException("No request with id " + id);
        return null;
    }

    private class LocalUserActionListener implements UserActionListener {

        @Override
        public void requestAction(UserAction userAction) {
            /*ActionRequestData ard = userActionToActionRequestData();*/
        }
    }

}
