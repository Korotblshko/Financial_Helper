public class Income extends Transaction {

    public Income(double amount, String category, String description) {
        super(amount, category, description);
    }

    @Override
    public String getType() {
        return "income";
    }

    @Override
    public String toString() {
        return String.format("Доход: +%.2f (%s) - %s [%s]",
                amount, category, description, date.toLocalDate());
    }
}