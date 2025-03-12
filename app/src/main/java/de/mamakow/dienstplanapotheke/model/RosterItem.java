package de.mamakow.dienstplanapotheke.model;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.mamakow.dienstplanapotheke.R;

public class RosterItem {
    /**
     * The primary key in the remote database.
     * This is not the same key as in the local Room database.
     */
    private int remotePrimaryKey;
    private LocalDate localDate;
    private int employeeKey;
    private int branchId;
    private String comment;
    private LocalDateTime dutyStartDateTime;
    private LocalDateTime dutyEndDateTime;
    private LocalDateTime breakStartDateTime;
    private LocalDateTime breakEndDateTime;
    private float workingHours;
    /**
     * The date and time the calendar information was last updated.
     */
    private LocalDateTime dtStamp;
    private Status status;

    public String getICalendarLocation(@NotNull Context context) {
        Branch branch = new NetworkOfBranchOffices().getBranchById(branchId);
        if (branch == null) {
            return context.getString(R.string.unknown_branch);
        }
        return branch.getBranchName() + System.lineSeparator() + branch.getBranchAddress();
    }

    public enum Status {
        TENTATIVE, CONFIRMED, CANCELLED;
    }

}