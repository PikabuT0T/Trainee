package com.example.traine;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Models.User;

public class CalendarActivity extends AppCompatActivity implements NoteAdapter.OnNoteEditListener, NoteAdapter.OnNoteDeleteListener {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private Map<String, List<Note>> notesMap;
    private NoteAdapter noteAdapter;
    private EditText editText;
    private TextView addNoteButton;
    private Calendar selectedDate;
    private List<EventDay> events;
    private NotesDatabaseHelper dbHelper;
    private User currentUser;
    private BackupHelper backupHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_layout);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        addNoteButton = findViewById(R.id.btnAddNote);
        notesMap = new HashMap<>();
        noteAdapter = new NoteAdapter(new ArrayList<>(), this, this);
        events = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(noteAdapter);
        setupListeners();
        dbHelper = new NotesDatabaseHelper(this);
        //backupHelper = new BackupHelper(this);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                selectedDate = eventDay.getCalendar();
                updateSelectedDate();
                updateNotesForSelectedDate();
            }
        });

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDate != null && !editText.getText().toString().isEmpty()) {
                    Note newNote = new Note(
                            0, // id will be set by the database
                            selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH),
                            selectedDate.get(Calendar.DAY_OF_MONTH),
                            editText.getText().toString()
                    );
                    dbHelper.addNote(newNote);

                    String dateKey = getDateKey(selectedDate);
                    List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
                    notesForDate.add(newNote);
                    notesMap.put(dateKey, notesForDate);

                    //updateNotesForSelectedDate();
                    updateSelectedDate();
                    editText.setText("");
                }
            }
        });

        loadNotesFromDatabase();
        updateSelectedDate();
    }

    private void setupListeners() {
        ImageButton buttonToMainActivity = findViewById(R.id.buttonToMainActivity);
        buttonToMainActivity.setOnClickListener(view -> {
            backupHelper.backupDatabaseToFirebase();
            goToActivity(MenuActivity.class);
        });
    }

    private void goToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    private void loadNotesFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("notes", null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
            int month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
            int day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
            String text = cursor.getString(cursor.getColumnIndexOrThrow("text"));

            Note note = new Note(id, year, month, day, text);
            String dateKey = year + "-" + (month + 1) + "-" + day;

            List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
            notesForDate.add(note);
            notesMap.put(dateKey, notesForDate);
        }
        cursor.close();
        db.close();
    }

    private String getDateKey(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void updateSelectedDate() {
        events.clear();
        for (Map.Entry<String, List<Note>> entry : notesMap.entrySet()) {
            Calendar cal = getCalendarFromKey(entry.getKey());
            if (dbHelper.getNotesForDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).size() > 0) {
                events.add(new EventDay(cal, R.drawable.baseline_edit_note_24));
            }
        }
        if (selectedDate != null) {
            events.add(new EventDay(selectedDate, R.drawable.round_corner));
        }
        calendarView.setEvents(events);
    }

    private void updateNotesForSelectedDate() {
        if (selectedDate != null) {
            String dateKey = getDateKey(selectedDate);
            List<Note> notesForDate = dbHelper.getNotesForDate(
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            notesMap.put(dateKey, notesForDate);
            noteAdapter.setNotes(notesForDate);
            noteAdapter.notifyDataSetChanged();
        }
    }

    private Calendar getCalendarFromKey(String key) {
        String[] parts = key.split("-");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
        cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        return cal;
    }

    @Override
    public void onEdit(Note note) {
        // Show a dialog or another activity to edit the note
        // For simplicity, let's use an AlertDialog with an EditText
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Note");

        final EditText input = new EditText(this);
        input.setText(note.getText());
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                note.setText(input.getText().toString());
                dbHelper.updateNote(note);
                updateNotesForSelectedDate();
                updateSelectedDate();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onDelete(Note note) {
        dbHelper.deleteNoteById(note.getId());
        String dateKey = getDateKey(selectedDate);
        List<Note> notesForDate = notesMap.get(dateKey);
        if (notesForDate != null) {
            notesForDate.remove(note);
            notesMap.put(dateKey, notesForDate);
        }
        updateNotesForSelectedDate();
        updateSelectedDate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = getIntent().getParcelableExtra("userDetails");
        Log.d("CalendarActivity", "Current user attached");
        if (currentUser != null) {
            Log.d("Firebase UID", currentUser.getUid());
            backupHelper = new BackupHelper(this, currentUser);  // Ініціалізація BackupHelper з currentUser

            backupHelper.checkDatabaseExists(new BackupHelper.BackupCallback() {
                @Override
                public void onSuccess() {
                    // Database exists, download it
                    backupHelper.downloadDatabaseFromFirebase(new BackupHelper.BackupCallback() {
                        @Override
                        public void onSuccess() {
                            loadNotesFromDatabase();
                            updateSelectedDate();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(CalendarActivity.this, "Failed to download database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure() {
                    // Database does not exist, create a new one
                    Log.d("Firebase", "Database does not exist in storage, creating new one");
                    dbHelper.createNewTable();
                    loadNotesFromDatabase(); // This will create a new database
                    updateSelectedDate();
                }
            });
        } else {
            Log.d("Firebase UID", "currentUser is null");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        currentUser = getIntent().getParcelableExtra("userDetails");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backupHelper.backupDatabaseToFirebase();

    }
}

//public class CalendarActivity extends AppCompatActivity {
//    private CalendarView calendarView;
//    private RecyclerView recyclerView;
//    private Map<String, List<Note>> notesMap;
//    private NoteAdapter noteAdapter;
//    private EditText editText;
//    private Button addNoteButton;
//    private Calendar selectedDate;
//    private List<EventDay> events;
//    private NotesDatabaseHelper dbHelper;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.calendar_layout);
//
//        calendarView = findViewById(R.id.calendarView);
//        recyclerView = findViewById(R.id.recyclerView);
//        editText = findViewById(R.id.editText);
//        addNoteButton = findViewById(R.id.btnAddNote);
//
//        notesMap = new HashMap<>();
//        noteAdapter = new NoteAdapter(new ArrayList<>());
//        events = new ArrayList<>();
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(noteAdapter);
//
//        dbHelper = new NotesDatabaseHelper(this);
//
//        calendarView.setOnDayClickListener(new OnDayClickListener() {
//            @Override
//            public void onDayClick(EventDay eventDay) {
//                selectedDate = eventDay.getCalendar();
//                updateSelectedDate(); // Update calendar when a day is selected
//                updateNotesForSelectedDate();
//            }
//        });
//
//        addNoteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (selectedDate != null && !editText.getText().toString().isEmpty()) {
//                    String dateKey = getDateKey(selectedDate);
//                    List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
//                    Note newNote = new Note(
//                            selectedDate.get(Calendar.YEAR),
//                            selectedDate.get(Calendar.MONTH),
//                            selectedDate.get(Calendar.DAY_OF_MONTH),
//                            editText.getText().toString()
//                    );
//                    notesForDate.add(newNote);
//                    notesMap.put(dateKey, notesForDate);
//
//                    dbHelper.addNote(newNote); // Save note to database
//
//                    updateSelectedDate(); // Refresh the calendar
//                    editText.setText(""); // Clear the EditText
//                }
//            }
//        });
//
//        loadNotesFromDatabase(); // Load notes from database on start
//
//        updateSelectedDate(); // Initial calendar setup
//    }
//
//    private String getDateKey(Calendar calendar) {
//        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
//    }
//
//    private void updateSelectedDate() {
//        events.clear(); // Очищаем все события
//
//        // Перевіряємо кожну дату, чи є записи у базі даних
//        for (Map.Entry<String, List<Note>> entry : notesMap.entrySet()) {
//            Calendar cal = getCalendarFromKey(entry.getKey());
//            if (dbHelper.getNotesForDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).size() > 0) {
//                events.add(new EventDay(cal, R.drawable.baseline_edit_note_24)); // Добавляем иконку для дней с заметками
//            }
//        }
//
//        // Виділяємо вибрану дату
//        if (selectedDate != null) {
//            events.add(new EventDay(selectedDate, R.drawable.round_corner)); // Выделяем выбранный день
//        }
//
//        calendarView.setEvents(events); // Обновляем календарь
//    }
//
//    private void updateNotesForSelectedDate() {
//        if (selectedDate != null) {
//            String dateKey = getDateKey(selectedDate);
//            List<Note> notesForDate = dbHelper.getNotesForDate(
//                    selectedDate.get(Calendar.YEAR),
//                    selectedDate.get(Calendar.MONTH),
//                    selectedDate.get(Calendar.DAY_OF_MONTH)
//            );
//            notesMap.put(dateKey, notesForDate); // Update notesMap
//            noteAdapter.setNotes(notesForDate);
//            noteAdapter.notifyDataSetChanged();
//        }
//    }
//
//    private Calendar getCalendarFromKey(String key) {
//        String[] parts = key.split("-");
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
//        cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
//        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
//        return cal;
//    }
//
//    private void loadNotesFromDatabase() {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        Cursor cursor = db.query(
//                "notes",   // The table to query
//                null,             // The array of columns to return (pass null to get all)
//                null,              // The columns for the WHERE clause
//                null,          // The values for the WHERE clause
//                null,                   // don't group the rows
//                null,                   // don't filter by row groups
//                null               // The sort order
//        );
//
//        while (cursor.moveToNext()) {
//            int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
//            int month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
//            int day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
//            String text = cursor.getString(cursor.getColumnIndexOrThrow("text"));
//
//            Note note = new Note(year, month, day, text);
//            String dateKey = year + "-" + (month + 1) + "-" + day;
//
//            List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
//            notesForDate.add(note);
//            notesMap.put(dateKey, notesForDate);
//        }
//        cursor.close();
//        db.close();
//    }
//}
//    private CalendarView calendarView;
//    private RecyclerView recyclerView;
//    private Map<String, List<Note>> notesMap;
//    private NoteAdapter noteAdapter;
//    private EditText editText;
//    private Button addNoteButton;
//    private Calendar selectedDate;
//    private List<EventDay> events;
//    private NotesDatabaseHelper dbHelper;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.calendar_layout);
//
//        calendarView = findViewById(R.id.calendarView);
//        recyclerView = findViewById(R.id.recyclerView);
//        editText = findViewById(R.id.editText);
//        addNoteButton = findViewById(R.id.btnAddNote);
//
//        notesMap = new HashMap<>();
//        noteAdapter = new NoteAdapter(new ArrayList<>());
//        events = new ArrayList<>();
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(noteAdapter);
//
//        dbHelper = new NotesDatabaseHelper(this);
//
//        calendarView.setOnDayClickListener(new OnDayClickListener() {
//            @Override
//            public void onDayClick(EventDay eventDay) {
//                selectedDate = eventDay.getCalendar();
//                updateSelectedDate(); // Update calendar when a day is selected
//                updateNotesForSelectedDate();
//            }
//        });
//
//        addNoteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (selectedDate != null && !editText.getText().toString().isEmpty()) {
//                    String dateKey = getDateKey(selectedDate);
//                    List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
//                    Note newNote = new Note(
//                            selectedDate.get(Calendar.YEAR),
//                            selectedDate.get(Calendar.MONTH),
//                            selectedDate.get(Calendar.DAY_OF_MONTH),
//                            editText.getText().toString()
//                    );
//                    notesForDate.add(newNote);
//                    notesMap.put(dateKey, notesForDate);
//
//                    dbHelper.addNote(newNote); // Save note to database
//
//                    updateSelectedDate(); // Refresh the calendar
//                    editText.setText(""); // Clear the EditText
//                }
//            }
//        });
//
//        updateSelectedDate(); // Initial calendar setup
//    }
//
//    private String getDateKey(Calendar calendar) {
//        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
//    }
//
//    private void updateSelectedDate() {
//        events.clear(); // Очищаем все события
//        for (Map.Entry<String, List<Note>> entry : notesMap.entrySet()) {
//            Calendar cal = getCalendarFromKey(entry.getKey());
//            events.add(new EventDay(cal, R.drawable.baseline_edit_note_24)); // Добавляем иконку для дней с заметками
//        }
//        if (selectedDate != null) {
//            events.add(new EventDay(selectedDate, R.drawable.round_corner)); // Выделяем выбранный день
//        }
//        calendarView.setEvents(events); // Обновляем календарь
//    }
//
//    private void updateNotesForSelectedDate() {
//        if (selectedDate != null) {
//            String dateKey = getDateKey(selectedDate);
//            List<Note> notesForDate = dbHelper.getNotesForDate(
//                    selectedDate.get(Calendar.YEAR),
//                    selectedDate.get(Calendar.MONTH),
//                    selectedDate.get(Calendar.DAY_OF_MONTH)
//            );
//            notesMap.put(dateKey, notesForDate); // Update notesMap
//            noteAdapter.setNotes(notesForDate);
//            noteAdapter.notifyDataSetChanged();
//        }
//    }
//
//    private Calendar getCalendarFromKey(String key) {
//        String[] parts = key.split("-");
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
//        cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
//        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
//        return cal;
//    }


//    private CalendarView calendarView;
//    private RecyclerView recyclerView;
//    private Map<String, List<Note>> notesMap;
//    private NoteAdapter noteAdapter;
//    private EditText editText;
//    private Button addNoteButton;
//    private Calendar selectedDate;
//    private List<EventDay> events;
//    private SQLiteDatabase db;
//    private CalendarDatabaseHelper dbHelper;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.calendar_layout);
//
//        dbHelper = new CalendarDatabaseHelper(this);
//        //db = dbHelper.getWritableDatabase();
//        //dbHelper.onCreate(db);
//        //dbHelper.onUpgrade(db, 1, 1);
//
//        calendarView = findViewById(R.id.calendarView);
//        recyclerView = findViewById(R.id.recyclerView);
//        editText = findViewById(R.id.editText);
//        addNoteButton = findViewById(R.id.btnAddNote);
//
//        notesMap = new HashMap<>();
//        noteAdapter = new NoteAdapter(new ArrayList<>());
//        events = new ArrayList<>();
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(noteAdapter);
//
//        calendarView.setOnDayClickListener(new OnDayClickListener() {
//            @Override
//            public void onDayClick(EventDay eventDay) {
//                selectedDate = eventDay.getCalendar();
//                updateSelectedDate(); // Update calendar when a day is selected
//                updateNotesForSelectedDate();
//
//            }
//        });
//
//        addNoteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (selectedDate != null && !editText.getText().toString().isEmpty()) {
//                    String dateKey = getDateKey(selectedDate);
//                    List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
//                    Note newNote = new Note(
//                            selectedDate.get(Calendar.YEAR),
//                            selectedDate.get(Calendar.MONTH),
//                            selectedDate.get(Calendar.DAY_OF_MONTH),
//                            editText.getText().toString()
//                    );
//                    notesForDate.add(newNote);
//                    notesMap.put(dateKey, notesForDate);
//
//                    dbHelper.addNote(selectedDate.get(Calendar.YEAR),
//                            selectedDate.get(Calendar.MONTH),
//                            selectedDate.get(Calendar.DAY_OF_MONTH),
//                            editText.getText().toString());
//                    updateNotesForSelectedDate();
//                    updateSelectedDate();
//                    editText.setText(""); // Clear the EditText
//                }
////                if (selectedDate != null && !editText.getText().toString().isEmpty()) {
////                    String dateKey = getDateKey(selectedDate);
////                    List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
////                    Note newNote = new Note(
////                            selectedDate.get(Calendar.YEAR),
////                            selectedDate.get(Calendar.MONTH),
////                            selectedDate.get(Calendar.DAY_OF_MONTH),
////                            editText.getText().toString()
////                    );
////                    notesForDate.add(newNote);
////                    notesMap.put(dateKey, notesForDate);
////                    updateSelectedDate(); // Refresh the calendar
////                    editText.setText(""); // Clear the EditText
//                }
//        });
//
//        updateSelectedDate(); // Initial calendar setup
//    }
//
//    private String getDateKey(Calendar calendar) {
//        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
//    }
//
//    private void updateSelectedDate() {
//        events.clear(); // Очищаем все события
//        List<Note> allNotes = dbHelper.getAllNotes(); // Предполагается, что этот метод возвращает все заметки из базы данных
//        Set<String> uniqueDates = new HashSet<>();
//
//        for (Note note : allNotes) {
//            String key = getDateKey(note.getCalendar()); // Получаем ключ даты из объекта Note
//            if (!uniqueDates.contains(key)) {
//                uniqueDates.add(key);
//                Calendar cal = getCalendarFromKey(key);
//                events.add(new EventDay(cal, R.drawable.baseline_edit_note_24)); // Добавляем иконку для дней с заметками
//            }
//        }
//        if (selectedDate != null) {
//            events.add(new EventDay(selectedDate, R.drawable.round_corner)); // Выделяем выбранный день
//        }
//        calendarView.setEvents(events); // Обновляем календарь
//    }
//
//    private void updateNotesForSelectedDate() {
//        if (selectedDate != null) {
//            List<Note> notesForDate = dbHelper.getNotesForDate(
//                    selectedDate.get(Calendar.YEAR),
//                    selectedDate.get(Calendar.MONTH),
//                    selectedDate.get(Calendar.DAY_OF_MONTH)
//            );
//            noteAdapter.setNotes(notesForDate);
//            noteAdapter.notifyDataSetChanged();
//        }
//    }
//
//
////    private void updateSelectedDate() {
////        events.clear(); // Очищаем все события
////        for (Map.Entry<String, List<Note>> entry : notesMap.entrySet()) {
////            Calendar cal = getCalendarFromKey(entry.getKey());
////            events.add(new EventDay(cal, R.drawable.baseline_edit_note_24)); // Добавляем иконку для дней с заметками
////        }
////        if (selectedDate != null) {
////            events.add(new EventDay(selectedDate, R.drawable.round_corner)); // Выделяем выбранный день
////        }
////        calendarView.setEvents(events); // Обновляем календарь
////    }
////    private void updateNotesForSelectedDate() {
////        if (selectedDate != null) {
////            String dateKey = getDateKey(selectedDate);
////            List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
////            noteAdapter.setNotes(notesForDate);
////            noteAdapter.notifyDataSetChanged();
////        }
////    }
//
//
//    private Calendar getCalendarFromKey(String key) {
//        String[] parts = key.split("-");
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
//        cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
//        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
//        return cal;
//    }

