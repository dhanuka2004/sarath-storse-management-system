import java.sql.*;
import java.util.Scanner;

public class GroceryInventorySystem {
    private static Scanner scanner = new Scanner(System.in);
    
    // Database credentials matching our Docker command
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/grocery";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2004";

    public static void main(String[] args) {
        setupDatabase(); // Creates the table automatically if it doesn't exist
        boolean running = true;
        System.out.println("Welcome to the Grocery Database Manager!");

        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Add New Item");
            System.out.println("2. View All Items");
            System.out.println("3. Update Item Quantity");
            System.out.println("4. Remove Item");
            System.out.println("5. Exit");
            System.out.print("Choose an option (1-5): ");

            int choice = getUserInt();

            switch (choice) {
                case 1: addItem(); break;
                case 2: viewItems(); break;
                case 3: updateQuantity(); break;
                case 4: removeItem(); break;
                case 5:
                    running = false;
                    System.out.println("Exiting system. Have a great day!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    // Creates the 'items' table in the database when the program starts
    private static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE TABLE IF NOT EXISTS items (" +
                         "id SERIAL PRIMARY KEY, " + 
                         "name VARCHAR(100) NOT NULL, " +
                         "quantity INT NOT NULL, " +
                         "price DOUBLE PRECISION NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Database connection failed. Is Docker running?");
        }
    }

    private static void addItem() {
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter initial quantity: ");
        int quantity = getUserInt();
        System.out.print("Enter item price: ");
        double price = getUserDouble();

        String sql = "INSERT INTO items (name, quantity, price) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
            System.out.println("Item added to database!");
            
        } catch (SQLException e) {
            System.out.println("Error adding item: " + e.getMessage());
        }
    }

    private static void viewItems() {
        String sql = "SELECT * FROM items";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n--- Database Inventory ---");
            boolean hasItems = false;
            
            while (rs.next()) {
                hasItems = true;
                // We reuse your Item class to print the data nicely
                Item item = new Item(
                    rs.getInt("id"), 
                    rs.getString("name"), 
                    rs.getInt("quantity"), 
                    rs.getDouble("price")
                );
                System.out.println(item);
            }
            
            if (!hasItems) System.out.println("The inventory is currently empty.");
            
        } catch (SQLException e) {
            System.out.println("Error reading items: " + e.getMessage());
        }
    }

    private static void updateQuantity() {
        System.out.print("Enter the ID of the item to update: ");
        int id = getUserInt();
        System.out.print("Enter new quantity: ");
        int newQuantity = getUserInt();

        String sql = "UPDATE items SET quantity = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Quantity updated in database!");
            } else {
                System.out.println("Item ID not found!");
            }
            
        } catch (SQLException e) {
            System.out.println("Error updating item: " + e.getMessage());
        }
    }

    private static void removeItem() {
        System.out.print("Enter the ID of the item to remove: ");
        int id = getUserInt();

        String sql = "DELETE FROM items WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Item removed from database!");
            } else {
                System.out.println("Item ID not found!");
            }
            
        } catch (SQLException e) {
            System.out.println("Error removing item: " + e.getMessage());
        }
    }

    // Input helpers remain exactly the same
    private static int getUserInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("That's not a valid number. Try again: ");
            scanner.next(); 
        }
        int value = scanner.nextInt();
        scanner.nextLine(); 
        return value;
    }

    private static double getUserDouble() {
        while (!scanner.hasNextDouble()) {
            System.out.print("That's not a valid price. Try again: ");
            scanner.next();
        }
        double value = scanner.nextDouble();
        scanner.nextLine(); 
        return value;
    }
}