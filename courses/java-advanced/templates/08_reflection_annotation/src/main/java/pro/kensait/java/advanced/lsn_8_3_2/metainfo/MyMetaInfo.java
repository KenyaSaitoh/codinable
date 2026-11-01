package pro.kensait.java.advanced.lsn_8_3_2.metainfo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 【1】
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD,
        ElementType.PARAMETER}) // 【2】
@Documented
public @interface MyMetaInfo {
    String value(); // 【3】
    String elemName() default ""; // 【4】
}