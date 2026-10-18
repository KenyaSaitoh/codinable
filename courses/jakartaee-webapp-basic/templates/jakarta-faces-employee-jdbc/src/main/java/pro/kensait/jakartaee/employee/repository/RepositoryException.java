package pro.kensait.jakartaee.employee.repository;

/** SQLException を包み直した非検査例外。上位の層を JDBC の検査例外と製品固有のエラーコードから切り離す */
public class RepositoryException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
