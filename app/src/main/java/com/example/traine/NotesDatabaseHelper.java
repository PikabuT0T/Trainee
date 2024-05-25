package com.example.traine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class NotesDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_MONTH = "month";
    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_TEXT = "text";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_YEAR + " INTEGER, " +
                    COLUMN_MONTH + " INTEGER, " +
                    COLUMN_DAY + " INTEGER, " +
                    COLUMN_TEXT + " TEXT" +
                    ");";

    public NotesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public void createNewTable(){
        SQLiteDatabase db = this.getReadableDatabase();
        onUpgrade(db, 1,1);
    }
    public void addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_YEAR, note.getYear());
        values.put(COLUMN_MONTH, note.getMonth());
        values.put(COLUMN_DAY, note.getDayOfMonth());
        values.put(COLUMN_TEXT, note.getText());

        db.insert(TABLE_NOTES, null, values);
        db.close();
    }

    public List<Note> getNotesForDate(int year, int month, int day) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_ID,
                COLUMN_YEAR,
                COLUMN_MONTH,
                COLUMN_DAY,
                COLUMN_TEXT
        };

        String selection = COLUMN_YEAR + " = ? AND " + COLUMN_MONTH + " = ? AND " + COLUMN_DAY + " = ?";
        String[] selectionArgs = {String.valueOf(year), String.valueOf(month), String.valueOf(day)};

        Cursor cursor = db.query(TABLE_NOTES, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                int noteYear = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR));
                int noteMonth = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MONTH));
                int noteDay = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAY));
                String text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT));

                Note note = new Note(id, noteYear, noteMonth, noteDay, text);
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return notes;
    }

    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_YEAR, note.getYear());
        values.put(COLUMN_MONTH, note.getMonth());
        values.put(COLUMN_DAY, note.getDayOfMonth());
        values.put(COLUMN_TEXT, note.getText());

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(note.getId()) };

        db.update(TABLE_NOTES, values, selection, selectionArgs);
        db.close();
    }

    public void deleteNoteById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.delete(TABLE_NOTES, selection, selectionArgs);
        db.close();
    }

    public interface NotesCallback {
        void onCallback(List<Note> notes);
    }
}

