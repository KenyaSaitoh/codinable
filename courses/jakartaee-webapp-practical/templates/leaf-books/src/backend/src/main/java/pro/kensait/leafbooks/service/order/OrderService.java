package pro.kensait.leafbooks.service.order;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import pro.kensait.leafbooks.common.MessageUtil;
import pro.kensait.leafbooks.dao.OrderDetailDao;
import pro.kensait.leafbooks.dao.OrderTranDao;
import pro.kensait.leafbooks.dao.StockDao;
import pro.kensait.leafbooks.entity.OrderDetail;
import pro.kensait.leafbooks.entity.OrderDetailPK;
import pro.kensait.leafbooks.entity.OrderTran;
import pro.kensait.leafbooks.entity.Stock;
import pro.kensait.leafbooks.dao.BookDao;
import pro.kensait.leafbooks.entity.Book;

// 注文処理と注文履歴取得を行うサービスクラス
@ApplicationScoped
@Transactional
public class OrderService implements OrderServiceIF {
    private static final Logger logger = LoggerFactory.getLogger(
            OrderService.class);

    @Inject
    private OrderTranDao orderTranDao;

    @Inject
    private OrderDetailDao orderDetailDao;

    @Inject
    private BookDao bookDao;

    @Inject
    private StockDao stockDao;

    // サービスメソッド：注文エンティティのリストを取得する（方式1）
    @Override
    public List<OrderTran> getOrderHistory(Integer customerId) {
        logger.info("[ OrderService#findOrderHistory ]");

        // 顧客IDから注文エンティティのリストを取得し、返す
        List<OrderTran> orderTranList =
                orderTranDao.findByCustomerId(customerId);
        return orderTranList;
    }

    // サービスメソッド：注文履歴のリストを取得する（方式2・詳細版）
    @Override
    public List<OrderHistoryTO> getOrderHistory2(Integer customerId) {
        logger.info("[ OrderService#findOrderHistory2 ]");

        // 顧客IDから注文履歴のリストを取得し、返す
        List<OrderHistoryTO> orderHistoryList =
                orderTranDao.findOrderHistoryByCustomerId(customerId);
        return orderHistoryList;
    }

    // サービスメソッド：注文エンティティのリストを取得する（方式3）
    @Override
    public List<OrderTran> getOrderHistory3(Integer customerId) {
        logger.info("[ OrderService#findOrderHistory3 ]");

        // 顧客IDから注文エンティティのリストを取得し、返す
        List<OrderTran> orderTranList =
                orderTranDao.findByCustomerIdWithDetails(customerId);
        return orderTranList;
    }

    // サービスメソッド：注文エンティティを取得する
    @Override
    public OrderTran getOrderTran(Integer orderTranId) {
        logger.info("[ OrderService#getOrderTran ]");

        // 注文IDから注文エンティティを取得し、返す
        OrderTran orderTran = orderTranDao.findById(orderTranId);
        if (orderTran == null) {
            throw new NotFoundException(MessageUtil.get("error.order-tran.not-found") + orderTranId);
        }
        return orderTran;
    }

    // サービスメソッド：注文エンティティを明細と共に取得する
    @Override
    public OrderTran getOrderTranWithDetails(Integer orderTranId) {
        logger.info("[ OrderService#getOrderTranWithDetails ]");

        // 注文IDから注文エンティティを明細と共に取得し、返す
        OrderTran orderTran = orderTranDao.findByIdWithDetails(orderTranId);
        if (orderTran == null) {
            throw new NotFoundException(MessageUtil.get("error.order-tran.not-found") + orderTranId);
        }
        return orderTran;
    }

    // サービスメソッド：注文明細エンティティを取得する
    @Override
    public OrderDetail getOrderDetail(OrderDetailPK pk) {
        logger.info("[ OrderService#getOrderDetail ]");

        // 複合主キー（注文IDと注文明細ID）から注文明細エンティティを取得し、返す
        OrderDetail orderDetail = orderDetailDao.findById(pk);
        if (orderDetail == null) {
            throw new NotFoundException(MessageUtil.get("error.order-detail.not-found") + pk);
        }
        return orderDetail;
    }

