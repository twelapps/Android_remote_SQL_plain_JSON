package com.example.twelker.reminderdemo;

import android.provider.BaseColumns;

/**
 * Created by twelh on 10/2/17.
 */

public class RemindersContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private RemindersContract() {}

    /* Inner class that defines the table contents */
    public static class ReminderEntry implements BaseColumns {
        public static final String TABLE_NAME = "Reminders";
        public static final String COLUMN_NAME_REMINDER = "reminder";

    }

}
