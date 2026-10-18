package pro.kensait.jakartaee.employee.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * ログイン画面（login.xhtml）のバッキングBean
 * SecurityContext.authenticate の戻り値で、元の画面へ戻る・エラーを出す・社員一覧へ移る、を分ける
 */
@Named
@RequestScoped
public class LoginBean {
    @Inject
    private SecurityContext securityContext;
    @Inject
    private FacesContext facesContext;

    private String username;
    private String password;

    public String login() {
        ExternalContext external = facesContext.getExternalContext();
        AuthenticationStatus status = securityContext.authenticate(
                (HttpServletRequest) external.getRequest(),
                (HttpServletResponse) external.getResponse(),
                AuthenticationParameters.withParams()
                        .credential(new UsernamePasswordCredential(username, password)));
        switch (status) {
            case SEND_CONTINUE -> {
                // 元の画面へのリダイレクトが設定済み。Faces はここで描画を終える
                facesContext.responseComplete();
                return null;
            }
            case SEND_FAILURE -> {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "ユーザー名またはパスワードが正しくありません", null));
                return null;
            }
            case SUCCESS -> {
                // ログイン画面を直接開いてログインした場合
                return "/employees/list.xhtml?faces-redirect=true";
            }
            default -> {
                return null;
            }
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
