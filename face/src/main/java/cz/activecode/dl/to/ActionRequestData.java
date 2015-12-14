package cz.activecode.dl.to;

import java.util.concurrent.atomic.AtomicInteger;

public interface ActionRequestData {

    class IdFactory {
        private static final AtomicInteger ID_GEN = new AtomicInteger();
        protected static String newId() {
            return "ar" + ID_GEN.getAndIncrement();
        }
    }

    String getHtml();

    Class<? extends ActionResponseData> reponseClass();
}
