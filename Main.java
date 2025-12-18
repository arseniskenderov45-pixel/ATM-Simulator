import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

class UserAccount {
    String name;
    String pin;
    double balance;
    List<String> history;

    public UserAccount(String name, String pin) {
        this.name = name;
        this.pin = pin;
        this.balance = 0.0;
        this.history = new ArrayList<>();
        addRecord("Аккаунт открыт");
    }

    public void addRecord(String record) {
        history.add(0, record);
        if (history.size() > 10) history.remove(history.size() - 1);
    }
}


class BankSystem {
    private static final int MAX_USERS = 10;
    private static final String FILE_NAME = "users.txt";
    private Map<String, UserAccount> users = new HashMap<>();

    public BankSystem() {
        loadData(); 
    }

   
    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (UserAccount user : users.values()) {
                
                writer.println(user.name + ";" + user.pin + ";" + user.balance);
            }
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

   
    private void loadData() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    UserAccount u = new UserAccount(parts[0], parts[1]);
                    u.balance = Double.parseDouble(parts[2]);
                    users.put(parts[0], u);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден.");
        }
    }

    
    public void updateDatabase() {
        saveData();
    }

    public String register(String name, String pin) {
        if (users.size() >= MAX_USERS) return "Ошибка: Банк переполнен.";
        if (users.containsKey(name)) return "Ошибка: Имя занято.";
        if (pin.length() != 4 || !pin.matches("\\d+")) return "Ошибка: ПИН - 4 цифры.";
        
        users.put(name, new UserAccount(name, pin));
        saveData(); 
        return "SUCCESS";
    }

    public UserAccount login(String name, String pin) {
        UserAccount user = users.get(name);
        if (user != null && user.pin.equals(pin)) {
            return user;
        }
        return null;
    }
    
    public UserAccount findUser(String name) {
        return users.get(name);
    }
}

public class Main extends Application {

    private BankSystem bank = new BankSystem();
    private UserAccount currentUser;
    private StackPane screenContainer;
    private TextField activeInputField; 

