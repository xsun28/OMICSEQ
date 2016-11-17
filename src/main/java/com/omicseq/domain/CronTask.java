package com.omicseq.domain;

public class CronTask extends BaseDomain {
    private static final long serialVersionUID = 1L;
    private String name;
    private String launchServer;
    private String cron;
    private String runtime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLaunchServer() {
        return launchServer;
    }

    public void setLaunchServer(String launchServer) {
        this.launchServer = launchServer;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }
}
