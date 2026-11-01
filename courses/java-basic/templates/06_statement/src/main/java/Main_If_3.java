import java.util.Scanner;

public class Main_If_3 {
    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        System.out.print("totalPrice? => ");
        int totalPrice = Integer.parseInt(new Scanner(System.in).nextLine());

        int deliveryFee;
        if (5000 <= totalPrice) {         // 誤った順序
            deliveryFee = 600;
        } else if (10000 <= totalPrice) { // この分岐には到達しない
            deliveryFee = 300;
        } else {
            deliveryFee = 900;
        }

        System.out.println("deliveryFee => " + deliveryFee);
    }
}