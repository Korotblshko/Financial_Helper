
import java.io.*;
import java.util.*;

public class Main {

    private static final String DATA_FILE = "users_data.txt";
    private static final Map<String, User> users = new HashMap<>();
    private static User currentUser = null;

    public static void main(String[] args) {
        loadAllUsers();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Личный финансовый помощник");
        System.out.println("Данные загружены из " + DATA_FILE + " (" + users.size() + " пользователей)");
        System.out.println("Команды:");
        System.out.println("  login <логин> <пароль>");
        System.out.println("  logout");
        System.out.println("  income <сумма> <описание>");
        System.out.println("  expense <сумма> <описание>");
        System.out.println("  status");
        System.out.println("  exit    (сохраняет данные)");
        System.out.println();

        while (true) {
            System.out.print(getPrompt());
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
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
                        System.out.println("Неизвестная команда. Доступны: login, logout, income, expense, status, exit");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static String getPrompt() {
        if (currentUser == null) {
            return "(не авторизован) > ";
        } else {
            return currentUser.getLogin() + " > ";
        }
    }

    private static boolean isLoggedIn() {
        if (currentUser == null) {
            System.out.println("Сначала войдите в систему (команда: login <логин> <пароль>)");
            return false;
        }
        return true;
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
            System.out.println("\nСостояние кошелька (" + currentUser.getLogin() + "):");
            System.out.printf("Баланс: %.2f\n", wallet.getBalance());
            System.out.println("Операции:");
            List<String> ops = wallet.getOperations();
            if (ops.isEmpty()) {
                System.out.println("  Пока нет операций");
            } else {
                for (String op : ops) {
                    System.out.println("  " + op);
                }
            }
            System.out.println();
            return;
        }

        if (parts.length < 3) {
            System.out.println("Формат: " + command + " <сумма> <описание> (описание может содержать пробелы)");
            return;
        }

        try {
            double amount = Double.parseDouble(parts[1]);
            String description = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));

            if (command.equals("income")) {
                wallet.addIncome(amount, description);
                System.out.println("Добавлен доход: +" + amount + " (" + description + ")");
            } else {
                wallet.addExpense(amount, description);
                System.out.println("Добавлен расход: -" + amount + " (" + description + ")");
            }
        } catch (NumberFormatException e) {
            System.out.println("Сумма должна быть числом");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void loadAllUsers() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("Файл данных не найден → начинаем с пустой базы");
            // Добавляем тестовых пользователей при первом запуске
            users.put("alice", new User("alice", "12345"));
            users.put("bob", new User("bob", "qwerty"));
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

                    users.put(login.toLowerCase(), user);  // Храним в lowerCase для поиска
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Не удалось открыть файл для чтения");
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
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
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }
}