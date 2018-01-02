package com.citi.model;

/**
 * Created by VALLA on 2018/1/2.
 */
public class PendingLog {

    private String pendingClass;

    private String inuseSec;

    private String startTime;

    private String issueLogFolderName;


    public String getPendingClass() {
        return pendingClass;
    }

    public void setPendingClass(String pendingClass) {
        this.pendingClass = pendingClass;
    }

    public String getInuseSec() {
        return inuseSec;
    }

    public void setInuseSec(String inuseSec) {
        this.inuseSec = inuseSec;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getIssueLogFolderName() {
        return issueLogFolderName;
    }

    public void setIssueLogFolderName(String issueLogFolderName) {
        this.issueLogFolderName = issueLogFolderName;
    }
}
