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
[1][x][2] Int maxWeaponDur // not implemented for enemies
[1][x][3] Int curWeaponDur // not implemented for enemies
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
[3][x][1] Int amount (positive)
[3][x][2] Int firstIndex
[3][x][3] Int secondIndex
[3][x][4] Int thirdIndex

[4] Inventory
[1][x] Item x
[1][x][0] String itemName

[5] Attack Patterns
[5][x] Attack pattern x (only one)
[5][x][y] Int actionY

Attack pattern actions:
-3: Charge (must be followed by a non-negative even integer or -1)
-2: Random (from -1 to the last action)
-1: Miss
0 to 2n: Even -> attack, odd -> block using weapon n

The lowest-numbered armor is used first.



Room args format:

[0] Metadata
[0][0] String roomType
[0][1] int addScore
[0][2] String itemGained
[0][3-x] String[] itemsRequired

[1-x] Room-specific

*/

import java.util.Scanner;
import java.util.Random;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
public class modularadv {
    static String[][][] player = new String[0][0][0];
    static Scanner kb = new Scanner(System.in);
    static Random rand = new Random();
    static DecimalFormat df = new DecimalFormat("#.##"); // keep 2 decimal places
    static DecimalFormat dfInt = new DecimalFormat("#"); // no decimal places
    static int posX = 0, posY = 0, score = 0;
    static boolean easyMode = false;

    static String[][][] goblinEnemy = new String[][][] {
        {{"Goblin", "1", "100", "100"}},
        {
            {"Dagger", "10", "10", "10", "0.5", "1.5", "0.9", "0.2", "3"},
            {"Sword", "20", "10", "10", "0.1", "1.5", "0.9", "0.2", "3"}
        },
        {{"Leather Armor", "0.5", "10", "10"}},
        {},
        {},
        {{"-2", "-2", "-3", "0"}}
    };
    static String[][][][] enemys = {
        goblinEnemy
    };

    public static Boolean checkInvItem(String item) {
        for (int i = 0; i < player[4].length; i++) {
            if (item.equals(getValue(player, i, "itemName"))) {
                return true;
            }
        }
        return false;
    }
    public static void delInvItem(String item) {
        for (int i = 0; i < player[4].length; i++) {
            if (item.equals(getValue(player, i, "itemName"))) {
                player = delItem(player, i, "Inventory");
            }
        }
    }
    public static void addInvItem(String item) {
        for (int i = 0; i < player[4].length; i++) {
            if (item.equals(getValue(player, i, "itemName"))) {
                System.out.println("You cannot carry more than one of the same item.");
                return;
            }
        }
        player = addItem(player, new String[] {item}, "Inventory");
    }

