package pro.kensait.jakartaee.facelets.web;

import java.io.Serializable;
import java.util.Locale;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named
@SessionScoped
public class LocaleBean implements Serializable {
    private Locale locale = Locale.JAPANESE;

    public String change(String language) {
        locale = Locale.of(language);
        return null;
    }

    public Locale getLocale() {
        return locale;
    }
}
