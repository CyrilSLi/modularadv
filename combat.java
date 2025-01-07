import java.util.Scanner;
import java.util.Random;
public class combat {
    static Scanner kb = new Scanner(System.in);
    static int[] inv = {1, 1, 3, 10}; // weapon, armor, potions, health
    public static void combat(String enemy, int enHealth) {
        System.out.println("You encounter a " + enemy + ". What do you want to do?");
        while (enHealth > 0 || inv[3] > 0) {
            System.out.println("Action: ");
            String a = kb.nextLine();
            if (a.equals("a")) {
                System.out.println(enemy + " " + 2 * inv[0]);
            }
            
        }
        if (enHealth <= 0) {
            System.out.println("Congratulations! You won!");
            return;
        } else {
            System.out.println("Looks like your adventure ends here.");
            System.exit(0);
        }
    }
    public static void main() {
        inv[2] = 2;
        System.out.println(inv[2]);
    }
}