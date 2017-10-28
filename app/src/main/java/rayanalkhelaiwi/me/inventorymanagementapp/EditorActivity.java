package rayanalkhelaiwi.me.inventorymanagementapp;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import rayanalkhelaiwi.me.inventorymanagementapp.database.InventoryContract;
import rayanalkhelaiwi.me.inventorymanagementapp.database.InventoryDbHelper;

/**
 * Created by Rean on 10/27/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;
    private static final int READ_EXTERNAL_STORAGE = 100;
    private static final int REQUESTED_IMAGE = 1000;
    private static String img = "none";
    Context context;
    Uri itemImage;
    InventoryDbHelper inventoryDbHelper;
    private Uri currentSingleUri;
    private boolean itemHasChanged = false;
    private long rowID;
    /**
     * EditText field to enter the item (product))'s name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the item (product))'s quantity
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the item (product))'s price
     */
    private EditText mPriceEditText;
    /**
     * Button to insert the item (product))'s image from the device
     */
    private Button mInsertButton;
    /**
     * ImageView field to add the item (product))'s quantity
     */
    private ImageView quantityAddition;
    /**
     * ImageView field to subtract the item (product))'s quantity
     */
    private ImageView quantitySubtraction;

    private ImageView imageView;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        context = this;

        Intent intent = getIntent();
        currentSingleUri = intent.getData();

        Log.v("URI", String.valueOf(currentSingleUri));

        if (currentSingleUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_item));

            //Invalidate the options menu, so the "Delete" menu option can be hidden. (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
        }

        inventoryDbHelper = new InventoryDbHelper(this);

        //Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mInsertButton = (Button) findViewById(R.id.insert_button);
        imageView = (ImageView) findViewById(R.id.inserted_image);
        quantityAddition = (ImageView) findViewById(R.id.edit_addition);
        quantitySubtraction = (ImageView) findViewById(R.id.edit_subtraction);


        mNameEditText.setOnTouchListener(touchListener);
        mQuantityEditText.setOnTouchListener(touchListener);
        mPriceEditText.setOnTouchListener(touchListener);
        imageView.setOnTouchListener(touchListener);
        quantityAddition.setOnTouchListener(touchListener);
        quantitySubtraction.setOnTouchListener(touchListener);

        mInsertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewImage(v);
            }
        });

        quantityAddition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mQuantityEditText.getText().toString();

                int quantityNum;

                if (quantityString.isEmpty()) {
                    quantityNum = 0;

                } else {

                    quantityNum = Integer.parseInt(quantityString);
                }

                int addedNum = quantityNum + 1;
                mQuantityEditText.setText("" + addedNum);
            }
        });

        quantitySubtraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mQuantityEditText.getText().toString();

                int quantityNum;

                if (quantityString.isEmpty()) {

                } else if (quantityString.equals("0")) {

                } else if (quantityString.equals("1")) {

                    Toast.makeText(context, "Insert at least 1 quantity", Toast.LENGTH_SHORT).show();
                } else {
                    quantityNum = Integer.parseInt(quantityString);
                    int subtractedNum = quantityNum - 1;
                    mQuantityEditText.setText("" + subtractedNum);
                }
            }
        });
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        //Create an AlertDialog.Builder and set the message, and click listeners for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //User clicked the "Keep editing" button, so dismiss the dialog and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveItem() {

        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        if (currentSingleUri == null) {
            if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString)) {
                Toast.makeText(this, "Missing fields", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE, itemImage.toString());

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);

        if (currentSingleUri == null) {
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.insert_fail_toast), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_successful_toast), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentSingleUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.save_item_update_fail), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.save_item_update_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);

        if (currentSingleUri == null) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            MenuItem orderMoreItem = menu.findItem(R.id.action_order_more);

            deleteItem.setVisible(false);
            orderMoreItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (currentSingleUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                //Save the entered data by the user to the database
                saveItem();

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                // Respond to a click on the "Up" arrow button in the app bar
                return true;

            case R.id.action_order_more:

                showOrderMoreConfirmationDialog();
                return true;

            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
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

        return new CursorLoader(this, currentSingleUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Cursor cursor = getContentResolver().query(currentSingleUri, null, null, null, null);

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);

            img = cursor.getString(imageColumnIndex);
            itemImage = Uri.parse(img);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));

            Glide.with(this).load(itemImage).centerCrop().into(imageView);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showOrderMoreConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to order more?");
        builder.setPositiveButton("ORDER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String name = mNameEditText.getText().toString().trim();
                String quantity = mQuantityEditText.getText().toString().trim();
                String price = mPriceEditText.getText().toString().trim();
                String orderMessage = "Send " + quantity + " more of " + name + '\n' + "With a price tag of: $" + price;

                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"rean.alklwiy@gmail.com"});
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_subject_mail_intent) + name);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, orderMessage);

                startActivity(intent);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {

        // Only perform the delete if this is an existing item.
        if (currentSingleUri != null) {
            int rowsDeleted = getContentResolver().delete(currentSingleUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void studioIntent() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent, "Select an Image"), REQUESTED_IMAGE);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_EXTERNAL_STORAGE) {
            if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                studioIntent();
            }
        }
    }

    public void viewImage(View v) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            return;
        }

        studioIntent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == REQUESTED_IMAGE && resultCode == Activity.RESULT_OK) {

            itemImage = resultData.getData();
            if (resultData != null) {

                Glide.with(this).load(itemImage).crossFade().centerCrop().into(imageView);
            }

        } else if (requestCode == REQUESTED_IMAGE) {

            Toast.makeText(this, "Not Available", Toast.LENGTH_SHORT).show();
        }
    }
}