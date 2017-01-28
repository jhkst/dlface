package cz.activecode.dl.to;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.atomic.AtomicInteger;

@XmlRootElement
public class AlertMessage {

    private String id;
    private String message;
    private AlertType type;



    public enum AlertType {
        SUCCESS,
        INFO,
        WARNING,
        DANGER;
    }
    private static final AtomicInteger ID_GEN = new AtomicInteger(0);

    public AlertMessage(AlertType type, String message) {
        this.id = String.valueOf(ID_GEN.getAndIncrement());
        this.type = type;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public AlertType getType() {
        return type;
    }
}
