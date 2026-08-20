package de.mamakow.dienstplanapotheke;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;

import de.mamakow.dienstplanapotheke.database.AppDatabase;
import de.mamakow.dienstplanapotheke.database.BranchDao;
import de.mamakow.dienstplanapotheke.model.Branch;

@RunWith(AndroidJUnit4.class)
public class BranchDatabaseTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private BranchDao branchDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        branchDao = db.branchDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetBranch() throws Exception {
        Branch branch = new Branch(1, 101, "Test Apotheke", "TA", "Strasse 1", "Chef", new HashMap<>());

        branchDao.insertBranch(branch);

        Branch retrieved = LiveDataTestUtil.getOrAwaitValue(branchDao.getBranchById(1));
        assertNotNull(retrieved);
        assertEquals("Test Apotheke", retrieved.getBranchName());
    }

    @Test
    public void getAllBranches() throws Exception {
        Branch b1 = new Branch(1, 101, "Apo 1", "A1", "Adr 1", "Chef 1", new HashMap<>());
        Branch b2 = new Branch(2, 102, "Apo 2", "A2", "Adr 2", "Chef 2", new HashMap<>());

        branchDao.insertBranch(b1);
        branchDao.insertBranch(b2);

        List<Branch> all = LiveDataTestUtil.getOrAwaitValue(branchDao.getAllBranches());
        assertEquals(2, all.size());
    }
}
