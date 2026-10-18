package pro.kensait.jakartaee.calc.web;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;

public class CalcExceptionHandlerFactory extends ExceptionHandlerFactory {
    public CalcExceptionHandlerFactory(ExceptionHandlerFactory wrapped) {
        super(wrapped);
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new CalcExceptionHandler(getWrapped().getExceptionHandler());
    }
}
