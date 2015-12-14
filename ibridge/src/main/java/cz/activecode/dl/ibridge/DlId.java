package cz.activecode.dl.ibridge;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class DlId implements Serializable {

    private transient static final AtomicInteger DLID_GEN = new AtomicInteger();

    private String id;

    private DlId(String id) {
        this.id = id;
    }

    public static DlId create() {
        return new DlId("dl" + DLID_GEN.getAndIncrement());
    }

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
}
