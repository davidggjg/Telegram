/*
 * This is the source code of VortexGram for Android.
 *
 * Archived chats: keeps the history of groups and channels you left or were
 * removed from, so it stays readable after the dialog is gone from Telegram.
 */

package com.radolyn.ayugram.database;

import androidx.room.Room;
import com.radolyn.ayugram.database.dao.ArchivedDialogDao;
import com.radolyn.ayugram.database.dao.ArchivedMessageDao;
import org.telegram.messenger.ApplicationLoader;

public class AyuArchiveData {
    public static final String DATABASE_NAME = "ayu-archive";

    private static AyuArchiveDatabase database;
    private static ArchivedDialogDao archivedDialogDao;
    private static ArchivedMessageDao archivedMessageDao;

    static {
        create();
    }

    public static void create() {
        database = Room.databaseBuilder(ApplicationLoader.applicationContext, AyuArchiveDatabase.class, DATABASE_NAME)
                .allowMainThreadQueries()
                .setJournalMode(resolveJournalMode())
                .build();

        archivedDialogDao = database.archivedDialogDao();
        archivedMessageDao = database.archivedMessageDao();
    }

    public static AyuArchiveDatabase getDatabase() {
        return database;
    }

    public static ArchivedDialogDao getArchivedDialogDao() {
        return archivedDialogDao;
    }

    public static ArchivedMessageDao getArchivedMessageDao() {
        return archivedMessageDao;
    }

    public static void clean() {
        database.close();
        ApplicationLoader.applicationContext.deleteDatabase(DATABASE_NAME);
        create();
    }

    /**
     * Reading the setting must not be able to stop the database being built:
     * this runs from a static initialiser, and an exception there would leave
     * the class unusable for the rest of the process.
     */
    private static androidx.room.RoomDatabase.JournalMode resolveJournalMode() {
        try {
            return com.radolyn.ayugram.AyuConfig.WALMode
                    ? androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING
                    : androidx.room.RoomDatabase.JournalMode.TRUNCATE;
        } catch (Throwable ignored) {
            return androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING;
        }
    }
}
