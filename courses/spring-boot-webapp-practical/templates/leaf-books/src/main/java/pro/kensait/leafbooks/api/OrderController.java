package pro.kensait.leafbooks.api;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.OptimisticLockException;
import pro.kensait.leafbooks.api.dto.OrderDetailResponse;
import pro.kensait.leafbooks.api.dto.OrderHistoryResponse;
import pro.kensait.leafbooks.api.dto.OrderRequest;
import pro.kensait.leafbooks.api.dto.OrderResponse;
import pro.kensait.leafbooks.entity.OrderDetail;
import pro.kensait.leafbooks.entity.OrderDetailPK;
import pro.kensait.leafbooks.entity.OrderTran;
import pro.kensait.leafbooks.security.JwtAuthenticationToken;
import pro.kensait.leafbooks.service.delivery.DeliveryFeeService;
import pro.kensait.leafbooks.service.order.OrderHistoryTO;
import pro.kensait.leafbooks.service.order.OrderServiceIF;
import pro.kensait.leafbooks.service.order.OrderTO;
import pro.kensait.leafbooks.service.order.OutOfStockException;
import pro.kensait.leafbooks.service.order.CartItem;

/*
 * 注文機能のコントローラー
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderServiceIF orderService;

    @Autowired
    private DeliveryFeeService deliveryFeeService;

    // 注文作成
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Validated @RequestBody OrderRequest request) {
        logger.info("[ OrderController#createOrder ]");

        // SecurityContextから認証情報を取得
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        Integer customerId = authentication.getCustomerId();

        // CartItemRequestをCartItemに変換
        // versionは注文処理時にStockテーブルから取得されるため、ここでは0Lを設定
        List<CartItem> cartItems = request.cartItems().stream()
                .map(item -> new CartItem(
                        item.bookId(),
                        item.bookName(),
                        item.publisherName(),
                        item.price(),
                        item.count(),
                        false,
                        0L))
                .collect(Collectors.toList());

        // サーバーサイドで配送料を計算
        // クライアントから送られた値は使用せず、サーバーで計算した値を使用
        java.math.BigDecimal calculatedDeliveryFee = deliveryFeeService.calculateDeliveryFee(
                request.deliveryAddress(), 
                request.totalPrice());
        
        logger.info("[ OrderController ] Calculated delivery fee: {}", calculatedDeliveryFee);

        // OrderTOを生成（サーバーで計算した配送料を使用）
        OrderTO orderTO = new OrderTO(
                customerId,
                LocalDate.now(),
                cartItems,
                request.totalPrice(),
                calculatedDeliveryFee,  // クライアントの値ではなく、サーバーで計算した値を使用
                request.deliveryAddress(),
                request.settlementType()
        );

        try {
            // 注文処理を実行
            OrderTran orderTran = orderService.orderBooks(orderTO);

            // レスポンス生成
            OrderResponse response = convertToOrderResponse(orderTran);

            return ResponseEntity.ok(response);

        } catch (OutOfStockException | OptimisticLockException e) {
            // グローバル例外ハンドラで処理される
            throw e;
        }
    }

    // 注文履歴取得
    @GetMapping("/history")
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistory() {
        logger.info("[ OrderController#getOrderHistory ]");

        // SecurityContextから認証情報を取得
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        Integer customerId = authentication.getCustomerId();

        // 注文履歴を取得
        List<OrderHistoryTO> orderHistoryList = orderService.getOrderHistory2(customerId);

        // レスポンス生成
        List<OrderHistoryResponse> response = orderHistoryList.stream()
                .map(history -> new OrderHistoryResponse(
                        history.orderDate(),
                        history.tranId(),
                        history.detailId(),
                        history.bookName(),
                        history.publisherName(),
                        history.price(),
                        history.count()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 注文詳細取得
    @GetMapping("/{tranId}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable Integer tranId) {
        logger.info("[ OrderController#getOrderDetail ] tranId: {}", tranId);

        OrderTran orderTran = orderService.getOrderTran(tranId);
        OrderResponse response = convertToOrderResponse(orderTran);

        return ResponseEntity.ok(response);
    }

    // 注文明細取得
    @GetMapping("/{tranId}/details/{detailId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetailItem(
            @PathVariable Integer tranId,
            @PathVariable Integer detailId) {
        logger.info("[ OrderController#getOrderDetailItem ] tranId: {}, detailId: {}", tranId, detailId);

        OrderDetailPK pk = new OrderDetailPK(tranId, detailId);
        OrderDetail orderDetail = orderService.getOrderDetail(pk);

        OrderDetailResponse response = new OrderDetailResponse(
                orderDetail.getOrderDetailId(),
                orderDetail.getBook().getBookId(),
                orderDetail.getBook().getBookName(),
                orderDetail.getBook().getPublisher().getPublisherName(),
                orderDetail.getPrice(),
                orderDetail.getCount()
        );

        return ResponseEntity.ok(response);
    }

    // OrderTranをOrderResponseに変換
    private OrderResponse convertToOrderResponse(OrderTran orderTran) {
        List<OrderDetailResponse> orderDetails = orderTran.getOrderDetails().stream()
                .map(detail -> new OrderDetailResponse(
                        detail.getOrderDetailId(),
                        detail.getBook().getBookId(),
                        detail.getBook().getBookName(),
                        detail.getBook().getPublisher().getPublisherName(),
                        detail.getPrice(),
                        detail.getCount()))
                .collect(Collectors.toList());

        return new OrderResponse(
                orderTran.getOrderTranId(),
                orderTran.getOrderDate(),
                orderTran.getTotalPrice(),
                orderTran.getDeliveryPrice(),
                orderTran.getDeliveryAddress(),
                orderTran.getSettlementType(),
                orderDetails
        );
    }
}

