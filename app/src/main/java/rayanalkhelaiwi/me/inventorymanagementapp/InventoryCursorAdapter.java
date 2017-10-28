package rayanalkhelaiwi.me.inventorymanagementapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import rayanalkhelaiwi.me.inventorymanagementapp.database.InventoryContract;

import static rayanalkhelaiwi.me.inventorymanagementapp.R.id.quantity;

/**
 * Created by Rean on 10/26/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    DisplayActivity displayActivity;

    //Constructor
    public InventoryCursorAdapter(DisplayActivity context, Cursor c) {
        super(context, c, 0 /* flags */);
        this.displayActivity = context;
    }

    //New View
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //return the list item view inflater
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //Bind View
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(quantity);
        ImageView orderImageView = (ImageView) view.findViewById(R.id.order_button);
        final ImageView imageView = (ImageView) view.findViewById(R.id.viewer);

        final long inventoryID = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));
        String inventoryName = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME));
        int inventoryPrice = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE));
        final int inventoryQuantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY));

        final Uri inventoryImage = Uri.parse(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE)));

        nameTextView.setText(inventoryName);
        quantityTextView.setText("" + inventoryQuantity);
        priceTextView.setText("" + inventoryPrice);
        Glide.with(context).load(inventoryImage).centerCrop().into(imageView);

        orderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, inventoryID);
                ContentValues values = new ContentValues();
                if (inventoryQuantity > 0) {
                    int quantityNum = inventoryQuantity - 1;
                    values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantityNum);

                    view.getContext().getContentResolver().update(uri, values, null, null);
                }
            }
        });
    }
}
