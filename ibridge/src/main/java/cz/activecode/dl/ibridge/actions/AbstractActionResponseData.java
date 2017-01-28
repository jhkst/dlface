package cz.activecode.dl.ibridge.actions;

public abstract class AbstractActionResponseData implements ActionResponseData {

    private String id;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
