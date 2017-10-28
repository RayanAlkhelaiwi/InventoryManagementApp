package rayanalkhelaiwi.me.inventorymanagementapp.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Rean on 10/27/2017.
 */

public final class InventoryContract {

    //Creating the content uri
    public static final String CONTENT_AUTHORITY = "rayanalkhelaiwi.me.inventorymanagementapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";


    //Create a constructor that is empty to prevent creating one.
    private InventoryContract() {
    }

    public static final class InventoryEntry implements BaseColumns {

        //Name of the table
        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INVENTORY_NAME = "name";
        public static final String COLUMN_INVENTORY_QUANTITY = "quantity";
        public static final String COLUMN_INVENTORY_PRICE = "price";
        public static final String COLUMN_INVENTORY_IMAGE = "image";

        //The MIME type of the CONTENT_URI for the table
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //The MIME type of the CONTENT_URI for a single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //Appending the path to the content uri inside the entry
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

    }
}

