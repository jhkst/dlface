package cz.activecode.dl.rest.v1;

import cz.activecode.dl.services.AlertMessageService;
import cz.activecode.dl.to.AlertMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * REST entry-point for alerts.
 */
@Path("/v1/alerts")
@Component
public class Alerts {

    private AlertMessageService alertMessageService;

    @Autowired
    public void setAlertMessageService(AlertMessageService alertMessageService) {
        this.alertMessageService = alertMessageService;
    }

    /**
     * Returns all alert messages.
     * @return all alert messages
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<AlertMessage> getAlertMessages() {
        return alertMessageService.readMessages();
    }

}
