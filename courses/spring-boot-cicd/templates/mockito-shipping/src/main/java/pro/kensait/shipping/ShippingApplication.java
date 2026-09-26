package pro.kensait.shipping;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
/** テスト時のモックに代えて、固定料金とメモリ内保存で注文を実行する例 */
public class ShippingApplication {
    public static void main(String[] args) {
        List<ShippingOrder> orders = new ArrayList<>();
        ShippingRateClient rateClient = (postalCode, weightGrams) -> 4000;
        ShippingRepository repository = orders::add;
        ShippingService service = new ShippingService(rateClient, repository, Clock.systemUTC());
        ShippingOrder order = service.order("1000001", 2000, Membership.GOLD);
        System.out.println("配送料: " + order.shippingCost() + "円");
        System.out.println("保存した注文: " + orders.size() + "件");
    }
}