    @Override
    public void start(Stage primaryStage) {
        BorderPane atmBody = new BorderPane();
        atmBody.setStyle("-fx-background-color: #5d6d7e; -fx-padding: 20;");
        
        Label brand = new Label("JAVA BANK ATM System");
        brand.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        brand.setTextFill(Color.WHITE);
        HBox topBox = new HBox(brand);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        atmBody.setTop(topBox);

        screenContainer = new StackPane();
        screenContainer.setMaxSize(600, 400);
        screenContainer.setMinSize(600, 400);
        screenContainer.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #2c3e50; -fx-border-width: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        screenContainer.setEffect(new DropShadow(10, Color.BLACK));
        
        atmBody.setCenter(screenContainer);
        atmBody.setBottom(createKeypad());

        showLoginScreen();

        Scene scene = new Scene(atmBody, 800, 750);
        primaryStage.setTitle("JavaFX ATM Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- ЭКРАНЫ ---

    private void showLoginScreen() {
        VBox layout = createScreenLayout();
        Label title = new Label("Вставьте карту или введите данные");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Имя пользователя");
        setActiveFieldLogic(nameField);

        PasswordField pinField = new PasswordField();
        pinField.setPromptText("ПИН-код");
        setActiveFieldLogic(pinField);

        Button btnLogin = new Button("ВОЙТИ");
        styleScreenButton(btnLogin);
        btnLogin.setOnAction(e -> {
            UserAccount user = bank.login(nameField.getText(), pinField.getText());
            if (user != null) {
                currentUser = user;
                showMainMenu();
            } else {
                showAlert("Ошибка входа", "Неверное имя или ПИН");
            }
        });

        Button btnRegister = new Button("СОЗДАТЬ АККАУНТ");
        styleScreenButton(btnRegister);
        btnRegister.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        btnRegister.setOnAction(e -> showRegisterScreen());

        layout.getChildren().addAll(title, nameField, pinField, btnLogin, btnRegister);
        screenContainer.getChildren().setAll(layout);
    }

    private void showRegisterScreen() {
        VBox layout = createScreenLayout();
        Label title = new Label("Регистрация нового клиента");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Придумайте Имя");
        setActiveFieldLogic(nameField);

        TextField pinField = new TextField(); 
        pinField.setPromptText("Придумайте ПИН (4 цифры)");
        setActiveFieldLogic(pinField);

        Button btnCreate = new Button("ПОДТВЕРДИТЬ");
        styleScreenButton(btnCreate);
        btnCreate.setOnAction(e -> {
            String result = bank.register(nameField.getText(), pinField.getText());
            if (result.equals("SUCCESS")) {
                showAlert("Успех", "Аккаунт создан! Войдите.");
                showLoginScreen();
            } else {
                showAlert("Ошибка", result);
            }
        });

        Button btnBack = new Button("НАЗАД");
        styleScreenButton(btnBack);
        btnBack.setOnAction(e -> showLoginScreen());

        layout.getChildren().addAll(title, nameField, pinField, btnCreate, btnBack);
        screenContainer.getChildren().setAll(layout);
    }

    private void showMainMenu() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(20);

        Label welcome = new Label("Клиент: " + currentUser.name + "\nБаланс: $" + currentUser.balance);
        welcome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button btnDeposit = createMenuButton("Пополнить");
        btnDeposit.setOnAction(e -> showTransactionScreen("Пополнение"));

        Button btnWithdraw = createMenuButton("Снять");
        btnWithdraw.setOnAction(e -> showTransactionScreen("Снятие"));

        Button btnTransfer = createMenuButton("Перевод");
        btnTransfer.setOnAction(e -> showTransferScreen());

        Button btnHistory = createMenuButton("История");
        btnHistory.setOnAction(e -> showHistoryScreen());

        Button btnExit = createMenuButton("ВЫХОД");
        btnExit.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-pref-width: 150; -fx-pref-height: 50;");
        btnExit.setOnAction(e -> {
            currentUser = null;
            showLoginScreen();
        });

        grid.add(welcome, 0, 0, 2, 1);
        grid.add(btnDeposit, 0, 1);
        grid.add(btnWithdraw, 1, 1);
        grid.add(btnTransfer, 0, 2);
        grid.add(btnHistory, 1, 2);
        grid.add(btnExit, 0, 3, 2, 1);

        screenContainer.getChildren().setAll(grid);
    }

    private void showTransactionScreen(String type) {
        VBox layout = createScreenLayout();
        Label title = new Label(type + " средств");
        
        TextField amountField = new TextField();
        amountField.setPromptText("Сумма");
        setActiveFieldLogic(amountField);

        Button btnOk = new Button("ВЫПОЛНИТЬ");
        styleScreenButton(btnOk);
        btnOk.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (type.equals("Пополнение")) {
                    if (amount > 0) {
                        currentUser.balance += amount;
                        currentUser.addRecord("Пополнение: +$" + amount);
                        bank.updateDatabase(); 
                        showAlert("Успех", "Баланс пополнен.");
                        showMainMenu();
                    }
                } else { 
                    if (amount > 0 && currentUser.balance >= amount) {
                        currentUser.balance -= amount;
                        currentUser.addRecord("Снятие: -$" + amount);
                        bank.updateDatabase(); 
                        showAlert("Успех", "Деньги выданы.");
                        showMainMenu();
                    } else {
                        showAlert("Ошибка", "Недостаточно средств.");
                    }
                }
            } catch (Exception ex) {
                showAlert("Ошибка", "Введите число.");
            }
        });

        Button btnCancel = new Button("ОТМЕНА");
        styleScreenButton(btnCancel);
        btnCancel.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(title, amountField, btnOk, btnCancel);
        screenContainer.getChildren().setAll(layout);
    }

    private void showTransferScreen() {
        VBox layout = createScreenLayout();
        Label title = new Label("Перевод клиенту");
        
        TextField targetUserField = new TextField();
        targetUserField.setPromptText("Имя получателя");
        setActiveFieldLogic(targetUserField);

        TextField amountField = new TextField();
        amountField.setPromptText("Сумма");
        setActiveFieldLogic(amountField);

        Button btnSend = new Button("ОТПРАВИТЬ");
        styleScreenButton(btnSend);
        btnSend.setOnAction(e -> {
            String targetName = targetUserField.getText();
            UserAccount target = bank.findUser(targetName);
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (target != null && amount > 0 && currentUser.balance >= amount) {
                    if(target.name.equals(currentUser.name)) {
                        showAlert("Ошибка", "Нельзя переводить самому себе.");
                        return;
                    }
                    currentUser.balance -= amount;
                    target.balance += amount;
                    
                    currentUser.addRecord("Перевод для " + targetName + ": -$" + amount);
                    target.addRecord("Перевод от " + currentUser.name + ": +$" + amount);
                    
                    bank.updateDatabase(); 
                    
                    showAlert("Успех", "Перевод выполнен!");
                    showMainMenu();
                } else {
                    showAlert("Ошибка", "Неверный получатель или мало средств.");
                }
            } catch (Exception ex) {
                showAlert("Ошибка", "Проверьте сумму.");
            }
        });

        Button btnCancel = new Button("ОТМЕНА");
        styleScreenButton(btnCancel);
        btnCancel.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(title, targetUserField, amountField, btnSend, btnCancel);
        screenContainer.getChildren().setAll(layout);
    }

    private void showHistoryScreen() {
        VBox layout = createScreenLayout();
        Label title = new Label("История операций");
        
        ListView<String> historyView = new ListView<>();
        historyView.getItems().addAll(currentUser.history);
        historyView.setMaxHeight(200);

        Button btnBack = new Button("НАЗАД");
        styleScreenButton(btnBack);
        btnBack.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(title, historyView, btnBack);
        screenContainer.getChildren().setAll(layout);
    }


    private VBox createKeypad() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));

        GridPane keypad = new GridPane();
        keypad.setHgap(10);
        keypad.setVgap(10);
        keypad.setAlignment(Pos.CENTER);

        int counter = 1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button btn = createKeypadButton(String.valueOf(counter));
                keypad.add(btn, j, i);
                counter++;
            }
        }
        
        Button btnClear = createKeypadButton("C");
        btnClear.setStyle("-fx-background-color: #f1c40f; -fx-font-weight: bold; -fx-font-size: 16px;");
        btnClear.setOnAction(e -> { if (activeInputField != null) activeInputField.clear(); });

        Button btnZero = createKeypadButton("0");
        Button btnEnter = createKeypadButton("OK");
        btnEnter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        keypad.add(btnClear, 0, 3);
        keypad.add(btnZero, 1, 3);
        keypad.add(btnEnter, 2, 3);

        container.getChildren().add(keypad);
        return container;
    }

    private Button createKeypadButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(60, 60);
        btn.setStyle("-fx-background-color: #bdc3c7; -fx-font-size: 18px; -fx-background-radius: 10;");
        btn.setOnMousePressed(e -> btn.setStyle("-fx-background-color: #95a5a6; -fx-font-size: 18px; -fx-background-radius: 10;"));
        btn.setOnMouseReleased(e -> btn.setStyle("-fx-background-color: #bdc3c7; -fx-font-size: 18px; -fx-background-radius: 10;"));

        if (!text.equals("C") && !text.equals("OK")) {
            btn.setOnAction(e -> { if (activeInputField != null) activeInputField.appendText(text); });
        }
        return btn;
    }

    private void setActiveFieldLogic(TextField field) {
        field.setOnMouseClicked(e -> activeInputField = field);
        activeInputField = field;
    }

    private VBox createScreenLayout() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        return layout;
    }

    private void styleScreenButton(Button btn) {
        btn.setPrefWidth(200);
        btn.setPrefHeight(40);
        btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
    }
    
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(150, 50);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px;");
        return btn;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