    // サービスメソッド：注文明細エンティティを取得する（オーバーロード）
    @Override
    public OrderDetail getOrderDetail(Integer tranId, Integer detailId) {
        logger.info("[ OrderService#getOrderDetail ] tranId=" + tranId 
                + ", detailId=" + detailId);

        OrderDetailPK pk = new OrderDetailPK(tranId, detailId);
        return getOrderDetail(pk);
    }

    // サービスメソッド：注文明細エンティティのリストを取得する
    @Override
    public List<OrderDetail> getOrderDetails(Integer orderTranId) {
        logger.info("[ OrderService#getOrderDetails ]");

        // 注文IDから注文明細エンティティのリストを取得し、返す
        List<OrderDetail> orderDetailList = orderDetailDao.findByOrderTranId(orderTranId);
        return orderDetailList;
    }

    // サービスメソッド：注文する
    @Override
    public OrderTran orderBooks(OrderTO orderTO) {
        logger.info("[ OrderService#orderBooks ]");

        // カートに追加された書籍毎に、在庫の残り個数をチェックし更新する
        for (CartItem cartItem : orderTO.cartItems()) {

            // 在庫を直接DBから取得（悲観的ロック）
            Stock stock = stockDao.findByIdWithLock(cartItem.getBookId());

            if (stock == null) {
                throw new RuntimeException(
                        String.format("%s: bookId=%d",
                                MessageUtil.get("error.stock.not-found"),
                                cartItem.getBookId()));
            }

            // 在庫が0未満になる場合は、例外を送出する
            int remaining = stock.getQuantity() - cartItem.getCount();
            if (remaining < 0) {
                throw new OutOfStockException(
                        cartItem.getBookId(),
                        cartItem.getBookName(),
                        MessageUtil.get("error.out-of-stock.message"));
            }

            // 在庫数を更新（JPA @Version による楽観的ロックが自動的に適用される）
            stock.setQuantity(remaining);
        }

        // 新しいOrderTranインスタンスを生成する
        OrderTran orderTran = new OrderTran(
                orderTO.orderDate(),
                orderTO.customerId(),
                orderTO.totalPrice(),
                orderTO.deliveryPrice(),
                orderTO.deliveryAddress(),
                orderTO.settlementType());

        // 生成したOrderTranインスタンスをpersist操作により永続化する
        orderTranDao.persist(orderTran);

        // カートアイテム（個々の注文明細）のイテレータを取得する
        List<CartItem> cartItems = orderTO.cartItems();

        // OrderDetailインスタンスの主キー値（注文明細ID）の初期値を設定する
        int orderDetailId = 0;

        for (CartItem cartItem : cartItems) {
            // 書籍情報を取得（直接DBアクセス）
            Book book = bookDao.findById(cartItem.getBookId());

            if (book == null) {
                throw new RuntimeException(
                        MessageUtil.get("error.book.not-found") + cartItem.getBookId());
            }

            // OrderDetailインスタンスの主キー値（注文明細ID）をカウントアップする
            orderDetailId = orderDetailId + 1;

            // 新しいOrderDetailインスタンスを生成する（書籍情報と出版社名を設定）
            String publisherName = (book.getPublisher() != null)
                    ? book.getPublisher().getPublisherName() : "不明";
            
            OrderDetail orderDetail = new OrderDetail(
                    orderTran.getOrderTranId(),
                    orderDetailId,
                    book.getBookId(),
                    book.getBookName(),
                    publisherName,
                    book.getPrice(),
                    cartItem.getCount());

            // OrderDetailインスタンスを保存する
            orderDetailDao.persist(orderDetail);
        }

        // データベースから明細を含めて再取得して返す
        // （永続化した明細をorderDetailsリレーションシップに反映させるため）
        return orderTranDao.findByIdWithDetails(orderTran.getOrderTranId());
    }
}