package pro.kensait.junit.fee.template;

// JUnit 5拡張機能をインポート
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;

/*
 * テストテンプレートのプロバイダ
 * TestTemplateInvocationContextProviderインターフェースを実装するクラスを定義
 */
public class FeeTestTemplateProvider implements TestTemplateInvocationContextProvider {
    private static final String STANDARD_REGION_CODE = "R01";
    private static final String REMOTE_REGION_CODE = "R99";

    // 配送料計算のテストケースを提供するスタティックメソッド
    static Stream<Arguments> feeCalculationArguments() {
        // 地域コード、購入金額、期待される配送料の組み合わせを提供
        return Stream.of(
                Arguments.of(STANDARD_REGION_CODE, 3000, 0),
                Arguments.of(STANDARD_REGION_CODE, 2999, 500),
                Arguments.of(REMOTE_REGION_CODE, 5000, 500),
                Arguments.of(REMOTE_REGION_CODE, 4999, 1000));
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        // このプロバイダがすべてのテストテンプレートに対応することを示す
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
            ExtensionContext context) {
        // feeCalculationArgumentsメソッドから提供されたテストデータを基にテストテンプレートのコンテキストを生成
        return feeCalculationArguments().map(arguments -> new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                // テスト実行時の表示名
                return String.format("［ 地域コード = %s, 購入金額 = %d, 期待配送料 = %d ]",
                        arguments.get()[0], arguments.get()[1], arguments.get()[2]);
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                // パラメータリゾルバを追加して、テストメソッドに渡されるパラメータを解決
                return List.of(new ParameterResolver() {
                    @Override
                    public boolean supportsParameter(ParameterContext parameterContext,
                            ExtensionContext extensionContext) {
                        // このリゾルバがすべてのパラメータをサポートすることを示す
                        return true;
                    }

                    @Override
                    public Object resolveParameter(ParameterContext parameterContext,
                            ExtensionContext extensionContext) {
                        // テストメソッドに渡されるべきパラメータの値を解決して提供
                        return arguments.get()[parameterContext.getIndex()];
                    }
                });
            }
        });
    }
}