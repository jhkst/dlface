package cz.activecode.dl.to.actionrequests;

import cz.activecode.dl.to.AbstractActionRequestData;
import cz.activecode.dl.to.ActionResponseData;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Base64;

public class CaptchaActionRequestData extends AbstractActionRequestData {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private byte[] image;


    public String getCaptchaImage() {
        return imageToEmbed(image);
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public Class<? extends ActionResponseData> reponseClass() {
        return CaptchaActionResponseData.class;
    }

    private static String imageToEmbed(byte[] image) {
        String contentType;
        try {
            contentType = URLConnection.guessContentTypeFromStream(new BufferedInputStream(new ByteArrayInputStream(image)));
        } catch (IOException e) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        return "data:" + contentType + ";base64," + encodeImage(image);
    }

    private static String encodeImage(byte[] image) {
        return Base64.getEncoder().encodeToString(image);
    }
}
