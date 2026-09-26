package pro.kensait.leafbooks.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import pro.kensait.leafbooks.api.dto.ErrorResponse;
import pro.kensait.leafbooks.api.dto.LoginRequest;
import pro.kensait.leafbooks.api.dto.LoginResponse;
import pro.kensait.leafbooks.api.dto.RegisterRequest;
import pro.kensait.leafbooks.external.CustomerApiClient;
import pro.kensait.leafbooks.external.CustomerExistsException;
import pro.kensait.leafbooks.external.CustomerNotFoundException;
import pro.kensait.leafbooks.external.CustomerTO;
import pro.kensait.leafbooks.security.JwtAuthenticationToken;
import pro.kensait.leafbooks.security.JwtUtil;

/*
 * 認証機能のコントローラー
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private CustomerApiClient customerApiClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // ログイン
    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest request) {
        logger.info("[ AuthController#login ] email: {}", request.email());

        try {
            // 顧客情報を取得
            CustomerTO customer = customerApiClient.queryByEmail(request.email());

            // パスワード照合
            if (!passwordEncoder.matches(request.password(), customer.password())) {
                logger.warn("[ AuthController#login ] Password mismatch for email: {}", request.email());
                ErrorResponse errorResponse = new ErrorResponse(
                        401,
                        "Unauthorized",
                        "メールアドレスまたはパスワードが正しくありません",
                        "/api/auth/login"
                );
                return ResponseEntity.status(401).body(errorResponse);
            }

            // JWT生成
            String jwt = jwtUtil.generateToken(customer.customerId(), customer.email());

            // HttpOnly Cookieを生成
            ResponseCookie cookie = ResponseCookie.from(jwtUtil.getCookieName(), jwt)
                    .httpOnly(true)
                    .secure(false)  // 開発環境ではfalse、本番環境ではtrue
                    .path("/")
                    .maxAge(jwtUtil.getExpirationSeconds())
                    .sameSite("Lax")
                    .build();

            // レスポンス生成
            LoginResponse response = new LoginResponse(
                    customer.customerId(),
                    customer.customerName(),
                    customer.email(),
                    customer.birthday(),
                    customer.address()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);

        } catch (CustomerNotFoundException e) {
            logger.warn("[ AuthController#login ] Customer not found: {}", request.email());
            ErrorResponse errorResponse = new ErrorResponse(
                    401,
                    "Unauthorized",
                    "メールアドレスまたはパスワードが正しくありません",
                    "/api/auth/login"
            );
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    // ログアウト
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        logger.info("[ AuthController#logout ]");

        // Cookieを削除
        ResponseCookie cookie = ResponseCookie.from(jwtUtil.getCookieName(), "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    // 新規登録
    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterRequest request) {
        logger.info("[ AuthController#register ] email: {}", request.email());

        // 住所のバリデーション（都道府県チェック）
        if (!checkAddressPrefectures(request.address())) {
            logger.warn("[ AuthController#register ] Invalid address: {}", request.address());
            ErrorResponse errorResponse = new ErrorResponse(
                    400,
                    "Bad Request",
                    "住所は都道府県名から始めてください",
                    "/api/auth/register"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // 顧客TO生成
            CustomerTO customer = new CustomerTO(
                    null,
                    request.customerName(),
                    passwordEncoder.encode(request.password()),
                    request.email(),
                    request.birthday(),
                    request.address()
            );

            // 顧客登録
            CustomerTO createdCustomer = customerApiClient.create(customer);

            // JWT生成
            String jwt = jwtUtil.generateToken(createdCustomer.customerId(), createdCustomer.email());

            // HttpOnly Cookieを生成
            ResponseCookie cookie = ResponseCookie.from(jwtUtil.getCookieName(), jwt)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(jwtUtil.getExpirationSeconds())
                    .sameSite("Lax")
                    .build();

            // レスポンス生成
            LoginResponse response = new LoginResponse(
                    createdCustomer.customerId(),
                    createdCustomer.customerName(),
                    createdCustomer.email(),
                    createdCustomer.birthday(),
                    createdCustomer.address()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);

        } catch (CustomerExistsException e) {
            logger.warn("[ AuthController#register ] Customer already exists: {}", request.email());
            // 例外ハンドラで処理されるのでスローする
            throw e;
        }
    }

    // 現在のログインユーザー情報取得
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser(HttpServletRequest request) {
        logger.info("[ AuthController#getCurrentUser ]");

        // SecurityContextから認証情報を取得
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) 
                SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // JWTから顧客IDを取得
        Integer customerId = authentication.getCustomerId();

        // 顧客情報を取得
        CustomerTO customer = customerApiClient.getById(customerId);

        LoginResponse response = new LoginResponse(
                customer.customerId(),
                customer.customerName(),
                customer.email(),
                customer.birthday(),
                customer.address()
        );

        return ResponseEntity.ok(response);
    }

    // 住所の都道府県チェック
    private boolean checkAddressPrefectures(String address) {
        for (String prefecture : PREFECTURES) {
            if (address.startsWith(prefecture)) {
                return true;
            }
        }
        return false;
    }

    private static final String[] PREFECTURES = {
            "北海道", "青森県", "岩手県", "宮城県", "秋田県",
            "山形県", "福島県", "茨城県", "栃木県", "群馬県",
            "埼玉県", "千葉県", "東京都", "神奈川県", "新潟県",
            "富山県", "石川県", "福井県", "山梨県", "長野県",
            "岐阜県", "静岡県", "愛知県", "三重県", "滋賀県",
            "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県",
            "鳥取県", "島根県", "岡山県", "広島県", "山口県",
            "徳島県", "香川県", "愛媛県", "高知県", "福岡県",
            "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県",
            "鹿児島県", "沖縄県"
    };
}

