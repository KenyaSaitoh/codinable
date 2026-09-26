package pro.kensait.spring.employee.graphql.api;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import pro.kensait.spring.employee.graphql.service.EmployeeNotFoundException;
/*
 * graphqlエラーの機能を提供するクラス
 */
@ControllerAdvice
public class GraphqlErrors {
    // 未存在の実行
    @GraphQlExceptionHandler(EmployeeNotFoundException.class)
    public GraphQLError missing(EmployeeNotFoundException error, DataFetchingEnvironment environment) {
        return GraphqlErrorBuilder.newError(environment).errorType(ErrorType.NOT_FOUND)
                .message(error.getMessage()).build();
    }
    // 不正入力エラーへの応答
    @GraphQlExceptionHandler({jakarta.validation.ConstraintViolationException.class,
            java.time.format.DateTimeParseException.class,
            org.springframework.dao.DataIntegrityViolationException.class})
    public GraphQLError invalid(Exception error, DataFetchingEnvironment environment) {
        return GraphqlErrorBuilder.newError(environment).errorType(ErrorType.BAD_REQUEST)
                .message("社員名・部署・給与・日付を確認してください").build();
    }
}
