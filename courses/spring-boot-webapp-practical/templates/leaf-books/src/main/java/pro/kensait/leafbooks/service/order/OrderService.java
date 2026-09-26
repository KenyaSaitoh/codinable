package pro.kensait.leafbooks.service.order;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import pro.kensait.leafbooks.entity.Book;
import pro.kensait.leafbooks.entity.OrderDetail;
import pro.kensait.leafbooks.entity.OrderDetailPK;
import pro.kensait.leafbooks.entity.OrderTran;
import pro.kensait.leafbooks.entity.Stock;
import pro.kensait.leafbooks.repository.BookRepository;
import pro.kensait.leafbooks.repository.OrderDetailRepository;
import pro.kensait.leafbooks.repository.OrderTranRepository;
import pro.kensait.leafbooks.repository.StockRepository;
// CartItemは同じパッケージ内のため、import不要

@Service
@Transactional
public class OrderService implements OrderServiceIF {
    private static final Logger logger = LoggerFactory.getLogger(
            OrderService.class);

    @Autowired
    private OrderTranRepository orderTranRepos;

    @Autowired
    private OrderDetailRepository orderDetailRepos;

    @Autowired
    private BookRepository bookRepos;

    @Autowired
    private StockRepository stockRepository;

    // サービスメソッド：注文エンティティのリストを取得する（方式1）
    @Override
    public List<OrderTran> getOrderHistory(Integer customerId) {
        logger.info("[ OrderService#findOrderHistory ]");

        // 顧客IDから注文エンティティのリストを取得し、返す
        List<OrderTran> orderTranList =
                orderTranRepos.findByCustomer(customerId);
        return orderTranList;
    }

    // サービスメソッド：注文エンティティのリストを取得する（方式1）
    @Override
    public List<OrderHistoryTO> getOrderHistory2(Integer customerId) {
        logger.info("[ OrderService#findOrderHistory2 ]");

        // 顧客IDから注文エンティティのリストを取得し、返す
        List<OrderHistoryTO> orderHistoryList =
                orderTranRepos.findOrderHistoryTOByCustomer(customerId);
        return orderHistoryList;
    }

    // サービスメソッド：注文エンティティの取得
    @Override
    public OrderTran getOrderTran(Integer orderTranId) {
        logger.info("[ OrderService#getOrderTran ]");

        // 注文IDから注文エンティティと注文明細を一緒に取得し、返す（FETCH JOIN使用）
        OrderTran orderTran = orderTranRepos.findByIdWithDetails(orderTranId);
        if (orderTran == null) {
            throw new RuntimeException("OrderTran not found for ID: " + orderTranId);
        }
        return orderTran;
    }

    // サービスメソッド：注文明細エンティティの取得
    @Override
    public OrderDetail getOrderDetail(OrderDetailPK pk) {
        logger.info("[ OrderService#getOrderDetail ]");

        // 複合主キー（注文IDと注文明細ID）から注文明細エンティティを取得し、返す
        Optional<OrderDetail> orderDetailOpt = orderDetailRepos.findById(pk);
        return orderDetailOpt.orElseThrow();
    }

    // サービスメソッド：注文する（楽観的ロック使用）
    @Override
    public OrderTran orderBooks(OrderTO orderTO) {
        logger.info("[ OrderService#orderBooks ]");

        // カートに追加された書籍毎に、在庫の残り個数をチェックする
        for (CartItem cartItem : orderTO.cartItems()) {

            // 現在の在庫数を取得（最新のバージョン情報を含む）
            Stock currentStock = stockRepository.findById(cartItem.getBookId())
                    .orElseThrow(() -> new RuntimeException("在庫が見つかりません"));

            // 在庫が0未満になる場合は、例外を送出する
            int remaining = currentStock.getQuantity() - cartItem.getCount();
            if (remaining < 0) {
                throw new OutOfStockException(
                        cartItem.getBookId(),
                        cartItem.getBookName(),
                        "在庫不足");
            }

            // 楽観的ロック：現在のStockから取得したVERSION値で在庫を更新
            // updateQuantityメソッドは WHERE s.version = :version 条件でバージョンチェックを行う
            int updateCount = stockRepository.updateQuantity(
                            cartItem.getBookId(),
                            cartItem.getCount(),
                            currentStock.getVersion());  // 現在のバージョンを使用
            
            // バージョン不一致の場合（updateCount = 0）は、他の顧客が在庫を更新済み
            if (updateCount != 1) {
                throw new OptimisticLockException("別の顧客によって在庫が更新されています");
            }
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
        orderTranRepos.save(orderTran);
        
        // OrderDetailを格納するリスト
        List<OrderDetail> orderDetails = new java.util.ArrayList<>();

        // カートアイテム（個々の注文明細）のイテレータを取得する
        List<CartItem> cartItems = orderTO.cartItems();

        // OrderDetailインスタンスの主キー値（注文明細ID）の初期値を設定する
        int orderDetailId = 0;

        for (CartItem cartItem : cartItems) {
            Book book = bookRepos.findById(cartItem.getBookId()).get();

            // OrderDetailインスタンスの主キー値（注文明細ID）をカウントアップする
            orderDetailId = orderDetailId + 1;

            // 新しいOrderDetailインスタンスを生成する
            OrderDetail orderDetail = new OrderDetail(
                    orderTran.getOrderTranId(),
                    orderDetailId,
                    book,
                    cartItem.getCount());

            // OrderDetailインスタンスを保存する
            orderDetailRepos.save(orderDetail);
            
            // リストに追加
            orderDetails.add(orderDetail);
        }
        
        // OrderTranにOrderDetailリストを設定
        orderTran.setOrderDetails(orderDetails);

        // 更新済みのOrderTranを返す
        return orderTran;
    }
}