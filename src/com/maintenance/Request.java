package com.maintenance;

public class Request implements Comparable<Request> {

    private int id;
    private String tenantName;
    private String address; // <--- NEW FIELD
    private String issue;
    private String category;
    private int severity;
    private String status;

    public Request(int id, String tenantName, String address, String issue, String category, int severity, String status) {
        this.id = id;
        this.tenantName = tenantName;
        this.address = address;
        this.issue = issue;
        this.category = category;
        this.severity = severity;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getTenantName() { return tenantName; }
    public String getAddress() { return address; }
    public String getIssue() { return issue; }
    public String getCategory() { return category; }
    public int getSeverity() { return severity; }
    public String getStatus() { return status; }

    // DSA Logic: Sort by Severity (High to Low)
    // This is the "Brain" of the Priority Queue
    @Override
    public int compareTo(Request other) {
        return Integer.compare(other.severity, this.severity);
    }

    @Override
    public String toString() {
        return "ID:" + id + " | " + tenantName + " (" + address + "): " + issue;
    }
}