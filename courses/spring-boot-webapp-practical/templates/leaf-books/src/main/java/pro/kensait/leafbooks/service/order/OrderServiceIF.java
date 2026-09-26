package pro.kensait.leafbooks.service.order;

import java.util.List;

import pro.kensait.leafbooks.entity.OrderDetail;
import pro.kensait.leafbooks.entity.OrderDetailPK;
import pro.kensait.leafbooks.entity.OrderTran;

/*
 * 注文サービス場合の契約を定義するインターフェース
 */
public interface OrderServiceIF {
    // サービスメソッド：注文エンティティのリストを取得する（方式1）
    List<OrderTran> getOrderHistory(Integer customerId);
    // サービスメソッド：注文エンティティのリストを取得する（方式1）
    List<OrderHistoryTO> getOrderHistory2(Integer customerId);
    // サービスメソッド：注文エンティティの取得
    OrderTran getOrderTran(Integer tranId);
    // サービスメソッド：注文明細エンティティの取得
    OrderDetail getOrderDetail(OrderDetailPK pk);
    // サービスメソッド：注文する（楽観的ロック使用）
    OrderTran orderBooks(OrderTO orderTO);
}
