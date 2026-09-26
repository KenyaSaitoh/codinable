package pro.kensait.leafbooks.service.delivery;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
/**
 * 配送料金を計算するサービスクラス
 */
@Service
public class DeliveryFeeService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryFeeService.class);

    // 配送料金の定数
    private static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("800");
    private static final BigDecimal OKINAWA_DELIVERY_FEE = new BigDecimal("1700");
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("5000");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    // 配送料金の計算
    public BigDecimal calculateDeliveryFee(String deliveryAddress, BigDecimal totalPrice) {
        logger.info("[ DeliveryFeeService#calculateDeliveryFee ] address={}, totalPrice={}", 
                deliveryAddress, totalPrice);

        // 購入金額が5000円以上の場合は送料無料
        if (totalPrice.compareTo(FREE_DELIVERY_THRESHOLD) >= 0) {
            logger.info("[ DeliveryFeeService ] 送料無料（購入金額{}円 >= {}円）", 
                    totalPrice, FREE_DELIVERY_THRESHOLD);
            return ZERO;
        }
        
        // 5000円未満の場合、配送先に応じて料金を決定
        
        // 配送先住所が沖縄県の場合は1700円
        if (deliveryAddress != null && deliveryAddress.startsWith("沖縄県")) {
            logger.info("[ DeliveryFeeService ] 沖縄県への配送料金: {}", OKINAWA_DELIVERY_FEE);
            return OKINAWA_DELIVERY_FEE;
        }
        
        // 通常配送料金は800円
        logger.info("[ DeliveryFeeService ] 通常配送料金: {}", STANDARD_DELIVERY_FEE);
        return STANDARD_DELIVERY_FEE;
    }
    // okinawaの判定
    public boolean isOkinawa(String deliveryAddress) {
        return deliveryAddress != null && deliveryAddress.startsWith("沖縄県");
    }
    // free配送の判定
    public boolean isFreeDelivery(BigDecimal totalPrice) {
        return totalPrice.compareTo(FREE_DELIVERY_THRESHOLD) >= 0;
    }
}

