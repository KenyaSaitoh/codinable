package pro.kensait.leafbooks.web.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import jakarta.servlet.http.HttpSession;
import pro.kensait.leafbooks.entity.Book;
import pro.kensait.leafbooks.entity.Stock;
import pro.kensait.leafbooks.repository.StockRepository;
import pro.kensait.leafbooks.external.CustomerTO;
import pro.kensait.leafbooks.service.book.BookService;
import pro.kensait.leafbooks.service.delivery.DeliveryFeeService;

/*
 * カート機能のコントローラー
 */
@Controller
@SessionAttributes("cartSession")
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(
            CartController.class);

    // initセッションの実行
    @ModelAttribute("cartSession")
    public CartSession initSession(){
        logger.info("[ CartController#initSession ]");

        return new CartSession(new CopyOnWriteArrayList<>(),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                null,
                null);
    }

    @Autowired
    private BookService bookService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private DeliveryFeeService deliveryFeeService;

    // アクションメソッド：書籍をカートに追加
    @PostMapping("/addBook")
    public String addBook(@RequestParam("bookId") Integer bookId,
            CartSession cartSession) {
        logger.info("[ CartController#addBook ]");

        // サービスを呼び出して、選択されたIDから書籍エンティティを取得する
        Book book = bookService.getBook(bookId);

        // 在庫情報を取得（楽観的ロック用にバージョンを取得）
        Stock stock = stockRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("在庫が見つかりません"));

        // 選択された書籍がカートに存在している場合は、注文数と金額を加算する
        boolean isExists = false;
        List<CartItem> cartItems = cartSession.getCartItems();
        for (CartItem cartItem : cartItems) {
            if (bookId.equals(cartItem.getBookId())) {
                cartItem.setCount(cartItem.getCount() + 1);
                cartItem.setPrice(cartItem.getPrice().add(book.getPrice()));
                // バージョンを更新（最新の値に）
                cartItem.setVersion(stock.getVersion());
                isExists = true;
                break;
            }
        }

        // 選択された書籍がカートに存在していない場合は、
        // 新しいCartItemBeanオブジェクトを生成し、カートに追加する
        if (! isExists) {
            CartItem cartItem = new CartItem(book.getBookId(),
                    book.getBookName(),
                    book.getPublisher().getPublisherName(),
                    book.getPrice(),
                    1,
                    false,
                    stock.getVersion());  // 楽観的ロック用にバージョンを保存
            cartItems.add(cartItem);
        }
        cartSession.setCartItems(cartItems);

        // 合計金額を加算し、HTTPセッションに追加する
        BigDecimal totalPrice = cartSession.getTotalPrice();
        cartSession.setTotalPrice(totalPrice.add(book.getPrice()));

        // CartViewPageにフォワードする
        return "CartViewPage";
    }

    // アクションメソッド：選択された書籍をカートから削除
    @PostMapping("/removeBook")
    public String removeBook(@RequestParam(required = false) List<Integer> removeBookIdList,
            CartSession cartSession) {
        logger.info("[ CartController#removeBook ]");

        // 選択された書籍があったかどうかを判定
        if (removeBookIdList != null) {
            // カート内の書籍と選択された書籍を、二重ループによってマッチングする
            List<CartItem> cartItems = cartSession.getCartItems();
            BigDecimal totalPrice = cartSession.getTotalPrice();
            for (CartItem cartItem : cartItems) {
                if (removeBookIdList.contains(cartItem.getBookId())) {
                    totalPrice = totalPrice.subtract(cartItem.getPrice());
                    cartItems.remove(cartItem);
                }
            }
            cartSession.setCartItems(cartItems);
            cartSession.setTotalPrice(totalPrice);
        }

        // CartViewPageにフォワードする
        return "CartViewPage";
    }

    // アクションメソッド：カートをクリア
    @PostMapping("/clear")
    public String clear(SessionStatus status) {
        logger.info("[ CartController#clear ]");

        // HTTPセッションからCartSessionを削除する
        status.setComplete();

        // CartClearPageにフォワードする
        return "CartClearPage";
    }

    // アクションメソッド：カートを参照
    @GetMapping("/viewCart")
    public String viewCart(CartSession cartSession, Model model) {
        logger.info("[ CartController#viewCart ]");

        // カートに商品が一つも入っていなかった場合は、エラーメッセージをモデルに追加する
        if (cartSession.getCartItems().size() == 0) {
            logger.info("[ CartController#viewCart ] カートに商品なしエラー");
            String globalErrorMessage = messageSource.getMessage("error.cart.empty", null, null);
            model.addAttribute("globalErrorMessage", globalErrorMessage);

            /* 元のページに戻すには設計上、難しい
             * このアクションは、様々なページから呼ばれる
             * 元のページにはそのページのモデルがあるので、正しく遷移するためにリダイレクトが必要
             * リダイレクトだとエラーメッセージが失われてしまうので、フラッシュスコープが必要
             */
        }

        // CartViewPageフォワードする
        return "CartViewPage";
    }

    // アクションメソッド：カートの内容を確定
    @PostMapping("/fix")
    public String fix(HttpSession httpSession, CartSession cartSession,
            BindingResult errors, Model model) {
        logger.info("[ CartController#fix ]");

        // HTTPセッションからCustomerを取り出し、デフォルトの配送先住所として、
        // CartSessionに住所を設定する
        CustomerTO customer = (CustomerTO) httpSession.getAttribute("customer");
        cartSession.setDeliveryAddress(customer.address());

        // 配送料金を計算し、CartSessionに設定する
        // - 通常配送料金: 800円
        // - 沖縄県への配送: 1700円
        // - 5000円以上購入: 送料無料（0円）
        BigDecimal deliveryPrice = deliveryFeeService.calculateDeliveryFee(
                customer.address(), 
                cartSession.getTotalPrice());
        cartSession.setDeliveryPrice(deliveryPrice);

        // BookOrderPageにフォワードする
        return "BookOrderPage";
    }
}
