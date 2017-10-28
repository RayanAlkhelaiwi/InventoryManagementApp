package rayanalkhelaiwi.me.inventorymanagementapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import rayanalkhelaiwi.me.inventorymanagementapp.database.InventoryContract;

//Displays list of items that were entered and stored in the app.
public class DisplayActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;
    InventoryCursorAdapter inventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        //Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DisplayActivity.this, EditorActivity.class);
                Uri uri = null;
                intent.setData(uri);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(inventoryCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DisplayActivity.this, EditorActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                intent.setData(currentItemUri);

                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    private void insertDummyData() {

        ContentValues contentValues = new ContentValues();

        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME, "Fake Product");
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, 3);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE, 25);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE, "android.resource://" + getPackageName() + "/drawable/ic_empty_image");

        //Insert a new row for the fake item into the provider using the ContentResolver.
        Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, contentValues);

        Toast.makeText(this, getString(R.string.insert_successful_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            //Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            //Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE
        };

        return new CursorLoader(this, InventoryContract.InventoryEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        inventoryCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inventoryCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all items in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from inventory database");
    }
}