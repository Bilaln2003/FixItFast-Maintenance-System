package com.maintenance;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.PriorityQueue;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

public class FixItFastApp extends Application {

    // --- DSA DATA STRUCTURE: Priority Queue ---
    // This automatically sorts jobs so Critical (10) is always above Minor (1)
    private PriorityQueue<Request> triageQueue = new PriorityQueue<>();

    // UI List helper
    private ObservableList<Request> tableData = FXCollections.observableArrayList();

    // UI Components
    private TableView<Request> table = new TableView<>();
    private Label statusLabel = new Label("System Ready");

    public static void main(String[] args) {
        DatabaseHandler.initDB();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Fix-It-Fast: Maintenance Triager");

        // --- DARK THEME STYLING STRINGS ---
        String darkBackground = "-fx-background-color: #1e1e1e;";
        String panelBackground = "-fx-background-color: #2b2b2b; -fx-border-color: #444; -fx-border-width: 0 1 0 0;";
        String labelStyle = "-fx-text-fill: #e0e0e0;";
        String fieldStyle = "-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-prompt-text-fill: #888;";
        String buttonStyle = "-fx-background-color: #0d47a1; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
        String dispatchBtnStyle = "-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";

        // --- 1. LEFT PANEL: INPUT FORM ---
        VBox inputPanel = new VBox(15);
        inputPanel.setPadding(new Insets(20));
        inputPanel.setStyle(panelBackground);
        inputPanel.setPrefWidth(320);

        Label header = new Label("New Maintenance Request");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.setStyle("-fx-text-fill: #64b5f6;");

        TextField txtName = new TextField();
        txtName.setPromptText("Tenant Name");
        txtName.setStyle(fieldStyle);

        // --- NEW ADDRESS FIELD ---
        TextField txtAddress = new TextField();
        txtAddress.setPromptText("Apt / House Address");
        txtAddress.setStyle(fieldStyle);

        TextField txtIssue = new TextField();
        txtIssue.setPromptText("Issue Description");
        txtIssue.setStyle(fieldStyle);

        // --- FIX FOR INVISIBLE CATEGORY TEXT ---
        ComboBox<String> comboCategory = new ComboBox<>();
        comboCategory.getItems().addAll("Plumbing", "Electrical", "Structural", "Appliance");
        comboCategory.setPromptText("Category");
        comboCategory.setMaxWidth(Double.MAX_VALUE);
        comboCategory.setStyle(fieldStyle);

        // Popup Styling
        comboCategory.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #3c3c3c;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white;");
                }
            }
        });
        comboCategory.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(comboCategory.getPromptText());
                    setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
                }
            }
        });

        // Severity Slider
        Label lblSeverity = new Label("Urgency Level: 5");
        lblSeverity.setStyle(labelStyle);

        Slider sliderSeverity = new Slider(1, 10, 5);
        sliderSeverity.setShowTickMarks(true);
        sliderSeverity.setShowTickLabels(true);
        sliderSeverity.setMajorTickUnit(1);
        sliderSeverity.setBlockIncrement(1);
        sliderSeverity.setStyle("-fx-control-inner-background: #3c3c3c;");
        sliderSeverity.valueProperty().addListener((obs, oldVal, newVal) ->
                lblSeverity.setText("Urgency Level: " + newVal.intValue()));

        Button btnAdd = new Button("Submit");
        btnAdd.setStyle(buttonStyle);
        btnAdd.setMaxWidth(Double.MAX_VALUE);

        // Styling input labels
        Label lblName = new Label("Name:"); lblName.setStyle(labelStyle);
        Label lblAddr = new Label("Address:"); lblAddr.setStyle(labelStyle); // New Label
        Label lblCat = new Label("Category:"); lblCat.setStyle(labelStyle);
        Label lblIssue = new Label("Issue:"); lblIssue.setStyle(labelStyle);

        // --- 2. RIGHT PANEL: DASHBOARD (The Queue) ---
        VBox dashboardPanel = new VBox(15);
        dashboardPanel.setPadding(new Insets(20));
        dashboardPanel.setStyle(darkBackground);
        HBox.setHgrow(dashboardPanel, Priority.ALWAYS);

        Label dashHeader = new Label("Active Job Queue (Sorted by Urgency)");
        dashHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        dashHeader.setStyle("-fx-text-fill: #81c784;");

        // Setup Table Columns
        TableColumn<Request, Integer> colSev = new TableColumn<>("Pri");
        colSev.setCellValueFactory(new PropertyValueFactory<>("severity"));
        colSev.setPrefWidth(40);
        colSev.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
                } else {
                    setText(item.toString());
                    if (item >= 8) setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; -fx-font-weight: bold;");
                    else if (item >= 5) setStyle("-fx-background-color: #f57f17; -fx-text-fill: black;");
                    else setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white;");
                }
            }
        });

        // Add Address Column to Table
        TableColumn<Request, String> colAddr = new TableColumn<>("Address");
        colAddr.setCellValueFactory(new PropertyValueFactory<>("address"));
        colAddr.setPrefWidth(120);

        TableColumn<Request, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Request, String> colIssue = new TableColumn<>("Issue");
        colIssue.setCellValueFactory(new PropertyValueFactory<>("issue"));
        colIssue.setPrefWidth(200);

        table.getColumns().addAll(colSev, colAddr, colCat, colIssue); // Added colAddr here
        table.setItems(tableData);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-background-color: #2b2b2b; -fx-control-inner-background: #2b2b2b; -fx-table-cell-border-color: #444; -fx-text-fill: white;");

        // Dispatch Button
        Button btnDispatch = new Button("Dispatch Top Priority Job (Admin Only)");
        btnDispatch.setStyle(dispatchBtnStyle);
        btnDispatch.setMaxWidth(Double.MAX_VALUE);

        statusLabel.setStyle("-fx-text-fill: #888;");

        // --- 3. LOGIC & EVENT HANDLERS ---

        btnAdd.setOnAction(e -> {
            if (txtName.getText().isEmpty() || txtAddress.getText().isEmpty() || txtIssue.getText().isEmpty() || comboCategory.getValue() == null) {
                statusLabel.setText("Error: Please fill all fields.");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            Request newReq = new Request(
                    0,
                    txtName.getText(),
                    txtAddress.getText(), // Pass Address
                    txtIssue.getText(),
                    comboCategory.getValue(),
                    (int) sliderSeverity.getValue(),
                    "PENDING"
            );

            DatabaseHandler.saveRequest(newReq);
            triageQueue.add(newReq);    
            loadFromDB();

            txtName.clear();
            txtAddress.clear(); // Clear Address field
            txtIssue.clear();
            statusLabel.setText("com.maintenance.Request Added Successfully!");
            statusLabel.setTextFill(Color.LIGHTGREEN);
        });

        // --- SECURE ADMIN DISPATCH ACTION ---
        btnDispatch.setOnAction(e -> {
            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Admin Verification");
            passwordDialog.setHeaderText("Restricted Action");
            passwordDialog.setContentText("Please enter Admin Password to dispatch:");

            // Style the dialog for Dark Mode
            DialogPane pane = passwordDialog.getDialogPane();
            pane.setStyle("-fx-background-color: #2b2b2b;");
            pane.lookup(".content.label").setStyle("-fx-text-fill: white;");
            pane.lookup(".header-panel").setStyle("-fx-background-color: #1e1e1e;");
            pane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");

            Optional<String> result = passwordDialog.showAndWait();

            if (result.isPresent() && result.get().equals("admin123")) {
                if (triageQueue.isEmpty()) {
                    statusLabel.setText("Queue is empty! No jobs to dispatch.");
                    return;
                }

                Request job = triageQueue.poll();
                DatabaseHandler.markRequestCompleted(job.getId());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Job Dispatched");
                // Updated Alert to show Address
                alert.setHeaderText("Dispatching to: " + job.getAddress());
                alert.setContentText("Tenant: " + job.getTenantName() + "\nIssue: " + job.getIssue() + "\nUrgency: " + job.getSeverity());
                alert.getDialogPane().setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
                alert.showAndWait();

                refreshTable();
                statusLabel.setText("Dispatched job for " + job.getTenantName());
                statusLabel.setTextFill(Color.LIGHTGREEN);
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Access Denied");
                errorAlert.setHeaderText("Unauthorized Action");
                errorAlert.setContentText("Incorrect Password! You cannot dispatch jobs.");
                errorAlert.getDialogPane().setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
                errorAlert.showAndWait();
            }
        });

        loadFromDB();

        // Assemble Layout with new Address field
        inputPanel.getChildren().addAll(header, lblName, txtName, lblAddr, txtAddress, lblCat, comboCategory, lblIssue, txtIssue, lblSeverity, sliderSeverity, btnAdd, statusLabel);
        dashboardPanel.getChildren().addAll(dashHeader, table, btnDispatch);

        HBox root = new HBox(inputPanel, dashboardPanel);
        Scene scene = new Scene(root, 950, 600);
        root.setStyle(darkBackground);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- HELPER METHODS ---

    private void loadFromDB() {
        triageQueue.clear();
        List<Request> dbList = DatabaseHandler.loadRequests();
        triageQueue.addAll(dbList);
        refreshTable();
    }

    private void refreshTable() {
        tableData.clear();
        Object[] sortedArr = triageQueue.toArray();
        Arrays.sort(sortedArr);
        for (Object o : sortedArr) {
            tableData.add((Request) o);
        }
    }
}