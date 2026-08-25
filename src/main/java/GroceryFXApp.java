import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class GroceryFXApp extends Application {
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/grocery";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2004";

    private TableView<GroceryItem> table;
    private ObservableList<GroceryItem> dataList = FXCollections.observableArrayList();

    private TextField nameField, qtyField, priceField, idField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sarath Stores - JavaFX Inventory Management");

        setupDatabase();

        // --- Form Inputs Layout ---
        GridPane formGrid = new GridPane();
        formGrid.setPadding(new Insets(10));
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        nameField = new TextField();
        qtyField = new TextField();
        priceField = new TextField();
        idField = new TextField();


        formGrid.addRow(0, new Label("Item Name:"), nameField);
        formGrid.addRow(1, new Label("Quantity:"), qtyField);
        formGrid.addRow(2, new Label("Price ($):"), priceField);
        formGrid.addRow(3, new Label("ID (for Update/Delete):"), idField);

        // --- Table View Setup ---
        table = new TableView<>();
        
        TableColumn<GroceryItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

        TableColumn<GroceryItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<GroceryItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        TableColumn<GroceryItem, Double> priceCol = new TableColumn<>("Price ($)");
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        table.getColumns().addAll(idCol, nameCol, qtyCol, priceCol);
        table.setItems(dataList);

        // --- Buttons Setup ---
        Button btnAdd = new Button("Add Item");
        Button btnUpdate = new Button("Update Quantity");
        Button btnDelete = new Button("Delete Item");
        Button btnRefresh = new Button("Refresh");

        HBox buttonBox = new HBox(10, btnAdd, btnUpdate, btnDelete, btnRefresh);
        buttonBox.setPadding(new Insets(10));

        // Button Actions
        btnAdd.setOnAction(e -> addItem());
        btnUpdate.setOnAction(e -> updateItem());
        btnDelete.setOnAction(e -> deleteItem());
        btnRefresh.setOnAction(e -> loadData());

        // Main Layout Container
        VBox mainLayout = new VBox(10, formGrid, buttonBox, table);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        loadData();
    }

    private void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS items (" +
                         "id SERIAL PRIMARY KEY, " +
                         "name VARCHAR(100) NOT NULL, " +
                         "quantity INT NOT NULL, " +
                         "price DOUBLE PRECISION NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not connect to Docker PostgreSQL. Is it running?");
        }
    }

    private void loadData() {
        dataList.clear();
        String sql = "SELECT * FROM items ORDER BY id ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dataList.add(new GroceryItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void addItem() {
        try {
            String name = nameField.getText().trim();
            int qty = Integer.parseInt(qtyField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            String sql = "INSERT INTO items (name, quantity, price) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, qty);
                pstmt.setDouble(3, price);
                pstmt.executeUpdate();
                
                loadData();
                clearFields();
            }
        } catch (Exception e) {
            showAlert("Input Error", "Please verify your input values.");
        }
    }

    private void updateItem() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            int newQty = Integer.parseInt(qtyField.getText().trim());

            String sql = "UPDATE items SET quantity = ? WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newQty);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
                loadData();
                clearFields();
            }
        } catch (Exception e) {
            showAlert("Input Error", "Please enter valid ID and quantity.");
        }
    }

    private void deleteItem() {
        try {
            int id = Integer.parseInt(idField.getText().trim());

            String sql = "DELETE FROM items WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadData();
                clearFields();
            }
        } catch (Exception e) {
            showAlert("Input Error", "Please enter a valid ID to delete.");
        }
    }

    private void clearFields() {
        nameField.clear();
        qtyField.clear();
        priceField.clear();
        idField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Helper Model Class for JavaFX Table Binding ---
    public static class GroceryItem {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty price;

        public GroceryItem(int id, String name, int quantity, double price) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.price = new SimpleDoubleProperty(price);
        }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleIntegerProperty quantityProperty() { return quantity; }
        public SimpleDoubleProperty priceProperty() { return price; }
    }
}