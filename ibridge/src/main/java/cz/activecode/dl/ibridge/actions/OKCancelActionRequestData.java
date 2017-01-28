package cz.activecode.dl.ibridge.actions;

public class OKCancelActionRequestData extends AbstractActionRequestData {

    private String text;

    public OKCancelActionRequestData(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getType() {
        return "okcancel";
    }

    @Override
    public Class<? extends ActionResponseData> responseClass() {
        return OKCancelActionResponseData.class;
    }
}
