package com.developersstack.edumanage.controller;

import com.developersstack.edumanage.db.Database;
import com.developersstack.edumanage.db.DbConnection;
import com.developersstack.edumanage.model.Program;
import com.developersstack.edumanage.model.Student;
import com.developersstack.edumanage.model.Teacher;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.awt.SystemColor.text;

public class ProgramsFormController {

    public AnchorPane context;


    public TextField txtId;


    public TextField txtName;


    public TextField txtSearch;


    public Button btn;


    public TableView<ProgramTm> tblPrograms;


    public TableColumn<?, ?> colId;


    public TableColumn<?, ?> colName;


    public TableColumn<?, ?> colTeacher;


    public TableColumn<?, ?> colTech;


    public TableColumn<?, ?> colCost;


    public TableColumn<?, ?> colOption;


    public TextField txtCost;


    public ComboBox<String> cmbTeacher;


    public TextField txtTechnology;


    public TableView<TechAddTm> tblTechnologies;


    public TableColumn<?, ?> colTCode;


    public TableColumn<?, ?> colTName;


    public TableColumn<?, ?> colTRemove;

    public void initialize() {
        setProgramCode();
        setTeachers();
        loadPrograms();

        colId.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        colTech.setCellValueFactory(new PropertyValueFactory<>("btnTech"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colOption.setCellValueFactory(new PropertyValueFactory<>("btn"));


        colTCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colTName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTRemove.setCellValueFactory(new PropertyValueFactory<>("btn"));

        try {
            loadTeachersComboBox();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

        tblPrograms.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (null!=newValue){
                        setData(newValue);
                    }
                });

        tblTechnologies.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setTechData(newValue);
        });

    }


    ArrayList<String> teachersArray = new ArrayList<>();

    ObservableList<TechAddTm> tmList = FXCollections.observableArrayList();

    public String getTechnologyIdsFromDatabase(Program p) throws SQLException, ClassNotFoundException {
        String techIds = null;

        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT technology_id FROM program WHERE code = ?");
        preparedStatement.setString(1, p.getCode());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            techIds= resultSet.getString("technology_id");
        }

        return techIds;
    }

    private void setData(ProgramTm tm) {
        txtId.setText(tm.getCode());
        txtName.setText(tm.getName());
        txtCost.setText(String.valueOf(tm.getCost()));

        try {
            // Retrieve technology IDs associated with the program using the Program model
            Program program = new Program();
            program.setCode(tm.getCode());

            Connection connection = DbConnection.getInstance().getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT CONCAT(teacher_id, '.', full_name) AS teacher FROM teacher WHERE teacher_id = ?");
            preparedStatement.setString(1, tm.getTeacher());
            ResultSet resultSet = preparedStatement.executeQuery();

            String teacherValue = "";
            if (resultSet.next()) {
                teacherValue = resultSet.getString("teacher");
            }

            // Set teacher value in the format "T-1.teachername"
            cmbTeacher.setValue(teacherValue);

            String technologyIds = getTechnologyIdsFromDatabase(program);
            // Remove square brackets from input string
            String modifiedString = technologyIds.substring(1, technologyIds.length() - 1);

            // Split the modified string by comma (",") delimiter
            String[] stringArray = modifiedString.split(",");

            // Convert String[] to List<String>
            List<String> stringList = Arrays.asList(stringArray);

            List<TechAddTm> technologies = new ArrayList<>();
            int idCounter = 1;
            for (String techId : stringList) {
                Button btn = new Button("Remove");
                TechAddTm techAddTm = new TechAddTm(idCounter, techId, btn);
                technologies.add(techAddTm);
                idCounter++;
            }
            ObservableList<TechAddTm> technologiesList = FXCollections.observableArrayList(technologies);
            tblTechnologies.setItems(technologiesList);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }




    private void setTechData(TechAddTm newValue) {
        txtTechnology.setText(String.valueOf(newValue.getName()));

    }

    private void setTeachers() {
        for (Teacher t : Database.teacherTable
        ) {
            teachersArray.add(t.getCode() + ". " + t.getName());
        }
        ObservableList<String> obList = FXCollections.observableArrayList(teachersArray);
        cmbTeacher.setItems(obList);
    }

    private void setProgramCode() {
        try {
            Connection connection = DbConnection.getInstance().getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT code FROM program ORDER BY code DESC LIMIT 1");
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String lastId = resultSet.getString("code");
                String[] splitData = lastId.split("-");
                int lastIntegerIdAsInt = Integer.parseInt(splitData[1]);
                lastIntegerIdAsInt++;
                String generatedProgramId = "P-" + lastIntegerIdAsInt;
                txtId.setText(generatedProgramId);
            } else {
                txtId.setText("P-1");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
    }


    public void backToHomeOnAction(ActionEvent event) throws IOException {
        setUi("DashboardForm");
    }


    public void newProgramOnAction(ActionEvent event) {
        clearFields();
    }


    public void saveOnAction(ActionEvent event) throws SQLException, ClassNotFoundException {
        String[] selectedTechs = new String[tmList.size()];
        int pointer = 0;
        for (TechAddTm t : tmList) {
            selectedTechs[pointer] = t.getName();
            pointer++;
        }

        if (btn.getText().equals("Save Program")) {
            String selectedTeacher = cmbTeacher.getValue();
            if (selectedTeacher == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a teacher").show();
                return;
            }
            // Extract the teacher ID from the selected value
            String[] selectedTeacherParts = selectedTeacher.split("\\.");
            String teacherId = selectedTeacherParts[0];

            Program program = new Program(
                    txtId.getText(),
                    txtName.getText(),
                    selectedTechs,
                    teacherId, // Use the extracted teacher ID
                    Double.parseDouble(txtCost.getText())
            );
            Database.programTable.add(program);

            Connection connection = DbConnection.getInstance().getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement("INSERT INTO program VALUES (?,?,?,?,?)");
            preparedStatement.setString(1, program.getCode());
            preparedStatement.setString(2, program.getName());
            preparedStatement.setDouble(3, program.getCost());
            preparedStatement.setString(4, Arrays.toString(program.getTechnologies()));
            preparedStatement.setString(5, program.getTeacherId());

            boolean saved = preparedStatement.executeUpdate() > 0;

            if (saved) {
                new Alert(Alert.AlertType.INFORMATION, "Saved").show();
                loadPrograms();
                clearFields();
            } else {
                new Alert(Alert.AlertType.WARNING, "Try Again!").show();
            }
        }
    }

    private void loadPrograms() {
        try {
            Connection connection = DbConnection.getInstance().getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT code, name, teacher_id, cost FROM program");
            ResultSet resultSet = preparedStatement.executeQuery();

            ObservableList<ProgramTm> programsTmList = FXCollections.observableArrayList();
            while (resultSet.next()) {
                String code = resultSet.getString("code");
                String name = resultSet.getString("name");
                String teacherId = resultSet.getString("teacher_id");
                double cost = resultSet.getDouble("cost");

                Button techButton = new Button("show Tech");
                Button removeButton = new Button("Delete");
                ProgramTm tm = new ProgramTm(
                        code,
                        name,
                        teacherId,
                        techButton,
                        cost,
                        removeButton
                );
                programsTmList.add(tm);
            }
            tblPrograms.setItems(programsTmList);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
    }

    private void setUi(String location) throws IOException {
        Stage stage = (Stage) context.getScene().getWindow();
        stage.setScene(new Scene(
                FXMLLoader.load(getClass().getResource("../view/" + location + ".fxml"))));
        stage.centerOnScreen();
    }



    public void addTechOnAction(ActionEvent actionEvent) {
        if (!isExists(txtTechnology.getText().trim())) {
            Button btn = new Button("Remove");
            TechAddTm tm = new TechAddTm(
                    tmList.size() + 1, txtTechnology.getText().trim(), btn
            );
            tmList.add(tm);
            tblTechnologies.setItems(tmList);
            txtTechnology.clear();
        } else {
            txtTechnology.selectAll();
            new Alert(Alert.AlertType.WARNING, "Already Exists").show();
        }
    }

    private void clearFields() {
        setProgramCode();
        txtTechnology.clear();
        txtName.clear();
        txtCost.clear();
        cmbTeacher.setValue(null);
        tblTechnologies.getItems().clear();
    }

    private boolean isExists(String tech) {
        for (TechAddTm tm : tmList
        ) {
            if (tm.getName().equals(tech)) {
                return true;
            }
        }
        return false;
    }

    private void loadTeachersComboBox() throws ClassNotFoundException, SQLException {
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT teacher_id, full_name FROM teacher");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            // Assuming the ComboBox is capable of holding both ID and full name
            cmbTeacher.getItems().add(resultSet.getString("teacher_id") + "." + resultSet.getString("full_name"));
        }
    }
}
