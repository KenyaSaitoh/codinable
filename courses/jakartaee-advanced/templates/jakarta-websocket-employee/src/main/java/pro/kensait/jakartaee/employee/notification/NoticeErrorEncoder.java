package pro.kensait.jakartaee.employee.notification;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

/*
 * 入力エラーの NoticeError を {"error":"..."} のテキストへ変換するエンコーダー
 */
public class NoticeErrorEncoder implements Encoder.Text<NoticeError> {
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Override
    public String encode(NoticeError error) throws EncodeException {
        return JSONB.toJson(error);
    }
}
