package cz.activecode.dl.frdbridge;

import cz.activecode.dl.ibridge.ActionHandler;
import cz.activecode.dl.ibridge.actions.*;
import cz.vity.freerapid.plugins.webclient.hoster.PremiumAccount;
import cz.vity.freerapid.plugins.webclient.interfaces.DialogSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class FrdDialogSupportImpl implements DialogSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrdDialogSupportImpl.class);
    private FrdBridgeConfig fdrBridgeConfig;
    private ActionHandler actionHandler;

    public void setFdrBridgeConfig(FrdBridgeConfig fdrBridgeConfig) {
        this.fdrBridgeConfig = fdrBridgeConfig;
    }

    public void setActionHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    @Override
    public PremiumAccount showAccountDialog(PremiumAccount premiumAccount, String dialogTitle) throws Exception {
        return showAccountDialog(premiumAccount, dialogTitle, false);
    }

    @Override
    public PremiumAccount showAccountDialog(PremiumAccount premiumAccount, String dialogTitle, boolean emptyAllowed) throws Exception {
        LOGGER.info("showAccountDialog({}, {}, {})", premiumAccount, dialogTitle, emptyAllowed);
        //TODO:
        return new PremiumAccount();
    }

    @Override
    public boolean showOKCancelDialog(Component container, String dialogTitle) throws Exception {
        OKCancelActionRequestData requestData = new OKCancelActionRequestData(dialogTitle);

        ActionResponseData responseData = waitForResponse(requestData, Long.MAX_VALUE);
        return responseData instanceof OKCancelActionResponseData && ((OKCancelActionResponseData) responseData).isConfirmed();
    }

    @Override
    public void showOKDialog(Component container, String dialogTitle) throws Exception {
        OKActionRequestData requestData = new OKActionRequestData(dialogTitle);

        waitForResponse(requestData, Long.MAX_VALUE);
    }

    @Override
    public String askForCaptcha(BufferedImage image) throws Exception {
        byte[] imageData;

        //convert image
        imageData = bufferedImageToPng(image);
        CaptchaActionRequestData captchaActionRequestData = new CaptchaActionRequestData();
        captchaActionRequestData.setImage(imageData);

        ActionResponseData responseData = waitForResponse(captchaActionRequestData, fdrBridgeConfig.getCaptchaWaitingTime());
        if(responseData instanceof CaptchaActionResponseData) {
            return ((CaptchaActionResponseData) responseData).getValue();
        }
        return null;
    }

    @Override
    public String askForPassword(String name) throws Exception {
        TextboxActionRequestData textboxActionRequestData = new TextboxActionRequestData(name);

        ActionResponseData responseData = waitForResponse(textboxActionRequestData, Long.MAX_VALUE);//todo: change constant
        if(responseData instanceof TextboxActionResponseData) {
            return ((TextboxActionResponseData) responseData).getValue();
        }
        return null;
    }

    @Override
    public Object getDialogLock() {
        LOGGER.debug("getDialogLock not implemented");
        return null;
    }

    private static byte[] bufferedImageToPng(BufferedImage image) throws IOException {
        byte[] imageData;
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            baos.flush();
            imageData = baos.toByteArray();
        }
        return imageData;
    }

    private ActionResponseData waitForResponse(ActionRequestData requestData, long timeout) {
        Semaphore semaphore = new Semaphore(0);
        AtomicReference<ActionResponseData> response = new AtomicReference<>();
        actionHandler.addActionRequest(requestData, actionResponseData -> {
            response.compareAndSet(null, actionResponseData);
            semaphore.release();
        });
        try {
            if(semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                return response.get();
            } else {
                LOGGER.warn("Action response timed out");
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Action response interrupted", e);
        }
        return null;
    }

}