    public static int numInput(int minValue, String prompt, int maxValue) {
        while (true) {
            System.out.print(prompt + " (min: " + minValue + ", max: " + maxValue + "): ");
            String numStr = kb.nextLine();
            if (numStr.equals("")) {
                System.out.println("Invalid input, please try again.");
                continue;
            }
            int num = Integer.parseInt(numStr);
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
        try {
            return options[choiceNumInput(options, prompt)];
        } catch (java.lang.NumberFormatException e) {
            return ""; // invalid input
        }
    }

    public static String getValue(String[][][] array, int index, String value) {
        String[][] values = {
            {"charName", "level", "maxHealth", "curHealth"},
            {"weaponName", "damage", "maxWeaponDur", "curWeaponDur", "critChance", "critMultiplier", "blockChance", "blockMultiplier", "chargeMultiplier"},
            {"armorName", "DefenseMultiplier", "maxArmorDur", "curArmorDur"},
            {"potionName", "amount", "firstIndex", "secondIndex", "thirdIndex"},
            {"itemName"}
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

    public static void chooseChar() {
        String[] characters = new String[player[0].length];
        for (int i = 0; i < player[0].length; i++) {
            characters[i] = getValue(player, i, "charName");
        }
        int charIndex = choiceNumInput(characters, "Please choose a character");
        String[] charObj = player[0][charIndex];
        player = delItem(player, charIndex, "Character");
        player = addItem(player, charObj, "Character");
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

    public static String[][][] addItem(String[][][] array, String[] item, String category) {
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
                newArray[i] = new String[array[i].length + 1][];
                for (int j = 0; j < array[i].length; j++) {
                    newArray[i][j] = array[i][j];
                }
                newArray[i][array[i].length] = item;
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
    public static int choosePotion() {
        String[] potions = new String[player[3].length];
        for (int i = 0; i < player[3].length; i++) {
            potions[i] = getValue(player, i, "potionName");
        }
        return choiceNumInput(potions, "Please choose a potion");
    }

    public static double calcDamage (String[][][] attacker, String[][][] opponent, int weapon, boolean charging, int opponentBlocking) {
        double critChance = Double.parseDouble(getValue(attacker, weapon, "critChance"));
        double critMultiplier = Double.parseDouble(getValue(attacker, weapon, "critMultiplier"));
        double chargeMultiplier = Double.parseDouble(getValue(attacker, weapon, "chargeMultiplier"));
        double damage = Double.parseDouble(getValue(attacker, weapon, "damage"));

        if (charging) {
            damage *= chargeMultiplier;
            System.out.println("Charged attack!");
        }
        if (rand.nextDouble() < critChance) {
            damage *= critMultiplier;
            System.out.println("Critical hit!");
        }
        if (opponentBlocking > -1) {
            double blockChance = Double.parseDouble(getValue(opponent, opponentBlocking, "blockChance"));
            if (rand.nextDouble() < blockChance) {
                damage *= Double.parseDouble(getValue(opponent, opponentBlocking, "blockMultiplier"));
                System.out.println("Attack blocked!");
            } else {
                System.out.println("Attack block failed!");
            }
        }
        return damage;
    }

    /**
     * Travel through a maze
     * @param maze
     * @param args [int addScore]
     */
    public static void travelMaze(String[][] maze, String[] args) {
        int mazeX = 0, mazeY = 0;
        while (mazeX != maze.length - 1 || mazeY != maze[0].length - 1) {
            
        }
    }

    public static Boolean completePattern(String[][] args) {
        int addScore = Integer.parseInt(args[0][1]);
        Boolean success = true;
        for (int i = 1; i < args.length; i++) {
            System.out.print ("Pattern " + i + ": " + args[i][0]);
            for (int j = 1; j < args[i].length - 1; j++) {
                System.out.print(", " + args[i][j]);
            }
            int nextNum;
            while (true) {
                System.out.print("\nWhat's the next number in the pattern? ");
                String nextStr = kb.nextLine();
                if (!nextStr.equals("")) {
                    nextNum = Integer.parseInt(nextStr);
                    break;
                }
                System.out.println("Invalid input, please try again.");
            }
            if (nextNum == Integer.parseInt(args[i][args[i].length - 1])) {
                System.out.println("Correct!");
            } else {
                System.out.println("Incorrect. The correct answer is " + args[i][args[i].length - 1] + ".");
                success = false;
            }
        }
        return success;
    }

    public static Boolean voidRoom(String[][] args) {
        if (args.length == 1) { // has story
            for (String line : args[0]) {
                System.out.println(line);
            }
        }
        return true;
    }

    /**
     * 
     * @param cost
     * @param addScore
     * @param winChance
     */
    public static Boolean lottery(String[][] args) {
        int cost = Integer.parseInt(args[1][0]);
        double winChance = Double.parseDouble(args[1][1]);
        Boolean success = false;

        System.out.println("This lottery costs " + cost + " score points.");
        score -= cost;
        if (rand.nextDouble() < winChance) {
            System.out.println("Congratulations! You won the lottery!");
            success = true;
        } else {
            System.out.println("Sorry, you lost the lottery.");
            
        }
        return success;
    }

    public static Boolean exitDoor(String[][] args) {
        int required = Integer.parseInt(args[1][0]);
        if (score >= required) {
            System.out.println("You have enough points to escape.");
            System.out.println("Congratulations! You have won the game!");
            System.exit(0);
        }
        System.out.println("You do not have enough points to escape.");
        System.out.println("Please try again.");
        return false;
    }

    public static void move(String[][][][]rooms) {
        while (true) {
            System.out.print("Enter a direction or action: ");
            String dir = kb.nextLine().toLowerCase();
            if ((dir.equals("s") || dir.equals("down")) && posY > 0) {
                if (rooms[posX][posY - 1] != null) {
                    posY--;
                    break;
                }
            } else if ((dir.equals("a") || dir.equals("left")) && posX > 0) {
                if (rooms[posX - 1][posY] != null) {
                    posX--;
                    break;
                }
            } else if (dir.equals("w") || dir.equals("up")) {
                if (rooms[posX].length > posY + 1 && rooms[posX][posY + 1] != null) {
                    posY++;
                    break;
                }
            } else if (dir.equals("d") || dir.equals("right")) {
                if (rooms.length > posX + 1 && rooms[posX + 1][posY] != null) {
                    posX++;
                    break;
                }
            } else if (dir.equals("t") || dir.equals("stats")) {
                showStats();
                break;
            } else if (dir.equals("c") || dir.equals("char")) {
                chooseChar();
                break;
            } else if (dir.equals ("i") || dir.equals("in")) {
                visitRoom(rooms[posX][posY]);
                continue;
            } else if (dir.equals("h") || dir.equals("help")) {
                System.out.println();
                System.out.println("h / help: Show help");
                System.out.println("s / down: Move down");
                System.out.println("w / up: Move up");
                System.out.println("a / left: Move left");
                System.out.println("d / right: Move right");
                System.out.println("t / stats: Show stats");
                System.out.println("c / char: Change character");
                System.out.println("i / in: Enter room");
                System.out.println();

                continue;
            }
            System.out.println("Invalid direction. please try again, or type 'h' for help.");
        }
    }

    public static Boolean combat(String[][][] enemy) {
        for (int i = 0; i <= 2; i++) { // Reset
            for (int j = 0; j < player[i].length; j++) {
                player[i][j][3] = player[i][j][2];
            }
            for (int k = 0; k < enemy[i].length; k++) {
                enemy[i][k][3] = enemy[i][k][2];
            }
        }

        String charName = getValue(enemy, 0, "charName");
        if (charName.substring(1).equals("A") ||
            charName.substring(1).equals("E") ||
            charName.substring(1).equals("I") ||
            charName.substring(1).equals("O") ||
            charName.substring(1).equals("U")) {
            System.out.println("\nYou encounter an " + charName + ". What do you want to do?");
        } else {
            System.out.println("\nYou encounter a " + charName + ". What do you want to do?");
        }

        String[] actions = {"Attack", "Block", "Charge", "Use Potion", "Show Stats (costs a turn)"};
        boolean charging = false, enemyCharging = false;
        int blocking = -1, enemyBlocking = -1, invalidTries = 0;
        Boolean success = false;

        int enemyPatternIndex = -1;
        while (true) {
            System.out.println("\nYour health: " + getValue(player, 0, "curHealth") + "/" + getValue(player, 0, "maxHealth"));
            System.out.println(charName + "'s health: " + getValue(enemy, 0, "curHealth") + "/" + getValue(enemy, 0, "maxHealth"));
            String action;
            if (charging) {
                action = "Attack";
            } else {
                action = choiceInput(actions, "Please choose an action");
            }

            if (action.equals("Attack")) {
                if (player[1].length == 0) {
                    System.out.println("You have no weapons! Please choose another action.");
                    continue;
                }
                int weapon = chooseWeapon();
                double damage = calcDamage(player, enemy, weapon, charging, enemyBlocking);
                enemyBlocking = -1;
                charging = false;
                invalidTries = 0;

                player[1][weapon][3] = dfInt.format(Integer.parseInt(getValue(player, weapon, "curWeaponDur")) - 1);
                if (enemy[2].length > 0) {
                    damage *= Double.parseDouble(getValue(enemy, 0, "DefenseMultiplier"));
                    enemy[2][0][3] = dfInt.format(Integer.parseInt(getValue(enemy, 0, "curArmorDur")) - 1);
                }
                if (player[1][weapon][3].equals("0")) {
                    System.out.println("Your weapon '" + getValue(player, weapon, "weaponName") + "' is broken!");
                    player = delItem(player, weapon, "Weapon");
                }

                int damageInt = (int) Math.round(damage);
                System.out.println("You deal " + damageInt + " damage to " + charName + "!");
                enemy[0][0][3] = dfInt.format(Integer.parseInt(getValue(enemy, 0, "curHealth")) - damageInt);

            } else if (action.equals("Block")) {
                blocking = chooseWeapon();
                charging = false;
                invalidTries = 0;
                System.out.println("Blocking the enemy's next attack!");

            } else if (action.equals("Charge")) {
                blocking = -1;
                charging = true;
                invalidTries = 0;
                System.out.println("Charging up for the next attack!");

            } else if (action.equals("Use Potion")) {
                blocking = -1;
                charging = false;
                invalidTries = 0;
                int potion = choosePotion();
                player = delItem(player, potion, "Potion");
                int i1 = Integer.parseInt(getValue(player, potion, "firstIndex")),
                    i2 = Integer.parseInt(getValue(player, potion, "secondIndex")),
                    i3 = Integer.parseInt(getValue(player, potion, "thirdIndex"));
                player[i1][i2][i3] = dfInt.format(Integer.parseInt(player[i1][i2][i3]) + Integer.parseInt(getValue(player, potion, "amount")));

            } else if (action.equals("Show Stats (costs a turn)")) {
                blocking = -1;
                charging = false;
                invalidTries = 0;
                showStats();

            } else {
                invalidTries++;
                if (invalidTries >= 3) {
                    blocking = -1;
                    charging = false;
                    invalidTries = 0;
                    System.out.println("Your turn is skipped due to too many invalid actions!");
                } else {
                    System.out.println("Invalid action, please try again.");
                    continue;
                }
            }
            
            if (Integer.parseInt(getValue(enemy, 0, "curHealth")) <= 0) {
                System.out.println("Congratulations! You defeated " + charName + "!");
                success = true;
                break;
            }
            if (enemy[2].length > 0 && enemy[2][0][3].equals("0")) {
                System.out.println(charName + "'s armor '" + getValue(enemy, 0, "armorName") + "' is broken!");
                enemy = delItem(enemy, 0, "Armor");
            }

            if (enemy[5][0].length > enemyPatternIndex + 1) {
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
                System.out.println(charName + " attacks you using " + getValue(enemy, enemyAction / 2, "weaponName") + "!");
                double damage = calcDamage(enemy, player, enemyAction / 2, enemyCharging, blocking);
                blocking = -1;
                enemyCharging = false;

                if (player[2].length > 0) {
                    damage *= Double.parseDouble(getValue(player, 0, "DefenseMultiplier"));
                    player[2][0][3] = dfInt.format(Integer.parseInt(getValue(player, 0, "curArmorDur")) - 1);
                }

                int damageInt = (int) Math.round(damage);
                System.out.println(charName + " deals " + damageInt + " damage to you!");
                player[0][0][3] = dfInt.format(Integer.parseInt(getValue(player, 0, "curHealth")) - damageInt);

                if (Integer.parseInt(getValue(player, 0, "curHealth")) <= 0) {
                    System.out.println("You have been defeated by " + charName + "!");
                    break;
                }
                if (player[2].length > 0 && player[2][0][3].equals("0")) {
                    System.out.println("Your armor '" + getValue(player, 0, "armorName") + "' is broken!");
                    player = delItem(player, 0, "Armor");
                }

            } else {
                enemyCharging = false;
                enemyBlocking = enemyAction / 2;
                System.out.println(charName + " misses the attack!"); // Do not tell the player of blocking action
            }
        }

        return success;
    }
    public static void showStats() {
        System.out.println("\nScore: " + score);
        System.out.println("Player Stats");
        System.out.print("Name: ");
        System.out.println(getValue(player, 0, "charName"));
        System.out.print("Level: ");
        System.out.println(getValue(player, 0, "level"));
        System.out.print("Health: ");
        System.out.println(getValue(player, 0, "curHealth") + "/" + getValue(player, 0, "maxHealth"));
        System.out.println("\nWeapons");
        for (int i = 0; i < player[1].length; i++) {
            System.out.print(getValue(player, i, "weaponName") + " (");
            System.out.println(getValue(player, i, "curWeaponDur") + "/" + getValue(player, i, "maxWeaponDur") + ")");
        }
        System.out.println("\nArmor");
        for (int i = 0; i < player[2].length; i++) {
            System.out.print(getValue(player, i, "armorName") + " (");
            System.out.println(getValue(player, i, "curArmorDur") + "/" + getValue(player, i, "maxArmorDur") + ")");
        }
        if (player[3].length == 0) {
            System.out.println("\nPotions:");
            return;
        }
        System.out.print("\nPotions: " + getValue(player, 0, "potionName"));
        for (int i = 1; i < player[3].length; i++) {
            System.out.print(", " + getValue(player, i, "potionName"));
        }
        System.out.println("\n");
    }

    public static void visitRoom(String[][]room) {
        String roomType = room[0][0];
        for (int i = 3; i < room[0].length; i++) {
            if (!checkInvItem(room[0][i])) {
                System.out.println("You do not have the required item " + room[0][i] + " to enter this room.");
                return;
            }
        }
        System.out.println("You are entering a " + roomType + " room.");
        Boolean success = false;
        if (roomType.equals("void")) {
            success = voidRoom(room);
        } else if (roomType.equals("completePattern")) {
            success = completePattern(room);
        } else if (roomType.equals("lottery")) {
            success = lottery(room);
        } else if (roomType.equals("combat")) {
            success = combat(enemys[Integer.parseInt(room[1][0])]);
        } else if (roomType.equals("exitDoor")) {
            success = exitDoor(room);
        } else {
            System.out.println("Invalid room type: " + roomType);
            System.exit(1);
        }
        if (success) {
            System.out.println("You have successfully completed the room!");
            System.out.println("You gain " + room[0][1] + " score points.");
            score += Integer.parseInt(room[0][1]);
            System.out.println("You find a " + room[0][2] + " in the room.");
            if (!room[0][2].equals("")) {
                addInvItem(room[0][2]);
            }
        } else {
            System.out.println("You have failed the room.");
        }
        if (score < 0) {
            System.out.println("You have negetive score! Game over.");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        player = new String[][][] {
            {{"Player", "1", "100", "100"}},
            {
                {"Dagger", "10", "10", "10", "0.5", "1.5", "0.9", "0.2", "3"},
                {"Sword", "20", "10", "10", "0.1", "1.5", "0.9", "0.2", "3"}
            },
            {{"Leather Armor", "0.5", "10", "10"}},
            {
                {"Health Potion", "10", "0", "0", "3"},
                {"Health Potion", "10", "0", "0", "3"},
                {"Dagger Repair Kit", "10", "1", "0", "3"},
                {"Sword Repair Kit", "10", "1", "1", "3"}
            },
            {},
            {}
        };
        String[][] pattern1 = new String[][] {
            {"completePattern", "10", "logicOrb", "dummyItem"},
            {"1", "2", "3", "4", "5"},
            {"10", "20", "30", "40", "50"},
            {"1, 3, 5, 7, 9", "11"},
            {"1, 4, 9, 16, 25", "36"},
            {"1, 2, 4, 8, 16", "32"},
            {"1, 1, 2, 3, 5", "8"},
        };
        String[][] startRoom = new String[][] {
            {"void", "0", "dummyItem"}
        };
        String[][] pointRoom = new String[][] {
            {"void", "10", "dummyItem"}
        };
        String[][] key1 = new String[][] {
            {"lottery", "10", "key1", "logicOrb"},
            {"10", "0.1"}
        };
        String[][] goblinFight1 = new String[][] {
            {"combat", "100", "goblinSkin", "key1"},
            {"0"}
        };
        String[][] exit1 = new String[][] {
            {"exitDoor", "0", "", "goblinSkin"},
            {"300"}
        };
        String[][][][] rooms = new String[][][][] {
            {startRoom, null, startRoom, startRoom, key1},
            {exit1, startRoom, startRoom, startRoom, null},
            {startRoom, null, goblinFight1, null, startRoom},
            {null, pointRoom, startRoom, pattern1, startRoom},

        };
        long delay = 300;
        try {
            System.out.println("Welcome to Modular Adventure!");
            TimeUnit.MILLISECONDS.sleep(delay);
            System.out.println("You are in a grid of rooms. Each room has a different challenge.");
            TimeUnit.MILLISECONDS.sleep(delay);
            System.out.println("Rooms can give and require items to enter, while some rooms just contain story.");
            TimeUnit.MILLISECONDS.sleep(delay);
            System.out.println("The goal is to find and complete the exit room to escape the grid.");
            TimeUnit.MILLISECONDS.sleep(delay);
            System.out.println("There is a score system. Rooms give points for completing them and you need a certain amount to exit.");
            TimeUnit.MILLISECONDS.sleep(delay);
            System.out.println("If at any point you have negative score you lose.");
            TimeUnit.MILLISECONDS.sleep(delay);
            System.out.println("Rooms will not tell you how much score they give and require, check for story rooms for hints.");
            TimeUnit.MILLISECONDS.sleep(delay);
            System.out.println("Enable easy mode to be able to view a map of the grid.");
        } catch (InterruptedException e) {}
        while (true) {
            move(rooms);
        }
    }
}