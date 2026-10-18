package pro.kensait.jakartaee.employee.notification;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

/*
 * 送信する EmployeeNotice を JSON のテキストへ変換するエンコーダー
 */
public class EmployeeNoticeEncoder implements Encoder.Text<EmployeeNotice> { // 【4】
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Override
    public String encode(EmployeeNotice notice) throws EncodeException {
        return JSONB.toJson(notice); // 【5】
    }
}
