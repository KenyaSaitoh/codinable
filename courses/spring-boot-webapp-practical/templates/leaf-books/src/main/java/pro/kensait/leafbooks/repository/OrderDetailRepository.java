package pro.kensait.leafbooks.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pro.kensait.leafbooks.entity.OrderDetail;
import pro.kensait.leafbooks.entity.OrderDetailPK;

/*
 * 注文詳細情報の永続化を担うリポジトリ
 */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailPK> {
}
