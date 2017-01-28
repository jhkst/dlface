package cz.activecode.dl.ibridge.actions;

public class OKCancelActionResponseData extends AbstractActionResponseData {

    private boolean confirmed;

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
