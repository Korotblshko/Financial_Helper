import java.io.PrintWriter;
import java.util.*;

public class Wallet {

    private double balance = 0.0;
    private final List<Transaction> transactions = new ArrayList<>();

    public void addIncome(double amount, String category, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма дохода должна быть > 0");
        Income income = new Income(amount, category, description);
        transactions.add(income);
        balance += amount;
    }

    public void addExpense(double amount, String category, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма расхода должна быть > 0");
        Expense expense = new Expense(amount, category, description);
        transactions.add(expense);
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public Map<String, Double> getIncomeByCategory() {
        Map<String, Double> map = new HashMap<>();
        for (Transaction t : transactions) {
            if (t instanceof Income) {
                map.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return map;
    }

    public Map<String, Double> getExpenseByCategory() {
        Map<String, Double> map = new HashMap<>();
        for (Transaction t : transactions) {
            if (t instanceof Expense) {
                map.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return map;
    }

    public void saveTo(PrintWriter writer) {
        writer.println("BALANCE " + balance);
        for (Transaction t : transactions) {
            String type = t instanceof Income ? "INCOME" : "EXPENSE";
            writer.printf("%s %.2f %s %s%n",
                    type, t.getAmount(), t.getCategory(), t.getDescription());
        }
    }

    public static Wallet loadFrom(Scanner scanner) {
        Wallet wallet = new Wallet();
        double loadedBalance = 0.0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.equals("---")) break;

            if (line.startsWith("BALANCE ")) {
                try {
                    loadedBalance = Double.parseDouble(line.substring(8).trim());
                } catch (Exception ignored) {}
            } else {
                String[] parts = line.split("\\s+", 4);
                if (parts.length < 4) continue;

                String type = parts[0];
                double amount;
                try {
                    amount = Double.parseDouble(parts[1]);
                } catch (Exception e) { continue; }

                String category = parts[2];
                String description = parts[3];

                if ("INCOME".equals(type)) {
                    wallet.addIncome(amount, category, description);
                } else if ("EXPENSE".equals(type)) {
                    wallet.addExpense(amount, category, description);
                }
            }
        }

        wallet.balance = loadedBalance;
        return wallet;
    }
}