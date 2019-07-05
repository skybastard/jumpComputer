package jumpsoft.jumpdata;

import android.app.ListFragment;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;



public class Logbook extends ListFragment {

    CursorAdapter listAdapter;
    ListView listJumps;

    //public Logbook() {
        // required empty constructor
    //}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_logbook, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        listPopulationFromDB();
        super.onViewCreated(view, savedInstanceState);
    }

    public ListView listPopulationFromDB() {
        try {
            String[] dbColumns = new String[]{"JUMPNO", "JUMPDATE", "EXIT", "DEPLOYMENT", "MAXSPEED", "AVGSPEED", "TIME"}; // list for cursor method
            int[] bindTo = new int[]{R.id.listJumpNo, R.id.listDate, R.id.listExit, R.id.listVavg,}; // list for cursor method
            listJumps = getListView(); // general android listview

            SQLiteOpenHelper logbookDatabaseHelper = new LogBookDatabaseHelper(getActivity().getApplicationContext());
            SQLiteDatabase logbookDB = logbookDatabaseHelper.getWritableDatabase();
            Cursor cursor = logbookDB.query("JUMPS", new String[]{"_id", "JUMPNO", "JUMPDATE", "EXIT",
                    "DEPLOYMENT", "DZ", "MAXSPEED", "AVGSPEED", "TIME"}, null, null, null, null, null);

            listAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(),
                    R.layout.list_item, cursor, dbColumns, bindTo);
            listJumps.setAdapter(listAdapter);
            //TODO put long and shortcklick listeners here to open item from list and delete


        } catch (SQLException e) {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Database unavailable",
                    Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }
        return listJumps;

    }
}
