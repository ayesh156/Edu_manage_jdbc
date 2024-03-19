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
import java.util.Optional;

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

    String searchText="";

    public void initialize() throws SQLException, ClassNotFoundException {
        setProgramCode();
        setTeachers();
        setProgramData(searchText);
        loadTeachersComboBox();

        colId.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        colTech.setCellValueFactory(new PropertyValueFactory<>("btnTech"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colOption.setCellValueFactory(new PropertyValueFactory<>("btn"));


        colTCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colTName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTRemove.setCellValueFactory(new PropertyValueFactory<>("btn"));

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchText=newValue;
            setProgramData(searchText);
        });

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
                Button removeBtn = new Button("Remove");
                TechAddTm techAddTm = new TechAddTm(idCounter, techId, removeBtn);

                removeBtn.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.YES) {
                        technologies.remove(techAddTm);
                        tblTechnologies.setItems(FXCollections.observableArrayList(technologies));
                    }
                });
                technologies.add(techAddTm);
                idCounter++;
            }
            ObservableList<TechAddTm> technologiesList = FXCollections.observableArrayList(technologies);
            tblTechnologies.setItems(technologiesList);
            btn.setText("Update Program");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void setTechData(TechAddTm newValue) {
        if (newValue != null) {
            txtTechnology.setText(String.valueOf(newValue.getName()));
        }
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
        btn.setText("Save Program");
    }


    public void saveOnAction(ActionEvent event) throws SQLException, ClassNotFoundException {

        if (btn.getText().equals("Save Program")) {
            String[] selectedTechs = new String[tmList.size()];
            int pointer = 0;
            for (TechAddTm t : tmList) {
                selectedTechs[pointer] = t.getName();
                pointer++;
            }

            String selectedTeacher = cmbTeacher.getValue();
            if (selectedTeacher == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a teacher").show();
                return;
            }
            // Extract the teacher ID from the selected value
            String[] selectedTeacherParts = selectedTeacher.split("\\.");
            String teacherId = selectedTeacherParts[1];

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
                setProgramData(searchText);
                clearFields();
            } else {
                new Alert(Alert.AlertType.WARNING, "Try Again!").show();
            }

        }else if(btn.getText().equals("Update Program")){

            ObservableList<TechAddTm> items = tblTechnologies.getItems();
            String[] technologyNames = new String[items.size()];

            for (int i = 0; i < items.size(); i++) {
                TechAddTm techAddTm = items.get(i);
                technologyNames[i] = techAddTm.getName();
            }

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
                    technologyNames,
                    teacherId, // Use the extracted teacher ID
                    Double.parseDouble(txtCost.getText())
            );

            Database.programTable.add(program);

            Connection connection = DbConnection.getInstance().getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement("UPDATE program SET  name=?, cost=?, technology_id=?, teacher_id=? WHERE code=?");
            preparedStatement.setString(1, program.getName());
            preparedStatement.setDouble(2, program.getCost());
            preparedStatement.setString(3, Arrays.toString(program.getTechnologies()));
            preparedStatement.setString(4, program.getTeacherId());
            preparedStatement.setString(5, program.getCode());

            boolean saved = preparedStatement.executeUpdate() > 0;

            if (saved) {
                new Alert(Alert.AlertType.INFORMATION, "Updated").show();
                setProgramData(searchText);
                clearFields();
                btn.setText("Save Program");
            } else {
                new Alert(Alert.AlertType.WARNING, "Try Again!").show();
            }
        }
    }

    private void setProgramData(String searchText) {
        try {
            ObservableList<ProgramTm> programsTmList = FXCollections.observableArrayList();
            for (Program pt: searchPrograms(searchText)
            ) {

                Button techButton = new Button("show Tech");
                Button removeButton = new Button("Delete");
                ProgramTm tm = new ProgramTm(
                        pt.getCode(),
                        pt.getName(),
                        pt.getTeacherId(),
                        techButton,
                        pt.getCost(),
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
                            if(deleteProgram(pt.getCode())){
                                new Alert(Alert.AlertType.INFORMATION, "Deleted!").show();
                                setProgramData(searchText);
                                setProgramCode();
                            }else {
                                new Alert(Alert.AlertType.WARNING, "Try Again!").show();
                            }
                        } catch (ClassNotFoundException | SQLException ex) {
                            new Alert(Alert.AlertType.ERROR, ex.toString()).show();
                        }

                    }
                });

                programsTmList.add(tm);
            }
            tblPrograms.setItems(programsTmList);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
    }

    private List<Program> searchPrograms(String text) throws ClassNotFoundException, SQLException {
        text = "%" + text + "%";
        // Create a Connection
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM program JOIN teacher t on program.teacher_id = t.teacher_id WHERE name LIKE ? OR t.full_name LIKE ?");
        preparedStatement.setString(1,text);
        preparedStatement.setString(2,text);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Program> list = new ArrayList<>();
        while (resultSet.next()){
            list.add(
                    new Program(
                            resultSet.getString("code"),
                            resultSet.getString("name"),
                            new String[]{resultSet.getString("technology_id")},
                            resultSet.getString("teacher_id"),
                            resultSet.getDouble("cost")
                    )
            );
        }
        return list;
    }

    private boolean deleteProgram(String id) throws ClassNotFoundException, SQLException {
        // Create a Connection
        Connection connection = null;
        try {
            connection = DbConnection.getInstance().getConnection();
            // Start transaction
            connection.setAutoCommit(false);

            // Delete related intakes first
            PreparedStatement preparedStatementIntake =
                    connection.prepareStatement("DELETE FROM intake WHERE program_code=?");
            preparedStatementIntake.setString(1, id);
            preparedStatementIntake.executeUpdate();

            // Now, delete the program
            PreparedStatement preparedStatement =
                    connection.prepareStatement("DELETE FROM program WHERE code=?");
            preparedStatement.setString(1, id);
            boolean programDeleted = preparedStatement.executeUpdate() > 0;

            // Commit transaction
            connection.commit();

            return programDeleted;
        } catch (SQLException e) {
            // Rollback in case of an exception
            if (connection != null) {
                connection.rollback();
            }
            // Handle or rethrow the exception
            throw e;
        } finally {
            // Close connection
            if (connection != null) {
                connection.setAutoCommit(true); // Reset auto-commit
            }
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

            btn.setOnAction(e -> {
                Alert alert = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "Are you sure?",
                        ButtonType.YES, ButtonType.NO
                );
                Optional<ButtonType> buttonType = alert.showAndWait();
                if (buttonType.isPresent() && buttonType.get() == ButtonType.YES) {
                    // Get the row associated with the button
                    TechAddTm item = (TechAddTm) btn.getUserData();
                    if (item != null) {
                        // Remove the item from the list
                        tmList.remove(item);
                        // Update the TableView
                        tblTechnologies.setItems(FXCollections.observableArrayList(tmList));
                    }
                }
            });

            // Set the button's user data to the TechAddTm object
            btn.setUserData(tm);

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
        txtSearch.clear();
        tblPrograms.getSelectionModel().clearSelection();
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
