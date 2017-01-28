package cz.activecode.dl.ibridge.actions;

public class AccountActionRequestData extends AbstractActionRequestData {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getType() {
        return "account";
    }

    @Override
    public Class<? extends ActionResponseData> responseClass() {
        return AccountActionResponseData.class;
    }
}
