import java.util.Scanner;

public class Main_Switch_1 {
    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        System.out.print("customerType? (0:GENERAL, 1:GOLD, 2:PLATINUM, 3:DIAMOND) => ");
        int customerType = Integer.parseInt(new Scanner(System.in).nextLine());

        int deliveryFee = 0; //【1】
        switch (customerType) {
        case 0, 1: // 一般会員またはゴールド会員
            deliveryFee = 900;
            break;
        case 2: // プラチナ会員
            deliveryFee = 600;
            break;
        case 3: // ダイヤモンド会員
            deliveryFee = 0;
            break;
        }

        System.out.println("deliveryFee => " + deliveryFee);
    }
}