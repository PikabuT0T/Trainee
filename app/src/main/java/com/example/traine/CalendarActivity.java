package com.example.traine;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private Map<String, List<Note>> notesMap;
    private NoteAdapter noteAdapter;
    private EditText editText;
    private Button addNoteButton;
    private Calendar selectedDate;
    private List<EventDay> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_layout);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        addNoteButton = findViewById(R.id.btnAddNote);

        notesMap = new HashMap<>();
        noteAdapter = new NoteAdapter(new ArrayList<>());
        events = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(noteAdapter);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                selectedDate = eventDay.getCalendar();
                updateSelectedDate(); // Update calendar when a day is selected
                updateNotesForSelectedDate();

            }
        });

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDate != null && !editText.getText().toString().isEmpty()) {
                    String dateKey = getDateKey(selectedDate);
                    List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
                    Note newNote = new Note(
                            selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH),
                            selectedDate.get(Calendar.DAY_OF_MONTH),
                            editText.getText().toString()
                    );
                    notesForDate.add(newNote);
                    notesMap.put(dateKey, notesForDate);
                    updateSelectedDate(); // Refresh the calendar
                    editText.setText(""); // Clear the EditText
                }
            }
        });

        updateSelectedDate(); // Initial calendar setup
    }

    private String getDateKey(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }
    private void updateSelectedDate() {
        events.clear(); // Очищаем все события
        for (Map.Entry<String, List<Note>> entry : notesMap.entrySet()) {
            Calendar cal = getCalendarFromKey(entry.getKey());
            events.add(new EventDay(cal, R.drawable.baseline_edit_note_24)); // Добавляем иконку для дней с заметками
        }
        if (selectedDate != null) {
            events.add(new EventDay(selectedDate, R.drawable.round_corner)); // Выделяем выбранный день
        }
        calendarView.setEvents(events); // Обновляем календарь
    }
    private void updateNotesForSelectedDate() {
        if (selectedDate != null) {
            String dateKey = getDateKey(selectedDate);
            List<Note> notesForDate = notesMap.getOrDefault(dateKey, new ArrayList<>());
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
}
