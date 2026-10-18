package pro.kensait.leafbooks.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.leafbooks.common.ErrorResponse;
import pro.kensait.leafbooks.api.dto.OrderDetailResponse;
import pro.kensait.leafbooks.api.dto.OrderHistoryResponse;
import pro.kensait.leafbooks.api.dto.OrderRequest;
import pro.kensait.leafbooks.api.dto.OrderResponse;
import pro.kensait.leafbooks.entity.OrderDetail;
import pro.kensait.leafbooks.entity.OrderTran;
import pro.kensait.leafbooks.security.AuthContext;
import pro.kensait.leafbooks.service.delivery.DeliveryFeeService;
import pro.kensait.leafbooks.service.order.CartItem;
import pro.kensait.leafbooks.service.order.OrderHistoryTO;
import pro.kensait.leafbooks.service.order.OrderServiceIF;
import pro.kensait.leafbooks.service.order.OrderTO;

/**
 * 注文API リソースクラス
 */
@Path("/orders")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    private static final Logger logger = LoggerFactory.getLogger(OrderResource.class);

    @Inject
    private OrderServiceIF orderService;

    @Inject
    private AuthContext authContext;

    @Inject
    private DeliveryFeeService deliveryFeeService;

    /**
     * 注文作成
     */
    @POST
    public Response createOrder(@Valid OrderRequest request) {
        logger.info("[ OrderResource#createOrder ]");

        // JWT認証チェック
        if (!authContext.isAuthenticated()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "Unauthorized",
                    "認証が必要です",
                    "/api/orders"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }

        Integer customerId = authContext.getCustomerId();

        // CartItemRequestをCartItemに変換
        List<CartItem> cartItems = request.cartItems().stream()
                .map(item -> new CartItem(
                        item.bookId(),
                        item.bookName(),
                        item.publisherName(),
                        item.price(),
                        item.count(),
                        false,
                        item.version().longValue()))
                .collect(Collectors.toList());

        // サーバーサイドで配送料を計算
        BigDecimal calculatedDeliveryFee = deliveryFeeService.calculateDeliveryFee(
                request.deliveryAddress(), 
                request.totalPrice());
        
        logger.info("[ OrderResource ] Calculated delivery fee: {}", calculatedDeliveryFee);

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

            return Response.ok(response).build();

        } catch (Exception e) {
            // OutOfStockException, OptimisticLockExceptionは
            // ExceptionMapperで処理される
            throw e;
        }
    }

    /**
     * 注文履歴取得
     */
    @GET
    @Path("/history")
    public Response getOrderHistory() {
        logger.info("[ OrderResource#getOrderHistory ]");

        // JWT認証チェック
        if (!authContext.isAuthenticated()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "Unauthorized",
                    "認証が必要です",
                    "/api/orders/history"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }

        Integer customerId = authContext.getCustomerId();

        // 注文履歴を取得
        List<OrderHistoryTO> orderHistoryList = orderService.getOrderHistory2(customerId);

        // レスポンス生成（内部モデルから外部APIモデルへの変換）
        List<OrderHistoryResponse> response = orderHistoryList.stream()
                .map(history -> new OrderHistoryResponse(
                        history.orderDate(),
                        history.tranId(),      // tranId → orderTranId（API層で明示的に）
                        history.detailId(),    // detailId → orderDetailId（API層で明示的に）
                        history.bookName(),
                        history.publisherName(),
                        history.price(),
                        history.count()))
                .collect(Collectors.toList());

        return Response.ok(response).build();
    }

    /**
     * 注文詳細取得
     */
    @GET
    @Path("/{tranId}")
    public Response getOrderDetail(@PathParam("tranId") Integer tranId) {
        logger.info("[ OrderResource#getOrderDetail ] tranId: {}", tranId);

        // JWT認証チェック
        if (!authContext.isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ErrorResponse.unauthorized("認証が必要です", "/api/orders/" + tranId))
                    .build();
        }

        // 注文が見つからなければ NotFoundException → NotFoundExceptionMapper（404）
        OrderTran orderTran = orderService.getOrderTranWithDetails(tranId);

        // 所有者確認：ログイン中の顧客の注文でなければ 403
        if (!orderTran.getCustomer().equals(authContext.getCustomerId())) {
            throw new ForbiddenException("他の顧客の注文は参照できません");
        }

        OrderResponse response = convertToOrderResponse(orderTran);
        return Response.ok(response).build();
    }

    /**
     * 注文明細取得
     */
    @GET
    @Path("/{tranId}/details/{detailId}")
    public Response getOrderDetailItem(
            @PathParam("tranId") Integer tranId,
            @PathParam("detailId") Integer detailId) {
        logger.info("[ OrderResource#getOrderDetailItem ] tranId: {}, detailId: {}", tranId, detailId);

        // JWT認証チェック
        if (!authContext.isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ErrorResponse.unauthorized("認証が必要です",
                            "/api/orders/" + tranId + "/details/" + detailId))
                    .build();
        }

        // 所有者確認（注文が見つからなければ 404）
        OrderTran orderTran = orderService.getOrderTran(tranId);
        if (!orderTran.getCustomer().equals(authContext.getCustomerId())) {
            throw new ForbiddenException("他の顧客の注文は参照できません");
        }

        // 明細が見つからなければ NotFoundException → 404
        OrderDetail orderDetail = orderService.getOrderDetail(tranId, detailId);

        String publisherName = orderService.getPublisherName(orderDetail.getBookId());

        OrderDetailResponse response = new OrderDetailResponse(
                orderDetail.getOrderDetailId(),
                orderDetail.getBookId(),
                orderDetail.getBookName(),
                publisherName,
                orderDetail.getPrice(),
                orderDetail.getCount()
        );

        return Response.ok(response).build();
    }

    /**
     * OrderTranをOrderResponseに変換
     */
    private OrderResponse convertToOrderResponse(OrderTran orderTran) {
        List<OrderDetailResponse> orderDetails = new ArrayList<>();
        if (orderTran.getOrderDetails() != null) {
            orderDetails = orderTran.getOrderDetails().stream()
                    .map(detail -> {
                        String publisherName = orderService.getPublisherName(detail.getBookId());

                        return new OrderDetailResponse(
                                detail.getOrderDetailId(),
                                detail.getBookId(),
                                detail.getBookName(),
                                publisherName,
                                detail.getPrice(),
                                detail.getCount());
                    })
                    .collect(Collectors.toList());
        }

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

