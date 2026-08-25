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
import javafx.scene.chart.*;

import java.sql.*;

public class GroceryFXApp extends Application {
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/grocery";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "2004";

    private Stage primaryStage;

    // Table views and data lists
    private TableView<GroceryItem> inventoryTable;
    private ObservableList<GroceryItem> inventoryDataList = FXCollections.observableArrayList();

    private TableView<StaffMember> staffTable;
    private ObservableList<StaffMember> staffDataList = FXCollections.observableArrayList();

    private TableView<Customer> customerTable;
    private ObservableList<Customer> customerDataList = FXCollections.observableArrayList();

    private TableView<Supplier> supplierTable;
    private ObservableList<Supplier> supplierDataList = FXCollections.observableArrayList();

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

    // --- 2. Admin Home Dashboard (5 Options Hub) ---
    private void showAdminHomeDashboard() {
        primaryStage.setTitle("Sarath Stores - Admin Dashboard");



        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #f4f6f9;");

        Label titleLabel = new Label("Admin Control Panel");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button btnStaff = createMenuButton("Staff Details");
        Button btnInventory = createMenuButton("Inventory Details");
        Button btnCustomer = createMenuButton("Customer Details");
        Button btnSupplier = createMenuButton("Supplier Details");
        Button btnAnalytics = createMenuButton("Analytics Details");
        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        // Connected admin buttons
        btnStaff.setOnAction(e -> showAdminStaffManagement());
        btnInventory.setOnAction(e -> showAdminInventoryManagement());
        btnCustomer.setOnAction(e -> showCustomerManagement(true));
        btnSupplier.setOnAction(e -> showSupplierManagement(true)); // Connected to Supplier module!

        btnAnalytics.setOnAction(e -> showAdminAnalyticsManagement());
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

        btnCustomer.setOnAction(e -> showCustomerManagement(false));
        btnSupplier.setOnAction(e -> showSupplierManagement(false));
        btnLogout.setOnAction(e -> showLoginScreen());

        VBox menuBox = new VBox(15, btnInventory, btnPos, btnCustomer, btnSupplier);
        menuBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(titleLabel, menuBox, btnLogout);

        Scene scene = new Scene(layout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(280);
        btn.setPrefHeight(45);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;");
        return btn;
    }

    // --- 4. Admin Supplier Management System ---
    private void showSupplierManagement(boolean isAdmin) {
        String roleTitle = isAdmin ? "ADMIN" : "EMPLOYEE";
        primaryStage.setTitle("Sarath Stores - Supplier Details Management");

        GridPane formGrid = new GridPane();
        formGrid.setPadding(new Insets(15));
        formGrid.setHgap(15);
        formGrid.setVgap(15);

        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField addressField = new TextField();
        TextField idField = new TextField();

        formGrid.addRow(0, new Label("Supplier Name:"), nameField);
        formGrid.addRow(1, new Label("Phone Number:"), phoneField);
        formGrid.addRow(2, new Label("Email Address:"), emailField);
        formGrid.addRow(3, new Label("Company Address:"), addressField);
        formGrid.addRow(4, new Label("ID (for Update/Delete):"), idField);

        setupSupplierTable();

        Button btnAdd = new Button("Add Supplier");
        Button btnUpdate = new Button("Update Phone");
        Button btnDelete = new Button("Delete Supplier");
        Button btnRefresh = new Button("Refresh");
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        HBox buttonBox = new HBox(12, btnAdd, btnUpdate, btnDelete, btnRefresh, btnBack);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        btnAdd.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String address = addressField.getText().trim();
                executeUpdate("INSERT INTO suppliers (name, phone, email, address) VALUES (?, ?, ?, ?)", name, phone, email, address);
                loadSupplierData();
                clearFields(nameField, phoneField, emailField, addressField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Verify supplier input values."); }
        });

        btnUpdate.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String newPhone = phoneField.getText().trim();
                executeUpdate("UPDATE suppliers SET phone = ? WHERE id = ?", newPhone, id);
                loadSupplierData();
                clearFields(nameField, phoneField, emailField, addressField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid ID and new phone number."); }
        });

