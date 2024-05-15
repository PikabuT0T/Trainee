package com.example.traine;

import com.applandeo.materialcalendarview.EventDay;

public class Note {
    private int year;
    private int month;
    private int dayOfMonth;
    private String text;

    public Note(int year, int month, int dayOfMonth, String text) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.text = text;
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
