package com.developersstack.edumanage.controller;

import com.developersstack.edumanage.db.Database;
import com.developersstack.edumanage.db.DbConnection;
import com.developersstack.edumanage.model.Student;
import com.developersstack.edumanage.model.Teacher;
import com.developersstack.edumanage.view.tm.StudentTm;
import com.developersstack.edumanage.view.tm.TeacherTm;
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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TeacherFormController {
    public AnchorPane teacherContext;
    public TextField txtId;
    public TextField txtName;
    public TextField txtAddress;
    public TextField txtSearch;
    public Button btn;
    public TableView<TeacherTm> tblTeachers;
    public TableColumn colId;
    public TableColumn colName;
    public TableColumn colContact;
    public TableColumn colAddress;
    public TableColumn colOption;
    public TextField txtContact;

    String searchText="";

    public void initialize(){

        colId.setCellValueFactory(new PropertyValueFactory<>("code"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colOption.setCellValueFactory(new PropertyValueFactory<>("btn"));

        setTeacherId();
        setTableData(searchText);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchText=newValue;
            setTableData(searchText);
        });

        tblTeachers.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (null!=newValue){
                        setData(newValue);
                    }
                });
    }

    private void setData(TeacherTm tm) {
        txtId.setText(tm.getCode());
        txtName.setText(tm.getName());
        txtContact.setText(tm.getContact());
        txtAddress.setText(tm.getAddress());
        btn.setText("Update Teacher");
    }

    private void setTableData(String searchText) {
        ObservableList<TeacherTm> obList = FXCollections.observableArrayList();
        try{
        for (Teacher t: searchTeachers(searchText)
        ) {
                Button btn= new Button("Delete");
                TeacherTm tm = new TeacherTm(
                        t.getCode(),
                        t.getName(),
                        t.getContact(),
                        t.getAddress(),
                        btn
                );

                btn.setOnAction(e->{
                    Alert alert= new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "Are you sure?",
                            ButtonType.YES,ButtonType.NO
                    );
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.get().equals(ButtonType.YES)){
                        try {
                            if(deleteTeacher(t.getCode())){
                                new Alert(Alert.AlertType.INFORMATION, "Deleted!").show();
                                setTableData(searchText);
                                setTeacherId();
                            }else{
                                new Alert(Alert.AlertType.WARNING, "Try Again!").show();
                            }
                        } catch (ClassNotFoundException | SQLException ex) {
                            new Alert(Alert.AlertType.ERROR, ex.toString()).show();
                        }

                    }
                });

                obList.add(tm);



        }
        tblTeachers.setItems(obList);
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void saveOnAction(ActionEvent actionEvent) {
        Teacher teacher = new Teacher(
                txtId.getText(),
                txtName.getText(),
                txtAddress.getText(),
                txtContact.getText()
        );
        if (btn.getText().equalsIgnoreCase("Save Teacher")){

            try {
                if(saveTeacher(teacher)){
                    setTeacherId();
                    clear();
                    setTableData(searchText);
                    new Alert(Alert.AlertType.INFORMATION, "Teacher saved!").show();
                }else {
                    new Alert(Alert.AlertType.WARNING, "Try Again!").show();
                }
            } catch (SQLException | ClassNotFoundException e){
                new Alert(Alert.AlertType.ERROR, e.toString()).show();
            }
        }else{
            try {
                if(updateTeacher(teacher)){
                    clear();
                    setTableData(searchText);
                    btn.setText("Save Teacher");
                    new Alert(Alert.AlertType.INFORMATION, "Teacher Updated!").show();
                }else {
                    new Alert(Alert.AlertType.WARNING, "Try Again!").show();
                }
            } catch (SQLException | ClassNotFoundException e){
                new Alert(Alert.AlertType.ERROR, e.toString()).show();
            }
        }
    }

    private void clear(){
        txtContact.clear();
        //txtName.setText("");
        txtName.clear();
        txtAddress.clear();
        tblTeachers.getSelectionModel().clearSelection();
    }

    private void setTeacherId() {
        try{
            String lastId = getLastId();
            if(null!=lastId) {
                String splitData[] = lastId.split("-");
                String lastIdIntegerNumberAsAString = splitData[1];
                int lastIntegerIdAsInt=Integer.parseInt(lastIdIntegerNumberAsAString);
                lastIntegerIdAsInt++;
                String generatedStudentId="T-"+lastIntegerIdAsInt;
                txtId.setText(generatedStudentId);
            } else {
                txtId.setText("T-1");
            }
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void newTeacherOnAction(ActionEvent actionEvent) {
        btn.setText("Save Teacher");
        setTeacherId();
        clear();
    }

    public void backToHomeOnAction(ActionEvent actionEvent) throws IOException {
        setUi("DashboardForm");
    }

    private void setUi(String location) throws IOException {
        Stage stage = (Stage) teacherContext.getScene().getWindow();
        stage.setScene(new Scene(
                FXMLLoader.load(getClass().getResource("../view/"+location+".fxml"))));
        stage.centerOnScreen();
    }

    private boolean saveTeacher(Teacher teacher) throws ClassNotFoundException, SQLException {
        // Create a Connection
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("INSERT INTO teacher VALUES (?,?,?,?)");
        preparedStatement.setString(1,teacher.getCode());
        preparedStatement.setString(2,teacher.getName());
        preparedStatement.setString(3,teacher.getContact());
        preparedStatement.setString(4,teacher.getAddress());
        return preparedStatement.executeUpdate()>0;
    }

    private String getLastId() throws ClassNotFoundException, SQLException {
        // Create a Connection
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT teacher_id FROM teacher ORDER BY CAST(SUBSTRING(teacher_id,3) AS UNSIGNED ) DESC LIMIT 1");
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            return resultSet.getString(1);
        }
        return null;
    }

    private List<Teacher> searchTeachers(String text) throws ClassNotFoundException, SQLException {
        text = "%" + text + "%";
        // Create a Connection
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("SELECT * FROM teacher WHERE full_name LIKE ? OR contact LIKE ? OR address LIKE ?");
        preparedStatement.setString(1,text);
        preparedStatement.setString(2,text);
        preparedStatement.setString(3,text);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Teacher> list = new ArrayList<>();
        while (resultSet.next()){
            list.add(
                    new Teacher(
                            resultSet.getString(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4)
                    )
            );
        }
        return list;
    }

    private boolean deleteTeacher(String id) throws ClassNotFoundException, SQLException {
        // Create a Connection
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("DELETE FROM teacher WHERE teacher_id=?");
        preparedStatement.setString(1,id);
        return preparedStatement.executeUpdate()>0;
    }
    private boolean updateTeacher(Teacher teacher) throws ClassNotFoundException, SQLException {
        // Create a Connection
        Connection connection = DbConnection.getInstance().getConnection();
        PreparedStatement preparedStatement =
                connection.prepareStatement("UPDATE teacher SET full_name=?, contact=?, address=? WHERE teacher_id=?");
        preparedStatement.setString(1,teacher.getName());
        preparedStatement.setObject(2,teacher.getContact());
        preparedStatement.setString(3,teacher.getAddress());
        preparedStatement.setString(4,teacher.getCode());
        return preparedStatement.executeUpdate()>0;
    }

}
