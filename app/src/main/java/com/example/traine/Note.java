package com.example.traine;

import com.applandeo.materialcalendarview.EventDay;

import java.util.Calendar;

public class Note {
    private int id;
    private int year;
    private int month;
    private int dayOfMonth;
    private String text;

    public Note(int id, int year, int month, int dayOfMonth, String text) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.text = text;
    }

    public Note(int year, int month, int dayOfMonth, String text) {
        this(0, year, month, dayOfMonth, text); // використання конструктора з id за замовчуванням
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Удален конструктор по умолчанию для чистоты кода

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    public Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return calendar;
    }

    @Override
    public String toString() {
        return "Note{" +
                "year=" + year +
                ", month=" + month +
                ", dayOfMonth=" + dayOfMonth +
                ", text='" + text + '\'' +
                '}';
    }
}
