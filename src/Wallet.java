
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Wallet {

    private double balance = 0.0;
    private final List<String> operations = new ArrayList<>();

    public void addIncome(double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Доход должен быть положительным");
        }
        balance += amount;
        operations.add("Доход: +" + amount + " (" + description + ")");
    }

    public void addExpense(double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Расход должен быть положительным");
        }
        balance -= amount;
        operations.add("Расход: -" + amount + " (" + description + ")");
    }

    public double getBalance() {
        return balance;
    }

    public List<String> getOperations() {
        return new ArrayList<>(operations); // возвращаем копию
    }

    public void saveTo(PrintWriter writer) {
        writer.println("BALANCE " + balance);
        for (String op : operations) {
            writer.println("OPERATION " + op);
        }
    }

    public static Wallet loadFrom(Scanner scanner) {
        Wallet wallet = new Wallet();
        double loadedBalance = 0.0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.equals("---")) {
                break;
            }

            if (line.startsWith("BALANCE ")) {
                try {
                    loadedBalance = Double.parseDouble(line.substring(8).trim());
                } catch (Exception e) {
                    System.err.println("Ошибка чтения баланса, используется 0");
                }
            } else if (line.startsWith("OPERATION ")) {
                String operation = line.substring(10);
                wallet.operations.add(operation);
            }
        }

        wallet.balance = loadedBalance;
        return wallet;
    }
}