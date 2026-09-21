package pro.kensait.spring.users;

/*
 * 指定された id の利用者がいなかったことを表す例外
 * これを HTTP の 404 に対応づけるのは GlobalExceptionHandler の仕事
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(int id) {
        super("id=" + id + " の利用者は見つかりません");
    }
}
