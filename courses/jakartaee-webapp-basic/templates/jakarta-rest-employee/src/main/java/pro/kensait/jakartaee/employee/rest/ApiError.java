package pro.kensait.jakartaee.employee.rest;

import java.util.List;

/** すべてのエラー本文に共通の形式（ステータス・メッセージ・項目ごとのエラー） */
public record ApiError(int status, String message, List<String> errors) {
}
