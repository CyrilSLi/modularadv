/*

Character String[][][] structure:
(other data types must be converted from String)

[0] Characters
[0][x] Character x
[0][x][0] String charName
[0][x][1] Int level
[0][x][2] Int maxHealth
[0][x][3] Int curHealth

[1] Weapons
[1][x] Weapon x
[1][x][0] String weaponName
[1][x][1] Int damage
[1][x][2] Int maxWeaponDur
[1][x][3] Int curWeaponDur
[1][x][4] Double critChance
[1][x][5] Double critMultiplier
[1][x][6] Double blockChance
[1][x][7] Double blockMultiplier
[1][x][8] Double chargeMultiplier

[2] Wardrobe
[2][x] Armor x
[2][x][0] String armorName
[2][x][1] Double DefenseMultiplier
[2][x][2] Int maxArmorDur
[2][x][3] Int curArmorDur

[3] Potions
[3][x] Potion x
[3][x][0] String potionName
[3][x][1] Int amount (if multiplier, set to 0)
[3][x][2] Int multiplier (if amount, set to 1)
[3][x][3] Int modifiedChar (0: Self, 1: Opponent)
[3][x][3] String modifiedValue

[4] Inventory
[1][x] Item x
[1][x][0] String itemName
[1][x][1] Int copies

[5] Attack Patterns
[5][x] Attack pattern x (only one)
[5][x][y] Int actionY

Attack pattern actions:
-3: Charge (must be followed by a non-negative even integer or -1)
-2: Random (from -1 to the last action)
-1: Miss
0 to 2n: Even -> attack, odd -> block using weapon n

The lowest-numbered armor is used first.
*/

import java.util.Scanner;
import java.util.Random;
import java.text.DecimalFormat;
public class modularadv {
    static String[][][] player = new String[0][0][0];
    static Scanner kb = new Scanner(System.in);
    static Random rand = new Random();
    static DecimalFormat df = new DecimalFormat("#.##"); // keep 2 decimal places
    static DecimalFormat dfInt = new DecimalFormat("#"); // no decimal places

    public static int numInput(int minValue, String prompt, int maxValue) {
        while (true) {
            System.out.print(prompt + " (min: " + minValue + ", max: " + maxValue + "): ");
            int num = Integer.parseInt(kb.nextLine());
            if (num >= minValue && num <= maxValue) {
                return num;
            }
            System.out.println("Number out of range, please try again.");
        }
    }
    public static int numInput(int minValue, String prompt) {
        return numInput(minValue, prompt, Integer.MAX_VALUE);
    }
    public static int numInput(String prompt, int maxValue) {
        return numInput(Integer.MIN_VALUE, prompt, maxValue);
    }
    public static int numInput(String prompt) {
        return numInput(Integer.MIN_VALUE, prompt, Integer.MAX_VALUE);
    }

