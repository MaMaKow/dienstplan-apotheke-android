package de.mamakow.dienstplanapotheke.model;

import java.util.HashMap;

public class Branch {
    private final int branchId;
    private final int branchPepId;
    private final String branchName;
    private final String branchShortName;
    private final String branchAddress;
    private final String branchManager;
    private final HashMap<Integer, String[]> openingTimesMap;

    public Branch(int branchId,
                  int branchPepId,
                  String branchName,
                  String branchShortName,
                  String branchAddress,
                  String branchManager,
                  HashMap<Integer, String[]> openingTimesMap) {
        this.branchId = branchId;
        this.branchPepId = branchPepId;
        this.branchName = branchName;
        this.branchShortName = branchShortName;
        this.branchAddress = branchAddress;
        this.branchManager = branchManager;
        this.openingTimesMap = openingTimesMap;
    }

    public int getBranchId() {
        return branchId;
    }

    public int getBranchPepId() {
        return branchPepId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getBranchShortName() {
        return branchShortName;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public String getBranchManager() {
        return branchManager;
    }

    public HashMap<Integer, String[]> getOpeningTimesMap() {
        return openingTimesMap;
    }

}
