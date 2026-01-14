import java.io.*;
import java.util.*;

public class Main {

    private static final String DATA_FILE = "users_data.txt";
    private static final Map<String, User> users = new HashMap<>();
    private static User currentUser = null;

    public static void main(String[] args) {
        loadAllUsers();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Личный финансовый менеджер v0.4");
        System.out.println("Данные загружены из " + DATA_FILE + " (" + users.size() + " пользователей)");
        System.out.println("Команды:");
        System.out.println("  register <логин> <пароль>");
        System.out.println("  login <логин> <пароль>");
        System.out.println("  logout");
        System.out.println("  income <сумма> <категория> <описание>");
        System.out.println("  expense <сумма> <категория> <описание>");
        System.out.println("  status");
        System.out.println("  exit");

        while (true) {
            System.out.print(getPrompt());
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "register":
                        handleRegister(parts);
                        break;
                    case "login":
                        handleLogin(parts);
                        break;
                    case "logout":
                        handleLogout();
                        break;
                    case "income":
                    case "expense":
                    case "status":
                        if (!isLoggedIn()) break;
                        handleFinanceCommand(command, parts);
                        break;
                    case "exit":
                        saveAllUsers();
                        System.out.println("Данные сохранены. До свидания!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Неизвестная команда");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static String getPrompt() {
        if (currentUser == null) {
            return "(не авторизован) > ";
        }
        return currentUser.getLogin() + " > ";
    }

    private static boolean isLoggedIn() {
        if (currentUser == null) {
            System.out.println("Сначала войдите в систему (login <логин> <пароль>)");
            return false;
        }
        return true;
    }

    private static void handleRegister(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Формат: register <логин> <пароль>");
            return;
        }

        String login = parts[1].trim();
        String password = parts[2].trim();

        if (login.isEmpty() || password.isEmpty()) {
            System.out.println("Логин и пароль не могут быть пустыми");
            return;
        }

        String key = login.toLowerCase();
        if (users.containsKey(key)) {
            System.out.println("Пользователь с таким логином уже существует");
            return;
        }

        User newUser = new User(login, password);
        users.put(key, newUser);
        System.out.println("Пользователь " + login + " успешно зарегистрирован!");
    }

    private static void handleLogin(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Формат: login <логин> <пароль>");
            return;
        }

        String login = parts[1];
        String password = parts[2];

        User user = users.get(login.toLowerCase());
        if (user != null && user.checkPassword(password)) {
            currentUser = user;
            System.out.println("Добро пожаловать, " + user.getLogin() + "!");
        } else {
            System.out.println("Неверный логин или пароль");
        }
    }

    private static void handleLogout() {
        if (currentUser == null) {
            System.out.println("Вы и так не авторизованы");
        } else {
            System.out.println("До свидания, " + currentUser.getLogin() + "!");
            currentUser = null;
        }
    }

    private static void handleFinanceCommand(String command, String[] parts) {
        Wallet wallet = currentUser.getWallet();

        if (command.equals("status")) {
            System.out.println("\n=== Состояние кошелька (" + currentUser.getLogin() + ") ===");
            System.out.printf("Текущий баланс: %.2f\n\n", wallet.getBalance());

            System.out.println("Доходы по категориям:");
            printCategoryMap(wallet.getIncomeByCategory());

            System.out.println("Расходы по категориям:");
            printCategoryMap(wallet.getExpenseByCategory());

            System.out.println();
            return;
        }

        if (parts.length < 4) {
            System.out.println("Формат: " + command + " <сумма> <категория> <описание>");
            return;
        }

        try {
            double amount = Double.parseDouble(parts[1]);
            String category = parts[2];
            String description = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length));

            if (command.equals("income")) {
                wallet.addIncome(amount, category, description);
                System.out.printf("Добавлен доход: +%.2f (%s) — %s\n", amount, category, description);
            } else {
                wallet.addExpense(amount, category, description);
                System.out.printf("Добавлен расход: -%.2f (%s) — %s\n", amount, category, description);
            }
        } catch (NumberFormatException e) {
            System.out.println("Сумма должна быть числом");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void printCategoryMap(Map<String, Double> map) {
        if (map.isEmpty()) {
            System.out.println("  Нет операций");
            return;
        }
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            System.out.printf("  %-15s : %10.2f\n", entry.getKey(), entry.getValue());
        }
        System.out.println();
    }

    private static void loadAllUsers() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("USER ")) {
                    String[] userParts = line.substring(5).trim().split("\\s+", 2);
                    if (userParts.length != 2) continue;

                    String login = userParts[0];
                    String password = userParts[1];

                    User user = new User(login, password);
                    Wallet wallet = Wallet.loadFrom(scanner);
                    user.setWallet(wallet);

                    users.put(login.toLowerCase(), user);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки данных: " + e.getMessage());
        }
    }

    private static void saveAllUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (User user : users.values()) {
                writer.println("USER " + user.getLogin() + " " + user.getPassword());
                user.getWallet().saveTo(writer);
                writer.println("---");
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }
}