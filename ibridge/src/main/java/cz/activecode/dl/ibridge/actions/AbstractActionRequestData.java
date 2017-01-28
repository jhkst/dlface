package cz.activecode.dl.ibridge.actions;

public abstract class AbstractActionRequestData implements ActionRequestData {

    private final String id = IdFactory.newId();

    private String dlId;

    public String getId() {
        return id;
    }

    public String getDlId() {
        return dlId;
    }

    public void setDlId(String dlId) {
        this.dlId = dlId;
    }
}
