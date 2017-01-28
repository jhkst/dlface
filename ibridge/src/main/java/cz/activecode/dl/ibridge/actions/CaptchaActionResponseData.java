package cz.activecode.dl.ibridge.actions;

public class CaptchaActionResponseData extends AbstractActionResponseData {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "CaptchaActionResponseData{" +
                "id='" + getId() + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
