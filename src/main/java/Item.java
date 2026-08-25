public class Item {
    private int id;
    private String name;
    private int quantity;
    private double price;

    public Item(int id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
    }

    @Override
    public String toString() {
        return String.format("ID: %-4d | Name: %-15s | Qty: %-5d | Price: $%.2f", id, name, quantity, price);
    }
}