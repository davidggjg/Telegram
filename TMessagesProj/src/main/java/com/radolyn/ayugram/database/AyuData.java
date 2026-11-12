/*
 * This is the source code of VortexGram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @Radolyn, 2023
 */

package com.radolyn.ayugram.database;

import androidx.room.Room;
import com.radolyn.ayugram.AyuConstants;
import com.radolyn.ayugram.database.dao.DeletedMessageDao;
import com.radolyn.ayugram.database.dao.EditedMessageDao;
import org.telegram.messenger.ApplicationLoader;

public class AyuData {
    private static AyuDatabase database;
    private static EditedMessageDao editedMessageDao;
    private static DeletedMessageDao deletedMessageDao;

    static {
        create();
    }

    public static void create() {
        database = Room.databaseBuilder(ApplicationLoader.applicationContext, AyuDatabase.class, AyuConstants.AYU_DATABASE)
                .allowMainThreadQueries()
                // The WAL switch had nothing reading it, so the setting did nothing.
                .setJournalMode(com.radolyn.ayugram.AyuConfig.WALMode
                        ? androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING
                        : androidx.room.RoomDatabase.JournalMode.TRUNCATE)
                .fallbackToDestructiveMigration()
                .build();

        editedMessageDao = database.editedMessageDao();
        deletedMessageDao = database.deletedMessageDao();
    }

    public static AyuDatabase getDatabase() {
        return database;
    }

    public static EditedMessageDao getEditedMessageDao() {
        return editedMessageDao;
    }

    public static DeletedMessageDao getDeletedMessageDao() {
        return deletedMessageDao;
    }

    public static void clean() {
        database.close();

        ApplicationLoader.applicationContext.deleteDatabase(AyuConstants.AYU_DATABASE);

        create();
    }
}