    public static int choiceNumInput(String[] options, String prompt) {
        System.out.println("\nNumber\tOption");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + "\t" + options[i]);
        }
        return numInput(1, prompt, options.length) - 1;
    }
    public static String choiceInput(String[] options, String prompt) {
        return options[choiceNumInput(options, prompt)];
    }

    public static String getValue(String[][][] array, int index, String value) {
        String[][] values = {
            {"charName", "level", "maxHealth", "curHealth"},
            {"weaponName", "damage", "maxWeaponDur", "curWeaponDur", "critChance", "critMultiplier", "blockChance", "blockMultiplier", "chargeMultiplier"},
            {"armorName", "DefenseMultiplier", "maxArmorDur", "curArmorDur"},
            {"potionName", "amount", "multiplier", "firstIndex", "secondIndex", "thirdIndex"},
            {"itemName", "copies"}
        };
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                if (value.equals(values[i][j])) {
                    return array[i][index][j];
                }
            }
        }
        return "";
    }

    public static String[][][] delItem(String[][][] array, int index, String category) {
        String[] categories = {"Character", "Weapon", "Armor", "Potion", "Inventory"};
        int catIndex = -1;
        for (int i = 0; i < categories.length; i++) {
            if (category.equals(categories[i])) {
                catIndex = i;
                break;
            }
        }
        String[][][] newArray = new String[array.length][][];
        for (int i = 0; i < array.length; i++) {
            if (i == catIndex) {
                newArray[i] = new String[array[i].length - 1][];
                for (int j = 0; j < array[i].length; j++) {
                    if (j < index) {
                        newArray[i][j] = array[i][j];
                    } else if (j > index) {
                        newArray[i][j - 1] = array[i][j];
                    }
                }
            } else {
                newArray[i] = array[i];
            }
        }
        return newArray;
    }

    public static int chooseWeapon() {
        String[] weapons = new String[player[1].length];
        for (int i = 0; i < player[1].length; i++) {
            weapons[i] = getValue(player, i, "weaponName");
        }
        return choiceNumInput(weapons, "Please choose a weapon");
    }

    public static String[] combat(String[][][] enemy) {
        for (int i = 0; i <= 2; i++) { // Reset
            for (int j = 0; j < player[i].length; j++) {
                player[i][j][3] = player[i][j][2];
            }
        }

        String charName = getValue(enemy, 0, "charName");
        if (charName.substring(1).equals("A") ||
            charName.substring(1).equals("E") ||
            charName.substring(1).equals("I") ||
            charName.substring(1).equals("O") ||
            charName.substring(1).equals("U")) {
            System.out.println("You encounter an " + charName + ". What do you want to do?");
        } else {
            System.out.println("You encounter a " + charName + ". What do you want to do?");
        }

        String[] actions = {"Attack", "Block", "Charge", "Use Potion"};
        boolean charging = false, enemyCharging = false;
        int blocking = -1, enemyBlocking = -1;

        int enemyPatternIndex = -1;
        while (true) {
            String action;
            if (charging) {
                action = "Attack";
            } else {
                action = choiceInput(actions, "Please choose an action");
            }

            if (action.equals("Attack")) {
                int weapon = chooseWeapon();

                double critChance = Double.parseDouble(getValue(player, weapon, "critChance"));
                double critMultiplier = Double.parseDouble(getValue(player, weapon, "critMultiplier"));
                double chargeMultiplier = Double.parseDouble(getValue(player, weapon, "chargeMultiplier"));
                double damage = Double.parseDouble(getValue(player, weapon, "damage"));

                if (charging) {
                    damage *= chargeMultiplier;
                    charging = false;
                    System.out.println("Charged attack!");
                }
                if (rand.nextDouble() < critChance) {
                    damage *= critMultiplier;
                    System.out.println("Critical hit!");
                }
                if (enemyBlocking > -1) {
                    damage *= Double.parseDouble(getValue(enemy, enemyBlocking, "blockMultiplier"));
                }

                player[1][weapon][3] = dfInt.format(Integer.parseInt(getValue(player, weapon, "curWeaponDur")) - 1);
                enemy[2][0][3] = dfInt.format(Integer.parseInt(getValue(enemy, 0, "curArmorDur")) - 1);
                if (enemy[2][0][3].equals("0")) {
                    System.out.println(charName + "'s armor '" + getValue(enemy, 0, "armorName") + "' is broken!");
                    enemy = delItem(enemy, 0, "Armor");
                }
                


            } else if (action.equals("Block")) {
                blocking = chooseWeapon();
                System.out.println("Blocking the enemy's next attack!");

            } else if (action.equals("Charge")) {
                charging = true;
                System.out.println("Charging up for the next attack!");

            } else if (action.equals("Use Potion")) {

            }

            if (enemy[5][0].length > enemyPatternIndex - 1) {
                enemyPatternIndex++;
            } else {
                enemyPatternIndex = 0;
            }
            int enemyAction = Integer.parseInt(enemy[5][0][enemyPatternIndex]);
            if (enemyAction == -2) {
                enemyAction = rand.nextInt(enemy[1].length * 2 + 1) - 1; 
            }

            if (enemyAction == -3) {
                enemyCharging = true;
                System.out.println(charName + " is charging up for the next attack!");

            } else if (enemyAction == -1) {
                enemyCharging = false;
                enemyBlocking = -1;
                System.out.println(charName + " misses the attack!");

            } else if (enemyAction % 2 == 0) {

            } else {
                enemyBlocking = enemyAction / 2;
                System.out.println(charName + " is blocking your next attack!");
            }
            

            break;
        }

        return new String[1];
    }
    public static void showStats() {
        System.out.println("\nPlayer Stats");
        System.out.print("Name: ");
        System.out.println(getValue(player, 0, "charName"));
        System.out.print("Level: ");
        System.out.println(getValue(player, 0, "level"));
        System.out.print("Health: ");
        System.out.println(getValue(player, 0, "curHealth") + "/" + getValue(player, 0, "maxHealth"));
        System.out.println("\nWeapons");
        for (int i = 0; i < player[1].length; i++) {
            System.out.print(getValue(player, i, "weaponName") + " (");
            System.out.print(getValue(player, i, "curWeaponDur") + "/" + getValue(player, i, "maxWeaponDur") + ")");
        }
        System.out.println("\n\nArmor");
        for (int i = 0; i < player[2].length; i++) {
            System.out.print(getValue(player, i, "armorName") + " (");
            System.out.print(getValue(player, i, "curArmorDur") + "/" + getValue(player, i, "maxArmorDur") + ")");
        }
        System.out.print("\n\nPotions: " + getValue(player, 0, "potionName"));
        for (int i = 1; i < player[3].length; i++) {
            System.out.print(", " + getValue(player, 3, "potionName"));
        }
        System.out.println("");
    }
    public static void main(String[] args) {
        player = new String[][][] {
            {{"Player", "1", "10", "10"}},
            {
                {"Dagger", "1", "10", "10", "0.5", "1.5", "0.9", "0.2", "3"},
                {"Sword", "2", "10", "10", "0.1", "1.5", "0.9", "0.2", "3"}
            },
            {{"Leather Armor", "0.5", "10", "10"}},
            {{"Health Potion", "10", "0", "0", "0", "curHealth"}},
        };
        String[][][] enemy = new String[][][] {
            {{"Goblin", "1", "10", "10"}},
            {
                {"Dagger", "1", "10", "10", "0.5", "1.5", "0.9", "0.2", "3"},
                {"Sword", "2", "10", "10", "0.1", "1.5", "0.9", "0.2", "3"}
            },
            {{"Leather Armor", "0.5", "10", "10"}},
            {{"Health Potion", "10", "0", "0", "0", "curHealth"}},
        };
        showStats();
        player = delItem(player, 1, "Weapon");
        showStats();
        combat(enemy);
    }
}