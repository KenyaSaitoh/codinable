package pro.kensait.jakartaee.calc.web;

import java.util.Iterator;

import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;

public class CalcExceptionHandler extends ExceptionHandlerWrapper {
    public CalcExceptionHandler(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handle() {
        Iterator<ExceptionQueuedEvent> events =
                getUnhandledExceptionQueuedEvents().iterator();
        while (events.hasNext()) {
            Throwable cause = getRootCause(
                    events.next().getContext().getException());
            if (cause instanceof ViewExpiredException) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.getApplication().getNavigationHandler()
                        .handleNavigation(context, null,
                                "ViewExpiredPage?faces-redirect=true");
                events.remove();
            }
        }
        getWrapped().handle();
    }
}
