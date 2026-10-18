package pro.kensait.jakartaee.employee.rest;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;
import jakarta.json.bind.config.PropertyOrderStrategy;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/*
 * Jakarta REST が本文の変換に使う Jsonb の設定
 * @Provider を外して ▶ アプリ実行すると、Victor の null の項目が省略される（既定の変換）
 */
@Provider // 【1】
public class JsonbConfigResolver implements ContextResolver<Jsonb> {
    private final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
            .withNullValues(true) // 【2】null の項目も "departmentId":null のように出力する
            .withPropertyOrderStrategy(PropertyOrderStrategy.LEXICOGRAPHICAL) // 【3】既定と同じ辞書順を明示する
            .withPropertyNamingStrategy(PropertyNamingStrategy.IDENTITY)); // 【4】既定と同じ項目名を明示する

    @Override
    public Jsonb getContext(Class<?> type) {
        return jsonb;
    }
}
