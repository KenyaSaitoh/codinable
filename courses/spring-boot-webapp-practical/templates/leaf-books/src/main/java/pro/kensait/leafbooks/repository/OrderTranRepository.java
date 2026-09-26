package pro.kensait.leafbooks.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pro.kensait.leafbooks.entity.OrderTran;
import pro.kensait.leafbooks.service.order.OrderHistoryTO;

/*
 * 注文tran情報の永続化を担うリポジトリ
 */
@Repository
public interface OrderTranRepository extends JpaRepository<OrderTran, Integer> {
    // 顧客の検索
    @Query("SELECT ot FROM OrderTran ot INNER JOIN ot.orderDetails od "
            + "WHERE ot.customerId = :customerId "
            + "ORDER BY ot.orderDate DESC, ot.orderTranId DESC, od.orderDetailId")
    List<OrderTran> findByCustomer(@Param("customerId") Integer customerId);

    // 注文履歴変換先顧客の検索
    @Query("SELECT new pro.kensait.leafbooks.service.order.OrderHistoryTO "
            + "(ot.orderDate, ot.orderTranId, od.orderDetailId, od.book.bookName, "
            + "od.book.publisher.publisherName, od.price, od.count) "
            + "FROM OrderTran ot INNER JOIN ot.orderDetails od "
            + "WHERE ot.customerId = :customerId "
            + "ORDER BY ot.orderDate DESC, ot.orderTranId DESC, od.orderDetailId")
    List<OrderHistoryTO> findOrderHistoryTOByCustomer(
            @Param("customerId") Integer customerId);

    // orderDetailsを一緒に取得するためのFETCH JOINクエリ
    @Query("SELECT ot FROM OrderTran ot LEFT JOIN FETCH ot.orderDetails od LEFT JOIN FETCH od.book b LEFT JOIN FETCH b.publisher "
            + "WHERE ot.orderTranId = :orderTranId")
    OrderTran findByIdWithDetails(@Param("orderTranId") Integer orderTranId);
}
