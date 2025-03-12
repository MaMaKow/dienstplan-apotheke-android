package de.mamakow.dienstplanapotheke.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import de.mamakow.dienstplanapotheke.model.RosterEntry;

@Database(entities = {RosterEntry.class}, version = 1)
public abstract class RosterDatabase extends RoomDatabase {

    private static volatile RosterDatabase INSTANCE;

    public static RosterDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RosterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RosterDatabase.class, "roster_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract RosterDao rosterDao();
}