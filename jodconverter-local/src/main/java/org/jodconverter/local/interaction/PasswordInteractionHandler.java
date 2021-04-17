package org.jodconverter.local.interaction;

import com.sun.star.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordInteractionHandler implements XInteractionHandler {

    public ThreadLocal<PasswordRequest> passwordRequests = ThreadLocal.withInitial(() -> null);

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordInteractionHandler.class);

    @Override
    public void handle(XInteractionRequest xInteractionRequest) {
        LOGGER.debug("Interaction detected with following request {}", xInteractionRequest.getRequest());

        Object request = xInteractionRequest.getRequest();

        if (request instanceof PasswordRequest) {
            String documentPath ="n.a.";
            if(request instanceof DocumentPasswordRequest){
                documentPath = ((DocumentPasswordRequest) request).Name;
            }

            if(request instanceof DocumentMSPasswordRequest){
                documentPath = ((DocumentMSPasswordRequest) request).Name;
            }

            LOGGER.debug("Password interaction detected for "+documentPath);

            passwordRequests.set((PasswordRequest) request);
        }
    }
}
