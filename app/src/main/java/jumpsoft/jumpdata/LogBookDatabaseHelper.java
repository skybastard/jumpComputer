package jumpsoft.jumpdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;


public class LogBookDatabaseHelper extends SQLiteOpenHelper {


    public LogBookDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE JUMPS (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "JUMPNO TEXT , "
                + "JUMPDATE TEXT, "
                + "EXIT TEXT, "
                + "MAXSPEED TEXT, "
                + "AVGSPEED TEXT, "
                + "DEPLOYMENT TEXT, "
                + "DZ TEXT, "
                + "PLANE TEXT, "
                + "TIME TEXT, "
                + "GEAR TEXT, "
                + "TOTALTIME TEXT, "
                + "COMMENTS TEXT);");
        Log.d("baas", "Table is created");


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
