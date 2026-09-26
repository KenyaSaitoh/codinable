package pro.kensait.spring.employee.advice;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.support.QueryMethodParameterConversionException;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/** 検索パラメーターの型変換失敗だけを400に変換するその他は標準の例外処理を使う */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = RepositoryRestController.class)
public class SearchExceptionAdvice {
    // 不正検索パラメータの実行
    @ExceptionHandler(QueryMethodParameterConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidSearchParameter() {
        return Map.of("message", "検索条件の型が正しくありません");
    }
}
