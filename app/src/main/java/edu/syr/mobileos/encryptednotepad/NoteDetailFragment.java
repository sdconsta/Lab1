package edu.syr.mobileos.encryptednotepad;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment which displays the details for a given note. These include the note's title,
 * decrypted text, and (optionally) the date of creation.
 *
 * There are two action bar buttons: one for editing the current note, and one for deleting
 * it. These two buttons trigger callbacks to the MainActivity via the interface defined in
 * NoteManipulatorFragment.
 */
public class NoteDetailFragment extends NoteManipulatorFragment {

    private static final String ARG_NOTE =
            "edu.syr.mobileos.encryptednotepad.NoteDetailFragment.note";

    private Note mNote;

    public static NoteDetailFragment newInstance(Note note) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTE, note);
        fragment.setArguments(args);
        return fragment;
    }

    public NoteDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNote = (Note) getArguments().getSerializable(ARG_NOTE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note_detail, container, false);
    }

}