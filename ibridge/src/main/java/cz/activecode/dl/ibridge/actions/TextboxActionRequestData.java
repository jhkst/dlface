package cz.activecode.dl.ibridge.actions;

public class TextboxActionRequestData extends AbstractActionRequestData {

    private String name;

    public TextboxActionRequestData(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return "textbox";
    }

    @Override
    public Class<? extends ActionResponseData> responseClass() {
        return TextboxActionResponseData.class;
    }


}
