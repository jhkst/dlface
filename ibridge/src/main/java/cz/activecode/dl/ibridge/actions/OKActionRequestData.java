package cz.activecode.dl.ibridge.actions;

public class OKActionRequestData extends AbstractActionRequestData {

    private String text;

    public OKActionRequestData(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String getType() {
        return "ok";
    }

    @Override
    public Class<? extends ActionResponseData> responseClass() {
        return OKActionResponseData.class;
    }
}
