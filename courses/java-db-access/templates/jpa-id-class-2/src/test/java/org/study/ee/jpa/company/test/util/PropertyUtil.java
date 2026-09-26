package org.study.ee.jpa.company.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/*
 * propertyの機能を提供するクラス
 */
public class PropertyUtil {

    private static ResourceBundle resource;
    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("db.properties")) {
            resource = new PropertyResourceBundle(is);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    // 値の取得
    public static String getValue(String key) {
        return resource.getString(key);
    }
}
