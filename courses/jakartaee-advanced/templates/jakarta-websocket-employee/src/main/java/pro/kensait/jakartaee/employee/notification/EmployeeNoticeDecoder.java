package pro.kensait.jakartaee.employee.notification;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

/*
 * 受信したテキストの JSON を EmployeeNotice へ変換するデコーダー
 */
public class EmployeeNoticeDecoder implements Decoder.Text<EmployeeNotice> { // 【1】
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Override
    public boolean willDecode(String text) { // 【2】
        return text != null && !text.isBlank();
    }

    @Override
    public EmployeeNotice decode(String text) throws DecodeException {
        try {
            return JSONB.fromJson(text, EmployeeNotice.class);
        } catch (JsonbException e) {
            throw new DecodeException(text, "通知のJSONを読み取れません", e); // 【3】
        }
    }
}
