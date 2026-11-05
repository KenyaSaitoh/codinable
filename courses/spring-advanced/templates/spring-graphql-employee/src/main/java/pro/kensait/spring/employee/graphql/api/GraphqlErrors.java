package pro.kensait.spring.employee.graphql.api;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import pro.kensait.spring.employee.graphql.service.EmployeeNotFoundException;
/*
 * graphqlエラーの機能を提供するクラス
 */
@ControllerAdvice
public class GraphqlErrors {
    // 社員が存在しない場合の応答
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
    // 引数の型へ変換できない入力（数字でない社員IDなど）への応答
    @GraphQlExceptionHandler(BindException.class)
    public GraphQLError invalidArgument(BindException error, DataFetchingEnvironment environment) {
        return GraphqlErrorBuilder.newError(environment).errorType(ErrorType.BAD_REQUEST)
                .message("引数の形式を確認してください").build();
    }
}
