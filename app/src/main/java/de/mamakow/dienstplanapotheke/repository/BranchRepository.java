package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.annotation.NonNull;
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

    public LiveData<List<Branch>> getAllBranches() {
        return branchDao.getAllBranches();
    }

    public void fetchAndSaveBranches(RetrofitNetworkHandler.NetworkResponseCallback<Void> callback) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            sessionManager.performLogin();
            if (callback != null) callback.onError("Nicht angemeldet.");
            return;
        }

        networkHandler.fetchBranches(token, new RetrofitNetworkHandler.NetworkResponseCallback<List<Branch>>() {
            @Override
            public void onSuccess(@NonNull List<Branch> branches) {
                executor.execute(() -> {
                    // Smart Sync for Branches: Use REPLACE. 
                    // Removal of clearBranches() to prevent UI flickering and maintain offline data.
                    branchDao.insertBranches(branches);
                    if (callback != null) callback.onSuccess(null);
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Network error: " + errorMessage);
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }
}