        btnDelete.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                executeUpdate("DELETE FROM suppliers WHERE id = ?", id);
                loadSupplierData();
                clearFields(nameField, phoneField, emailField, addressField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid supplier ID to delete."); }
        });

        btnRefresh.setOnAction(e -> loadSupplierData());

        // Dynamic back button routing based on role
        btnBack.setOnAction(e -> {
            if (isAdmin) {
                showAdminHomeDashboard();
            } else {
                showEmployeeHomeDashboard();
            }
        });

        VBox mainLayout = new VBox(15, new Label(" SUPPLIER MANAGEMENT"), formGrid, buttonBox, supplierTable);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        loadSupplierData();
    }

    // --- 5. Admin Customer Management System ---
    private void showCustomerManagement(boolean isAdmin) {
        String roleTitle = isAdmin ? "ADMIN" : "EMPLOYEE";
        primaryStage.setTitle("Sarath Stores - Customer Details Management");

        GridPane formGrid = new GridPane();
        formGrid.setPadding(new Insets(15));
        formGrid.setHgap(15);
        formGrid.setVgap(15);

        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField addressField = new TextField();
        TextField idField = new TextField();

        formGrid.addRow(0, new Label("Customer Name:"), nameField);
        formGrid.addRow(1, new Label("Phone Number:"), phoneField);
        formGrid.addRow(2, new Label("Email Address:"), emailField);
        formGrid.addRow(3, new Label("Home Address:"), addressField);
        formGrid.addRow(4, new Label("ID (for Update/Delete):"), idField);

        setupCustomerTable();

        Button btnAdd = new Button("Add Customer");
        Button btnUpdate = new Button("Update Phone");
        Button btnDelete = new Button("Delete Customer");
        Button btnRefresh = new Button("Refresh");
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        HBox buttonBox = new HBox(12, btnAdd, btnUpdate, btnDelete, btnRefresh, btnBack);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        btnAdd.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String address = addressField.getText().trim();
                executeUpdate("INSERT INTO customers (name, phone, email, address) VALUES (?, ?, ?, ?)", name, phone, email, address);
                loadCustomerData();
                clearFields(nameField, phoneField, emailField, addressField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Verify customer input values."); }
        });

        btnUpdate.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String newPhone = phoneField.getText().trim();
                executeUpdate("UPDATE customers SET phone = ? WHERE id = ?", newPhone, id);
                loadCustomerData();
                clearFields(nameField, phoneField, emailField, addressField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid ID and new phone number."); }
        });

        btnDelete.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                executeUpdate("DELETE FROM customers WHERE id = ?", id);
                loadCustomerData();
                clearFields(nameField, phoneField, emailField, addressField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid customer ID to delete."); }
        });

        btnRefresh.setOnAction(e -> loadCustomerData());

        // Dynamic back button routing based on role
        btnBack.setOnAction(e -> {
            if (isAdmin) {
                showAdminHomeDashboard();
            } else {
                showEmployeeHomeDashboard();
            }
        });

        VBox mainLayout = new VBox(15, new Label("ADMIN CUSTOMER MANAGEMENT"), formGrid, buttonBox, customerTable);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        loadCustomerData();
    }

    // --- 6. Admin Staff Management System ---
    private void showAdminStaffManagement() {
        primaryStage.setTitle("Sarath Stores - Staff Details Management");

        GridPane formGrid = new GridPane();
        formGrid.setPadding(new Insets(15));
        formGrid.setHgap(15);
        formGrid.setVgap(15);

        TextField nameField = new TextField();
        TextField roleField = new TextField();
        TextField phoneField = new TextField();
        TextField salaryField = new TextField();
        TextField idField = new TextField();

        formGrid.addRow(0, new Label("Staff Name:"), nameField);
        formGrid.addRow(1, new Label("Job Role:"), roleField);
        formGrid.addRow(2, new Label("Phone Number:"), phoneField);
        formGrid.addRow(3, new Label("Salary ($):"), salaryField);
        formGrid.addRow(4, new Label("ID (for Update/Delete):"), idField);

        setupStaffTable();

        Button btnAdd = new Button("Add Staff");
        Button btnUpdate = new Button("Update Salary");
        Button btnDelete = new Button("Delete Staff");
        Button btnRefresh = new Button("Refresh");
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        HBox buttonBox = new HBox(12, btnAdd, btnUpdate, btnDelete, btnRefresh, btnBack);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        btnAdd.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                String role = roleField.getText().trim();
                String phone = phoneField.getText().trim();
                double salary = Double.parseDouble(salaryField.getText().trim());
                executeUpdate("INSERT INTO staff (name, role, phone, salary) VALUES (?, ?, ?, ?)", name, role, phone, salary);
                loadStaffData();
                clearFields(nameField, roleField, phoneField, salaryField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Verify staff input values."); }
        });

        btnUpdate.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                double newSalary = Double.parseDouble(salaryField.getText().trim());
                executeUpdate("UPDATE staff SET salary = ? WHERE id = ?", newSalary, id);
                loadStaffData();
                clearFields(nameField, roleField, phoneField, salaryField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid ID and new salary."); }
        });

        btnDelete.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                executeUpdate("DELETE FROM staff WHERE id = ?", id);
                loadStaffData();
                clearFields(nameField, roleField, phoneField, salaryField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid staff ID to delete."); }
        });

        btnRefresh.setOnAction(e -> loadStaffData());
        btnBack.setOnAction(e -> showAdminHomeDashboard());

        VBox mainLayout = new VBox(15, new Label("ADMIN STAFF MANAGEMENT"), formGrid, buttonBox, staffTable);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        loadStaffData();
    }

    // --- 7. Admin Inventory Management System ---
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

        setupInventoryTable();

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
                loadInventoryData();
                clearFields(nameField, qtyField, priceField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Verify input values."); }
        });

        btnUpdate.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                int newQty = Integer.parseInt(qtyField.getText().trim());
                executeUpdate("UPDATE items SET quantity = ? WHERE id = ?", newQty, id);
                loadInventoryData();
                clearFields(nameField, qtyField, priceField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid ID and quantity."); }
        });

        btnDelete.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                executeUpdate("DELETE FROM items WHERE id = ?", id);
                loadInventoryData();
                clearFields(nameField, qtyField, priceField, idField);
            } catch (Exception ex) { showAlert("Input Error", "Enter valid ID to delete."); }
        });

        btnRefresh.setOnAction(e -> loadInventoryData());
        btnBack.setOnAction(e -> showAdminHomeDashboard());

        VBox mainLayout = new VBox(15, new Label("ADMIN INVENTORY MANAGEMENT"), formGrid, buttonBox, inventoryTable);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        loadInventoryData();
    }

    // --- 8. Employee Inventory Management System ---
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

        setupInventoryTable();

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
                loadInventoryData();
                idField.clear();
                qtyField.clear();
            } catch (Exception ex) { showAlert("Input Error", "Enter valid ID and quantity."); }
        });

        btnRefresh.setOnAction(e -> loadInventoryData());
        btnBack.setOnAction(e -> showEmployeeHomeDashboard());

        VBox mainLayout = new VBox(15, new Label("EMPLOYEE INVENTORY SYSTEM (Stock Update)"), formGrid, buttonBox, inventoryTable);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        loadInventoryData();
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

    // --- Table Setups & Database Queries ---
    private void setupInventoryTable() {
        inventoryTable = new TableView<>();
        inventoryTable.setPrefHeight(300);

        TableColumn<GroceryItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idCol.setPrefWidth(80);

        TableColumn<GroceryItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(220);

        TableColumn<GroceryItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        qtyCol.setPrefWidth(120);

        TableColumn<GroceryItem, Double> priceCol = new TableColumn<>("Price ($)");
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        priceCol.setPrefWidth(150);

        inventoryTable.getColumns().clear();
        inventoryTable.getColumns().addAll(idCol, nameCol, qtyCol, priceCol);
        inventoryTable.setItems(inventoryDataList);
    }

    private void setupStaffTable() {
        staffTable = new TableView<>();
        staffTable.setPrefHeight(280);

        TableColumn<StaffMember, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idCol.setPrefWidth(60);

        TableColumn<StaffMember, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(180);

        TableColumn<StaffMember, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        roleCol.setPrefWidth(130);

        TableColumn<StaffMember, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        phoneCol.setPrefWidth(130);

        TableColumn<StaffMember, Double> salaryCol = new TableColumn<>("Salary ($)");
        salaryCol.setCellValueFactory(cellData -> cellData.getValue().salaryProperty().asObject());
        salaryCol.setPrefWidth(100);

        staffTable.getColumns().clear();
        staffTable.getColumns().addAll(idCol, nameCol, roleCol, phoneCol, salaryCol);
        staffTable.setItems(staffDataList);
    }

    private void setupCustomerTable() {
        customerTable = new TableView<>();
        customerTable.setPrefHeight(280);

        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idCol.setPrefWidth(60);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(160);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        phoneCol.setPrefWidth(130);

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        emailCol.setPrefWidth(180);

        TableColumn<Customer, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        addressCol.setPrefWidth(180);

        customerTable.getColumns().clear();
        customerTable.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, addressCol);
        customerTable.setItems(customerDataList);
    }

    private void setupSupplierTable() {
        supplierTable = new TableView<>();
        supplierTable.setPrefHeight(280);

        TableColumn<Supplier, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idCol.setPrefWidth(60);

        TableColumn<Supplier, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(160);

        TableColumn<Supplier, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        phoneCol.setPrefWidth(130);

        TableColumn<Supplier, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        emailCol.setPrefWidth(180);

        TableColumn<Supplier, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        addressCol.setPrefWidth(180);

        supplierTable.getColumns().clear();
        supplierTable.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, addressCol);
        supplierTable.setItems(supplierDataList);
    }

    private void showAdminAnalyticsManagement() {
        primaryStage.setTitle("Sarath Stores - Business Analytics");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Item Name");
        yAxis.setLabel("Quantity in Stock");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Top Inventory Stock Levels");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Stock Quantity");

        // Fetch live data from PostgreSQL
        String sql = "SELECT name, quantity FROM items ORDER BY quantity DESC LIMIT 5";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("name"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load analytics data.");
        }
        barChart.getData().add(series);

        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        btnBack.setOnAction(e -> showAdminHomeDashboard());

        VBox mainLayout = new VBox(15, new Label("BUSINESS PERFORMANCE ANALYTICS"), barChart, btnBack);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    private void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Create items table
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "price DOUBLE PRECISION NOT NULL)");

            // Create staff table
            stmt.execute("CREATE TABLE IF NOT EXISTS staff (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "salary DOUBLE PRECISION NOT NULL)");

            // Create customers table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "address VARCHAR(255))");

            // Create suppliers table
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "address VARCHAR(255))");

        } catch (SQLException e) {
            showAlert("Database Error", "Could not connect to Docker PostgreSQL.");
        }
    }

    private void loadInventoryData() {
        inventoryDataList.clear();
        String sql = "SELECT * FROM items ORDER BY id ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                inventoryDataList.add(new GroceryItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load inventory data: " + e.getMessage());
        }
    }

    private void loadStaffData() {
        staffDataList.clear();
        String sql = "SELECT * FROM staff ORDER BY id ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                staffDataList.add(new StaffMember(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("phone"),
                        rs.getDouble("salary")
                ));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load staff data: " + e.getMessage());
        }
    }

    private void loadCustomerData() {
        customerDataList.clear();
        String sql = "SELECT * FROM customers ORDER BY id ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customerDataList.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load customer data: " + e.getMessage());
        }
    }

    private void loadSupplierData() {
        supplierDataList.clear();
        String sql = "SELECT * FROM suppliers ORDER BY id ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                supplierDataList.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load supplier data: " + e.getMessage());
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

    // --- Model Classes ---
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

    public static class StaffMember {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty role;
        private final SimpleStringProperty phone;
        private final SimpleDoubleProperty salary;

        public StaffMember(int id, String name, String role, String phone, double salary) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.role = new SimpleStringProperty(role);
            this.phone = new SimpleStringProperty(phone);
            this.salary = new SimpleDoubleProperty(salary);
        }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty roleProperty() { return role; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleDoubleProperty salaryProperty() { return salary; }
    }

    public static class Customer {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty email;
        private final SimpleStringProperty address;

        public Customer(int id, String name, String phone, String email, String address) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.phone = new SimpleStringProperty(phone);
            this.email = new SimpleStringProperty(email);
            this.address = new SimpleStringProperty(address);
        }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty addressProperty() { return address; }
    }

    public static class Supplier {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty email;
        private final SimpleStringProperty address;

        public Supplier(int id, String name, String phone, String email, String address) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.phone = new SimpleStringProperty(phone);
            this.email = new SimpleStringProperty(email);
            this.address = new SimpleStringProperty(address);
        }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty addressProperty() { return address; }
    }
}