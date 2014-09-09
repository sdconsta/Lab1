package edu.syr.mobileos.encryptednotepad;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements
        NoteManipulatorFragment.OnNoteInteractionListener,
        NoteEditFragment.OnDoneClickedListener,
        NoteListFragment.OnNoteCreateListener,
        NoteListFragment.OnNoteClickedListener,
        PasswordDialogFragment.EditPasswordDialogListener
{

    private byte[] mKey;
    private ENDBManager mENDBManagerObject;

    @Override
    protected void onStart() {
        super.onStart();
        PasswordDialogFragment dialogFragment = new PasswordDialogFragment();
        dialogFragment.show(getFragmentManager(), null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (int i = 0; i < mKey.length; i++)
            mKey[i] = 0;
        mENDBManagerObject.closeDatabase();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new TestNotes();
        List<Note> notes = TestNotes.get();

        mENDBManagerObject = new ENDBManager(this);
        mENDBManagerObject.openDatabase();

        /* uncomment to make more notes
        mENDBManagerObject.addNote(notes.get(0));
        mENDBManagerObject.addNote(notes.get(1));
        mENDBManagerObject.addNote(notes.get(2));
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDoneClicked(Note note) {
        long noteId;
        if (!mENDBManagerObject.updateNoteThroughId(note)) {
            noteId = mENDBManagerObject.addNote(note);
        }
        else
        {
            noteId=note.getID();
        }

        Note new_note = getNoteThroughCursor(mENDBManagerObject.getNoteThroughId(noteId));
        getFragmentManager().beginTransaction()
                .replace(R.id.container, NoteDetailFragment.newInstance(new_note))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onNoteClicked(Note note) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, NoteDetailFragment.newInstance(note))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onNoteCreateClicked() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, NoteEditFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onNoteInteraction(int action, Note note) {
        switch (action) {
            case Note.ACTION_DELETE:
                mENDBManagerObject.deleteNote(note.getID());
                ArrayList<Note> notes = new ArrayList<Note>();
                Cursor cursor= mENDBManagerObject.getAllNotes();
                for (long id : getAllNotesIdsFromCursor(cursor))
                    notes.add(getNoteThroughCursor(mENDBManagerObject.getNoteThroughId(id)));
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, NoteListFragment.newInstance(notes))
                        .addToBackStack(null)
                        .commit();
                break;
            case Note.ACTION_EDIT:
                long note_id;
                if (!mENDBManagerObject.updateNoteThroughId(note)) {
                    Log.d("MainActivity", "tried to update a note which doesn't exist in DB");
                }
                else {
                    note_id=note.getID();
                    Note new_note = getNoteThroughCursor(mENDBManagerObject.getNoteThroughId(note_id));
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, NoteEditFragment.newInstance(new_note))
                            .addToBackStack(null)
                            .commit();
                }
                break;
        }
    }

    @Override
    public void onFinishEditDialog(String password) {
        mKey = Crypto.sha256(password);

        ArrayList<Note> notes = new ArrayList<Note>();
        for (long id : getAllNotesIdsFromCursor(mENDBManagerObject.getAllNotes()))
            notes.add(getNoteThroughCursor(mENDBManagerObject.getNoteThroughId(id)));

        getFragmentManager().beginTransaction()
                .add(R.id.container, NoteListFragment.newInstance(notes))
                .commit();
    }

    private Note getNoteThroughCursor(Cursor cursor)
    {
        Note note = new Note();
        long noteId = cursor.getLong(cursor.getColumnIndexOrThrow(ENDBManager.ENOTE_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(ENDBManager.ENOTE_TITLE));
        String contents = cursor.getString(cursor.getColumnIndexOrThrow(ENDBManager.ENOTE_CONTENTS));
        note.setID(noteId);
        note.setTitle(title);
        note.setText(contents);
        return note;
    }
    private ArrayList<Long> getAllNotesIdsFromCursor(Cursor cursor)
    {
        ArrayList<Long> notesIdList = new ArrayList<Long>();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false)
        {
            notesIdList.add(cursor.getLong(cursor.getColumnIndexOrThrow(ENDBManager.ENOTE_ID)));
            cursor.moveToNext();
        }
        return notesIdList;
    }


    // test function, please ignore
    private void testCrypto() {
        // Example demonstrating the Crypto class
        byte[] key = mKey;
        Log.d("debug", "key: " + Crypto.bin2hex(key));
        String plaintext = "123456789101112131415161718192021222324252627282930313233343536";

        byte[] sIV = Crypto.getIV();
        Log.d("debug", "IV: " + sIV);

        Log.d("debug", "plaintext: " + plaintext);
        String ciphertext = Crypto.aes256_enc(key, plaintext,sIV);
        Log.d("debug", "ciphertext: " + ciphertext);

        String plaintext2 = Crypto.aes256_dec(key, ciphertext,sIV);
        Log.d("debug", "plaintext2: " + plaintext2);

        String hmacText = Crypto.hmac_sha256(key, ciphertext);
        Log.d("debug", "hmacText: "+hmacText);
        //Log.d("debug",Crypto)

        // Log.d("debug",Crypto.hm)
    }
}
