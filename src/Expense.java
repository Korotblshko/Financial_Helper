public class Expense extends Transaction {

    public Expense(double amount, String category, String description) {
        super(amount, category, description);
    }

    @Override
    public String getType() {
        return "expense";
    }

    @Override
    public String toString() {
        return String.format("Расход: -%.2f (%s) - %s [%s]",
                amount, category, description, date.toLocalDate());
    }
}