public class Main_Average_3 {
    public static void main(String[] args) {
        int[] scores = {650, 570, 700};
        double sum = 0;
        for (int score : scores) {
            sum += score;
        }
        double avg = sum / scores.length;
        System.out.println(avg);
    }
}