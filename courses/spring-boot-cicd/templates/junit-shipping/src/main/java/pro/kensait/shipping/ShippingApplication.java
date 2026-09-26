package pro.kensait.shipping;
/** JUnitのテスト対象となる配送料割引を実行するコンソール例 */
public class ShippingApplication {
    public static void main(String[] args) {
        ShippingFeePolicy policy = new ShippingFeePolicy();
        int cost = policy.applyDiscount(Membership.GOLD, 4000);
        System.out.println("GOLD会員の配送料: " + cost + "円");
    }
}
