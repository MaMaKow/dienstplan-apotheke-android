package de.mamakow.dienstplanapotheke.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

@Entity(tableName = "branch_table")
public class Branch {
    @PrimaryKey
    @SerializedName("branch_id")
    private final int branchId;

    @SerializedName("PEP")
    private final int branchPepId;

    @SerializedName("name")
    private final String branchName;

    @SerializedName("short_name")
    private final String branchShortName;

    @SerializedName("address")
    private final String branchAddress;

    @SerializedName("manager")
    private final String branchManager;

    @TypeConverters(Converters.class)
    @SerializedName("Opening_times")
    private final HashMap<Integer, OpeningHours> openingTimesMap;

    public Branch(int branchId,
                  int branchPepId,
                  String branchName,
                  String branchShortName,
                  String branchAddress,
                  String branchManager,
                  HashMap<Integer, OpeningHours> openingTimesMap) {
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

    public HashMap<Integer, OpeningHours> getOpeningTimesMap() {
        return openingTimesMap;
    }

    @NonNull
    @Override
    public String toString() {
        return "Branch{" +
                "id=" + branchId +
                ", name='" + branchName + '\'' +
                '}';
    }
}
