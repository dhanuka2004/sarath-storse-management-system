import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class GroceryFXApp extends Application {
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/grocery";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2004";

    private Stage primaryStage;
    private TableView<GroceryItem> table;
    private ObservableList<GroceryItem> dataList = FXCollections.observableArrayList();

    //private TextField nameField, qtyField, priceField, idField;
    //private Button btnAdd, btnUpdate, btnDelete, btnRefresh;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setupDatabase();
        showLoginScreen();
    }

    // --- 1. Login Interface with Business Logo & 800x700 Dimension ---
    private void showLoginScreen() {
        primaryStage.setTitle("Sarath Stores - Login");

        VBox loginLayout = new VBox(20);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(60));
        loginLayout.setStyle("-fx-background-color: #F5F6F2;");

        // Business Logo Setup
        ImageView logoView = new ImageView();
        try {
            // Place your logo file named 'logo.png' in your project root folder
            Image logoImage = new Image("file:logo.jpeg");
            logoView.setImage(logoImage);
            logoView.setFitWidth(260);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            // Fallback if logo.png is not found yet
            System.out.println("Logo image not found. Place 'logo.png' in project root.");
        }

        Label brandLabel = new Label("Sarath Stores Managment System ");
        brandLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Form Grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);

        TextField userField = new TextField();
        userField.setPromptText("Enter username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        Button btnLogin = new Button("Login");
        btnLogin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        grid.addRow(0, new Label("Username:"), userField);
        grid.addRow(1, new Label("Password:"), passField);

        HBox btnBox = new HBox(btnLogin);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(btnBox, 1, 2);

        grid.add(errorLabel, 1, 3);

        btnLogin.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();
            String role = authenticateUser(username, password);

            if (role != null) {
                if (role.equals("admin")) {
                    showAdminHomeDashboard();
                } else {
                    showEmployeeHomeDashboard();
                }
            } else {
                errorLabel.setText("Invalid username or password!");
            }
        });

        loginLayout.getChildren().addAll(logoView, brandLabel, grid);

        Scene loginScene = new Scene(loginLayout, 800, 700);
        primaryStage.setScene(loginScene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private String authenticateUser(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role"); // Returns 'admin' or 'employee'
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showAdminHomeDashboard() {
        primaryStage.setTitle("Sarath Stores - Admin Dashboard");

        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #f4f6f9;");

        Label titleLabel = new Label("Admin Control Panel");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button btnStaff = createMenuButton("Staff Details");
        Button btnInventory = createMenuButton("Inventory Details");
        Button btnCustomer = createMenuButton("Customer Details");
        Button btnSupplier = createMenuButton("Supplier Details");
        Button btnAnalytics = createMenuButton("Analytics Details");
        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        btnInventory.setOnAction(e -> showAdminInventoryManagement());
        btnStaff.setOnAction(e -> showAdminPlaceholderScreen("Staff Details"));
        btnCustomer.setOnAction(e -> showAdminPlaceholderScreen("Customer Details"));
        btnSupplier.setOnAction(e -> showAdminPlaceholderScreen("Supplier Details"));
        btnAnalytics.setOnAction(e -> showAdminPlaceholderScreen("Analytics Details"));
        btnLogout.setOnAction(e -> showLoginScreen());

        VBox menuBox = new VBox(15, btnStaff, btnInventory, btnCustomer, btnSupplier, btnAnalytics);
        menuBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(titleLabel, menuBox, btnLogout);

        Scene scene = new Scene(layout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    // --- 3. Employee Home Dashboard (4 Options Hub) ---
    private void showEmployeeHomeDashboard() {
        primaryStage.setTitle("Sarath Stores - Employee Portal");

        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #f4f6f9;");

        Label titleLabel = new Label("Employee Portal");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button btnInventory = createMenuButton("Inventory System");
        Button btnPos = createMenuButton("POS System");
        Button btnCustomer = createMenuButton("Customer Details");
        Button btnSupplier = createMenuButton("Supplier Details");
        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        btnInventory.setOnAction(e -> showEmployeeInventoryManagement());
        btnPos.setOnAction(e -> showEmployeePlaceholderScreen("POS System"));
        btnCustomer.setOnAction(e -> showEmployeePlaceholderScreen("Customer Details"));
        btnSupplier.setOnAction(e -> showEmployeePlaceholderScreen("Supplier Details"));
        btnLogout.setOnAction(e -> showLoginScreen());

        VBox menuBox = new VBox(15, btnInventory, btnPos, btnCustomer, btnSupplier);
        menuBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(titleLabel, menuBox, btnLogout);

        Scene scene = new Scene(layout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    // --- Helper for Uniform Menu Buttons ---
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(300);
        btn.setPrefHeight(45);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;");
        return btn;
    }

    // --- 4. Admin Inventory Management System ---
    private void showAdminInventoryManagement() {
        primaryStage.setTitle("Sarath Stores - Inventory Management System (Admin)");

        GridPane formGrid = new GridPane();
        formGrid.setPadding(new Insets(15));
        formGrid.setHgap(15);
        formGrid.setVgap(15);

        TextField nameField = new TextField();
        TextField qtyField = new TextField();
        TextField priceField = new TextField();
        TextField idField = new TextField();

        formGrid.addRow(0, new Label("Item Name:"), nameField);
        formGrid.addRow(1, new Label("Quantity:"), qtyField);
        formGrid.addRow(2, new Label("Price ($):"), priceField);
        formGrid.addRow(3, new Label("ID (for Update/Delete):"), idField);

        setupTable();

        Button btnAdd = new Button("Add Item");
        Button btnUpdate = new Button("Update Quantity");
        Button btnDelete = new Button("Delete Item");
        Button btnRefresh = new Button("Refresh");
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        HBox buttonBox = new HBox(12, btnAdd, btnUpdate, btnDelete, btnRefresh, btnBack);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        btnAdd.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                int qty = Integer.parseInt(qtyField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                executeUpdate("INSERT INTO items (name, quantity, price) VALUES (?, ?, ?)", name, qty, price);
                loadData();
                clearFields(nameField, qtyField, priceField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Verify input values."); }
        });

        btnUpdate.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                int newQty = Integer.parseInt(qtyField.getText().trim());
                executeUpdate("UPDATE items SET quantity = ? WHERE id = ?", newQty, id);
                loadData();
                clearFields(nameField, qtyField, priceField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid ID and quantity."); }
        });

        btnDelete.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                executeUpdate("DELETE FROM items WHERE id = ?", id);
                loadData();
                clearFields(nameField, qtyField, priceField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid ID to delete."); }
        });

        btnRefresh.setOnAction(e -> loadData());
        btnBack.setOnAction(e -> showAdminHomeDashboard());

        VBox mainLayout = new VBox(15, new Label("ADMIN INVENTORY MANAGEMENT"), formGrid, buttonBox, table);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        loadData();
    }

    // --- 5. Employee Inventory Management System ---
    private void showEmployeeInventoryManagement() {
        primaryStage.setTitle("Sarath Stores - Inventory System (Employee)");

        GridPane formGrid = new GridPane();
        formGrid.setPadding(new Insets(15));
        formGrid.setHgap(15);
        formGrid.setVgap(15);

        TextField idField = new TextField();
        TextField qtyField = new TextField();

        formGrid.addRow(0, new Label("Item ID (to update stock):"), idField);
        formGrid.addRow(1, new Label("New Quantity:"), qtyField);

        setupTable();

        Button btnUpdate = new Button("Update Quantity");
        Button btnRefresh = new Button("Refresh");
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        HBox buttonBox = new HBox(15, btnUpdate, btnRefresh, btnBack);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        btnUpdate.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                int newQty = Integer.parseInt(qtyField.getText().trim());
                executeUpdate("UPDATE items SET quantity = ? WHERE id = ?", newQty, id);
                loadData();
                idField.clear();
                qtyField.clear();
            } catch (Exception ex) { showAlert("Input Error", "Enter valid ID and quantity."); }
        });

        btnRefresh.setOnAction(e -> loadData());
        btnBack.setOnAction(e -> showEmployeeHomeDashboard());

        VBox mainLayout = new VBox(15, new Label("EMPLOYEE INVENTORY SYSTEM (Stock Update)"), formGrid, buttonBox, table);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        loadData();
    }

    // --- Placeholder Screens ---
    private void showAdminPlaceholderScreen(String moduleName) {
        showPlaceholderScreen(moduleName, true);
    }

    private void showEmployeePlaceholderScreen(String moduleName) {
        showPlaceholderScreen(moduleName, false);
    }

    private void showPlaceholderScreen(String moduleName, boolean isAdmin) {
        primaryStage.setTitle("Sarath Stores - " + moduleName);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        Label title = new Label(moduleName + " Module");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        Label info = new Label("This section is under development.");
        info.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 20;");

        if (isAdmin) {
            btnBack.setOnAction(e -> showAdminHomeDashboard());
        } else {
            btnBack.setOnAction(e -> showEmployeeHomeDashboard());
        }

        layout.getChildren().addAll(title, info, btnBack);

        Scene scene = new Scene(layout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    // --- Shared Helper Methods ---
    private void setupTable() {
        table = new TableView<>();
        table.setPrefHeight(350);

        TableColumn<GroceryItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idCol.setPrefWidth(100);

        TableColumn<GroceryItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(250);

        TableColumn<GroceryItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        qtyCol.setPrefWidth(150);

        TableColumn<GroceryItem, Double> priceCol = new TableColumn<>("Price ($)");
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        priceCol.setPrefWidth(200);

        table.getColumns().clear();
        table.getColumns().addAll(idCol, nameCol, qtyCol, priceCol);
        table.setItems(dataList);
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
            showAlert("Database Error", "Could not connect to Docker PostgreSQL.");
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

    private void executeUpdate(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }

    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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

