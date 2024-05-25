package com.example.traine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private OnNoteEditListener onNoteEditListener;
    private OnNoteDeleteListener onNoteDeleteListener;

    public NoteAdapter(List<Note> notes, OnNoteEditListener onNoteEditListener, OnNoteDeleteListener onNoteDeleteListener) {
        this.notes = notes;
        this.onNoteEditListener = onNoteEditListener;
        this.onNoteDeleteListener = onNoteDeleteListener;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.noteText.setText(note.getText());

        holder.noteDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNoteDeleteListener.onDelete(note);
            }
        });

        holder.noteEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNoteEditListener.onEdit(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<Note> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public TextView noteText;
        public ImageButton noteDelete, noteEdit;

        public NoteViewHolder(View itemView) {
            super(itemView);
            noteText = itemView.findViewById(R.id.note_title);
            noteDelete = itemView.findViewById(R.id.button_delete);
            noteEdit = itemView.findViewById(R.id.button_edit);
        }
    }

    public interface OnNoteEditListener {
        void onEdit(Note note);
    }

    public interface OnNoteDeleteListener {
        void onDelete(Note note);
    }
}

//public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
//    private List<Note> notes;
//
//    public NoteAdapter(List<Note> notes) {
//        this.notes = notes;
//    }
//
//    @Override
//    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_item, parent, false);
//        return new NoteViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(NoteViewHolder holder, int position) {
//        Note note = notes.get(position);
//        holder.noteText.setText(note.getText());
//        // Здесь можно добавить другие поля или методы для установки данных в представлениях
//    }
//
//    @Override
//    public int getItemCount() {
//        return notes.size();
//    }
//
//    public void setNotes(List<Note> newNotes) {
//        this.notes = newNotes;
//        notifyDataSetChanged(); // Уведомляем адаптер о том, что данные изменились
//    }
//
//    public static class NoteViewHolder extends RecyclerView.ViewHolder {
//        public TextView noteText;
//
//        public ImageButton noteDelete, noteEdit;
//
//        public NoteViewHolder(View itemView) {
//            super(itemView);
//
//            noteText = itemView.findViewById(R.id.note_title);
//            // Убедитесь, что у вас есть TextView с таким ID в вашем layout
//            noteDelete = itemView.findViewById(R.id.button_delete);
//            noteEdit = itemView.findViewById(R.id.button_edit);
//
//        }
//    }
//}
