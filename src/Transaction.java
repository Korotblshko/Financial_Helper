import java.time.LocalDateTime;

public abstract class Transaction {
    protected final double amount;
    protected final String category;
    protected final String description;
    protected final LocalDateTime date;

    public Transaction(double amount, String category, String description) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = LocalDateTime.now();
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public abstract String getType();
}