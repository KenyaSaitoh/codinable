package pro.kensait.leafbooks.service.stock;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import pro.kensait.leafbooks.dao.StockDao;
import pro.kensait.leafbooks.entity.Stock;

/**
 * 在庫の照会と更新を行うサービスクラス
 * 在庫の更新は、クライアントが送った version を現在の version と照合してから行う
 */
@ApplicationScoped
public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    @Inject
    private StockDao stockDao;

    public List<Stock> getStocksAll() {
        logger.info("[ StockService#getStocksAll ]");
        return stockDao.findAll();
    }

    public Stock getStock(Integer bookId) {
        logger.info("[ StockService#getStock ] bookId: {}", bookId);
        Stock stock = stockDao.findById(bookId);
        if (stock == null) {
            throw new NotFoundException("在庫情報が見つかりません");
        }
        return stock;
    }

    /**
     * 在庫数の更新
     * @Transactional により、find で得た管理状態の Stock の変更がメソッド終了時のコミットで同期される
     */
    @Transactional
    public Stock updateStock(Integer bookId, Long version, Integer quantity) {
        logger.info("[ StockService#updateStock ] bookId: {}, version: {}, quantity: {}",
                bookId, version, quantity);
        Stock stock = stockDao.findById(bookId);
        if (stock == null) {
            throw new NotFoundException("在庫情報が見つかりません");
        }
        if (!stock.getVersion().equals(version)) {
            logger.warn("Optimistic lock failure for bookId: {}. Expected: {}, Actual: {}",
                    bookId, version, stock.getVersion());
            throw new OptimisticLockException("Version mismatch");
        }
        stock.setQuantity(quantity);
        return stock;
    }
}
