package de.mamakow.dienstplanapotheke.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.mamakow.dienstplanapotheke.model.Branch;

@Dao
public interface BranchDao {
    @Query("SELECT * FROM branch_table WHERE branchId = :id")
    LiveData<Branch> getBranchById(int id);

    @Query("SELECT * FROM branch_table")
    LiveData<List<Branch>> getAllBranches();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBranches(List<Branch> branches);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBranch(Branch branch);

    @Query("DELETE FROM branch_table")
    void clearBranches();
}
