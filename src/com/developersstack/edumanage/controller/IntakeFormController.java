package com.developersstack.edumanage.controller;

import com.developersstack.edumanage.db.Database;
import com.developersstack.edumanage.db.DbConnection;
import com.developersstack.edumanage.model.Intake;
import com.developersstack.edumanage.model.Program;
import com.developersstack.edumanage.model.Teacher;
import com.developersstack.edumanage.view.tm.IntakesTm;
import com.developersstack.edumanage.view.tm.ProgramTm;
import com.developersstack.edumanage.view.tm.StudentTm;
import com.developersstack.edumanage.view.tm.TechAddTm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IntakeFormController {

    public AnchorPane context;
    public TextField txtId;
    public TextField txtSearch;
    public Button btn;
    public TableView tblIntakes;
    public TableColumn colId;
    public TableColumn colIntake;
    public TableColumn colStartDate;
    public TableColumn colProgram;
    public TableColumn colCompleteState;
    public TableColumn colOption;
    public TextField txtName;
    public DatePicker txtDate;
    public ComboBox cmbProgram;

    String searchText="";

    public void initialize() throws SQLException, ClassNotFoundException {
        setIntakeId();
        setIntakeData(searchText);
        loadProgramComboBox();

        colId.setCellValueFactory(new PropertyValueFactory<>("intakeId"));
        colIntake.setCellValueFactory(new PropertyValueFactory<>("intakeName"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("programId"));
        colCompleteState.setCellValueFactory(new PropertyValueFactory<>("completeState"));
        colOption.setCellValueFactory(new PropertyValueFactory<>("btn"));

        tblIntakes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (null!=newValue){
                        setData((IntakesTm) newValue);
                    }
                });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchText=newValue;
            setIntakeData(searchText);
        });
    }

    private void setData(IntakesTm tm) {
        txtId.setText(tm.getIntakeId());
        txtDate.setValue(LocalDate.parse(tm.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        txtName.setText(tm.getIntakeName());
        try {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT CONCAT(code, '.', name) AS programn FROM program WHERE name = ?");
        preparedStatement.setString(1, tm.getProgramId());
        ResultSet resultSet = preparedStatement.executeQuery();

        String programValue = "";
        if (resultSet.next()) {
            programValue = resultSet.getString("programn");
        }

        // Set teacher value in the format "T-1.teachername"
        cmbProgram.setValue(programValue);

        btn.setText("Update Intake");

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    ArrayList<String> programsArray = new ArrayList<>();
    private void setIntakeId() {
        try {
            Connection connection = DbConnection.getInstance().getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT intake_id FROM intake ORDER BY intake_id DESC LIMIT 1");
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String lastId = resultSet.getString("intake_id");
                String[] splitData = lastId.split("-");
                int lastIntegerIdAsInt = Integer.parseInt(splitData[1]);
                lastIntegerIdAsInt++;
                String generatedIntakeId = "I-" + lastIntegerIdAsInt;
                txtId.setText(generatedIntakeId);
            } else {
                txtId.setText("I-1");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
    }

    private void setIntakeData(String searchText) {
        try {
            ObservableList<IntakesTm> intakeTmList = FXCollections.observableArrayList();
            for (Intake it: searchPrograms(searchText)
            ) {
                Button removeButton = new Button("Delete");
                IntakesTm tm = new IntakesTm(
                        it.getIntakeId(),
                        String.valueOf(it.getStartDate()),
                        it.getIntakeName(),
                        it.getProgramId(),
                        it.getIntakeCompleteness(),
                        removeButton
                );

                removeButton.setOnAction(e->{
                    Alert alert= new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "Are you sure?",
                            ButtonType.YES,ButtonType.NO
                    );
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.get().equals(ButtonType.YES)){

                        try {
                            if(deleteIntake(it.getIntakeId())){
                                new Alert(Alert.AlertType.INFORMATION, "Deleted!").show();
                                setIntakeData(searchText);
                                setIntakeId();
                            }else {
                                new Alert(Alert.AlertType.WARNING, "Try Again!").show();
                            }
                        } catch (ClassNotFoundException | SQLException ex) {
                            new Alert(Alert.AlertType.ERROR, ex.toString()).show();
                        }

                    }
                });


                intakeTmList.add(tm);
            }
            tblIntakes.setItems(intakeTmList);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
    }

    private boolean deleteIntake(String id) throws ClassNotFoundException, SQLException {
        // Create a Connection
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("DELETE FROM intake WHERE intake_id=?");
        preparedStatement.setString(1,id);
        return preparedStatement.executeUpdate()>0;
    }

    private List<Intake> searchPrograms(String text) throws ClassNotFoundException, SQLException {
        text = "%" + text + "%";
        // Create a Connection
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT intake_id, start_date, intake_name, p.name AS pname, intake_com  FROM intake JOIN program p on p.code = intake.program_code  WHERE intake_name LIKE ? OR p.name LIKE ?");
        preparedStatement.setString(1,text);
        preparedStatement.setString(2,text);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Intake> list = new ArrayList<>();
        while (resultSet.next()){
            list.add(
                    new Intake(
                            resultSet.getString("intake_id"),
                            resultSet.getDate("start_date"),
                            resultSet.getString("intake_name"),
                            resultSet.getString("pname"),
                            resultSet.getString("intake_com")
                    )
            );
        }
        return list;
    }

    private void clearFields() {
        setIntakeId();
        txtName.clear();
        txtDate.setValue(null);
        cmbProgram.setValue(null);
        txtSearch.clear();
        tblIntakes.getSelectionModel().clearSelection();
    }

    public void newIntakeOnAction(ActionEvent actionEvent) {
        clearFields();
        btn.setText("Save Intake");
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        setUi("DashboardForm");
    }

    public void saveOnAction(ActionEvent actionEvent) throws SQLException, ClassNotFoundException {

            String selectedProgram = (String) cmbProgram.getValue();
            if (selectedProgram == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a program").show();
                return;
            }
            // Extract the teacher ID from the selected value
            String[] selectedProgramParts = selectedProgram.split("\\.");
            String programId = selectedProgramParts[0];

            Intake intake = new Intake(
                    txtId.getText(),
                    Date.from(txtDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    txtName.getText(),
                    programId, // Use the extracted teacher ID
                    "true"
            );
            Database.intakeTable.add(intake);

        if (btn.getText().equals("Save Intake")) {

            Connection connection = DbConnection.getInstance().getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement("INSERT INTO intake VALUES (?,?,?,?,?)");
            preparedStatement.setString(1, intake.getIntakeId());
            preparedStatement.setObject(2, intake.getStartDate());
            preparedStatement.setString(3, intake.getIntakeName());
            preparedStatement.setString(4, programId);
            preparedStatement.setString(5, intake.getIntakeCompleteness());

            boolean saved = preparedStatement.executeUpdate() > 0;

            if (saved) {
                new Alert(Alert.AlertType.INFORMATION, "Saved").show();
                setIntakeData(searchText);
                clearFields();
            } else {
                new Alert(Alert.AlertType.WARNING, "Try Again!").show();
            }

        }else if(btn.getText().equals("Update Intake")){

            Connection connection = DbConnection.getInstance().getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement("UPDATE intake SET  start_date=?, intake_name=?, program_code=?, intake_com=? WHERE intake_id=?");
            preparedStatement.setObject(1, intake.getStartDate());
            preparedStatement.setString(2, intake.getIntakeName());
            preparedStatement.setString(3, programId);
            preparedStatement.setString(4, intake.getIntakeCompleteness());;
            preparedStatement.setString(5, intake.getIntakeId());

            boolean saved = preparedStatement.executeUpdate() > 0;

            if (saved) {
                new Alert(Alert.AlertType.INFORMATION, "Updated").show();
                setIntakeData(searchText);
                clearFields();
                btn.setText("Save Intake");
            } else {
                new Alert(Alert.AlertType.WARNING, "Try Again!").show();
            }
        }
    }

    private void setUi(String location) throws IOException {
        Stage stage = (Stage) context.getScene().getWindow();
        stage.setScene(new Scene(
                FXMLLoader.load(getClass().getResource("../view/"+location+".fxml"))));
        stage.centerOnScreen();
    }

    private void loadProgramComboBox() throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT code, name FROM program");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            // Assuming the ComboBox is capable of holding both ID and full name
            cmbProgram.getItems().add(resultSet.getString("code") + "." + resultSet.getString("name"));
        }
    }
}
