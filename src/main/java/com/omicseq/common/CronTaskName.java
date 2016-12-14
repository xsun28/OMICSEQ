package com.omicseq.common;

public enum CronTaskName {
    ROBOT_SRA_PARSER("rebot.sra.parser"), ;

    private String task;

    CronTaskName(String task) {
        this.task = task;
    }

    public String task() {
        return task;
    }
}
