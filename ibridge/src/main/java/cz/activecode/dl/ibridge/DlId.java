package cz.activecode.dl.ibridge;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * General class wrapping download ID.
 */
public class DlId implements Serializable {

    private transient static final AtomicInteger DLID_GEN = new AtomicInteger();

    private final String id;

    private DlId(String id) {
        this.id = id;
    }

    /**
     * Create unique ID (currently just for running instance)
     * @return unique ID
     */
    public static DlId create() {
        return new DlId("dl" + DLID_GEN.getAndIncrement());
    }

    /**
     * return ID value
     * @return ID value
     */
    //@JsonValue
    public String getId() {
        return id;
    }

    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DlId dlId = (DlId) o;

        return id.equals(dlId.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Parses id from string
     * @param dlId the id in string
     * @return the id
     */
    public static DlId fromString(String dlId) {
        return new DlId(dlId);
    }
}
