package pro.kensait.leafbooks.web.book;

/*
 * 検索パラメータに使用するデータ
 */
public record SearchParam(
        Integer categoryId,
        String keyword) {
}
