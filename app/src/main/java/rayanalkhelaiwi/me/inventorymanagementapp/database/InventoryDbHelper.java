package rayanalkhelaiwi.me.inventorymanagementapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rean on 10/27/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    /* It does the command:
       CREATE TABLE inventory (_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                name TEXT NOT NULL,
                                quantity INTEGER NOT NULL DEFAULT 0,
                                price INTEGER NOT NULL DEFAULT 0,
                                image  TEXT NOT NULL);
                                */
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            InventoryContract.InventoryEntry.TABLE_NAME + " (" +
            InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME + " TEXT NOT NULL, " +
            InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
            InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
            InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE + " TEXT NOT NULL);";


    //Delete the table
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + InventoryContract.InventoryEntry.TABLE_NAME;

    //Version and names of the table
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "store.db";

    //Constructor
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //To initialize the table if it doesn't exist
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    //To upgrade the table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}