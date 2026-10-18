package pro.kensait.jakartaee.upload;

/** 保存処理が判断した、業務上受け付けられないファイル（空・形式・名前の不正） */
public class InvalidFileException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidFileException(String message) {
        super(message);
    }
}
