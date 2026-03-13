package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mamakow.dienstplanapotheke.database.BranchDao;
import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class BranchRepository {
    private static final String TAG = "BranchRepository";
    private final BranchDao branchDao;
    private final RetrofitNetworkHandler networkHandler;
    private final SessionManager sessionManager;
    private final Executor executor;

    public BranchRepository(BranchDao branchDao, RetrofitNetworkHandler networkHandler, SessionManager sessionManager) {
        this.branchDao = branchDao;
        this.networkHandler = networkHandler;
        this.sessionManager = sessionManager;
        this.executor = Executors.newSingleThreadExecutor();
    }

    private void handleNetworkError(String errorMessage) {
        Log.e(TAG, "Network error: " + errorMessage);
        if (errorMessage != null && (errorMessage.contains("Invalid token") || errorMessage.contains("Token expired"))) {
            Log.i(TAG, "Token issue detected. Triggering re-login.");
            sessionManager.logout();
            sessionManager.performLogin();
        }
    }

    public LiveData<List<Branch>> getAllBranches() {
        return branchDao.getAllBranches();
    }

    public LiveData<Branch> getBranchById(int id) {
        return branchDao.getBranchById(id);
    }

    public void fetchAndSaveBranches() {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            Log.e(TAG, "Token is null, cannot fetch branches.");
            sessionManager.performLogin();
            return;
        }

        networkHandler.fetchBranches(token, new RetrofitNetworkHandler.NetworkResponseCallback<List<Branch>>() {
            @Override
            public void onSuccess(List<Branch> branches) {
                executor.execute(() -> {
                    branchDao.clearBranches();
                    branchDao.insertBranches(branches);
                    Log.d(TAG, "Branches saved to database: " + branches.size());
                });
            }

            @Override
            public void onError(String errorMessage) {
                handleNetworkError(errorMessage);
            }
        });
    }

    public void fetchAndSaveBranchById(int branchId) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            Log.e(TAG, "Token is null, cannot fetch branch.");
            sessionManager.performLogin();
            return;
        }

        networkHandler.fetchBranchById(token, branchId, new RetrofitNetworkHandler.NetworkResponseCallback<Branch>() {
            @Override
            public void onSuccess(Branch branch) {
                executor.execute(() -> {
                    branchDao.insertBranch(branch);
                    Log.d(TAG, "Branch " + branchId + " saved to database.");
                });
            }

            @Override
            public void onError(String errorMessage) {
                handleNetworkError(errorMessage);
            }
        });
    }
}
