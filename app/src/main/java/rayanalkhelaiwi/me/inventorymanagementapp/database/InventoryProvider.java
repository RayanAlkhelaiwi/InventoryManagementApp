package rayanalkhelaiwi.me.inventorymanagementapp.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import rayanalkhelaiwi.me.inventorymanagementapp.R;


/**
 * Created by Rean on 10/27/2017.
 */

public class InventoryProvider extends ContentProvider {

    //Tag for the class name
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    //Constant ID integers for content patterns
    private static final int INVENTORY_TABLE = 100;
    private static final int INVENTORY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY_TABLE);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    private InventoryDbHelper inventoryDbHelper;

    //Initialize the provider and the database helper object.
    @Override
    public boolean onCreate() {

        inventoryDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    //Perform the query for the given URI.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //Get readable database
        SQLiteDatabase database = inventoryDbHelper.getReadableDatabase();

        //This cursor will hold the result of the query
        Cursor cursor;

        //Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_TABLE:

                //Query for the whole table
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                //Query for a single item of the table
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    //Insert new data into the provider with the given ContentValues.
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_TABLE:
                return insertItem(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    //Insert an item into the database with the given content values. Return the new content URI for that specific row in the database.
    private Uri insertItem(Uri uri, ContentValues values) {

        //Check that the name is not null
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
        if (name == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.input_requirement) + "name");
        }

        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException(String.valueOf(R.string.input_requirement) + "quantity");
        }

        Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException(String.valueOf(R.string.input_requirement) + "price");
        }

        SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();
        long id = db.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, String.valueOf(R.string.insert_fail_toast));
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // If the id is fetched, return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    //Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_TABLE:
                return updateItem(uri, contentValues, selection, selectionArgs);

            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
            if (name == null) {
                throw new IllegalArgumentException(String.valueOf(R.string.input_requirement) + "name");
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException(String.valueOf(R.string.input_requirement + "quantity"));
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException(String.valueOf(R.string.input_requirement + "price"));
            }
        }

        //If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }

    //Delete the data at the given selection and selection arguments.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_TABLE:
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                // Delete all rows that match the selection and selection args
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(R.string.deletion_not_supported) + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    //Returns the MIME type of data for the content URI.
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_TABLE:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(String.valueOf(R.string.get_type_unknown_uri) + uri + String.valueOf(R.string.get_type_with_match) + match);
        }
    }
}