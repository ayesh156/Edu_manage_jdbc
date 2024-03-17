package com.developersstack.edumanage.view.tm;

import javafx.scene.control.Button;

import java.util.Date;

public class IntakesTm {
    private String intakeId;
    private String startDate;
    private String intakeName;
    private String programId;
    private String completeState;
    private Button btn;

    public IntakesTm() {
    }

    public IntakesTm(String intakeId, String startDate, String intakeName, String programId, String completeState, Button btn) {
        this.intakeId = intakeId;
        this.startDate = startDate;
        this.intakeName = intakeName;
        this.programId = programId;
        this.completeState = completeState;
        this.btn = btn;
    }

    public String getIntakeId() {
        return intakeId;
    }

    public void setIntakeId(String intakeId) {
        this.intakeId = intakeId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getIntakeName() {
        return intakeName;
    }

    public void setIntakeName(String intakeName) {
        this.intakeName = intakeName;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getCompleteState() {
        return completeState;
    }

    public void setCompleteState(String completeState) {
        this.completeState = completeState;
    }

    public Button getBtn() {
        return btn;
    }

    public void setBtn(Button btn) {
        this.btn = btn;
    }

    @Override
    public String toString() {
        return "IntakesTm{" +
                "intakeId='" + intakeId + '\'' +
                ", startDate='" + startDate + '\'' +
                ", intakeName='" + intakeName + '\'' +
                ", programId='" + programId + '\'' +
                ", completeState='" + completeState + '\'' +
                ", btn=" + btn +
                '}';
    }
}
