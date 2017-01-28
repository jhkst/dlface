package cz.activecode.dl.ibridge.actions;

public class TextboxActionResponseData implements ActionResponseData {

    private String id;
    private String value;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TextboxActionResponseData{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
