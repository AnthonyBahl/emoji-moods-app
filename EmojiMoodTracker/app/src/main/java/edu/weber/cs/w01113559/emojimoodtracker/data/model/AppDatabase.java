package edu.weber.cs.w01113559.emojimoodtracker.data.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AppDatabase {

    //region Variables
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final FirebaseUser currentUser = mAuth.getCurrentUser();
    private static final String userID = (currentUser != null) ? currentUser.getUid() : null;
    public static final List<Record> recordList = new ArrayList<>();
    public static Settings userSettings;

    private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();    // General Database Reference
    private static final DatabaseReference mRecordsRef = databaseReference.child("Records").child(Objects.requireNonNull(userID));          // Reference to the records for the user
    private static final DatabaseReference mUserSettingsRef = databaseReference.child("Settings").child(userID);        // Reference to the user settings

    private graphFragInterface mCallback;
    //endregion

    //region Writers
    public static void writeUserSettings(Context context, @NonNull List<String> emojis, List<ReminderData> reminders) {
        userSettings.deleteAlarmsForReminders(context);
        Settings _userSettings = new Settings(emojis, reminders);
        _userSettings.scheduleAlarmsForReminders(context);
        mUserSettingsRef.setValue(_userSettings);
    }
    //endregion

    //region Constructors
    public AppDatabase() {
        userSettingsEventListener();
        addRecordsEventListener();
    }
    //endregion

    //endregion

    //region Event Listeners
    /**
     * Gets record list from database and assigns it to recordList. Listens for updates.
     */
    private void addRecordsEventListener() {
        mRecordsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Record record = snapshot.getValue(Record.class);
                String key = snapshot.getKey();
                if (record != null) { record.setKey(key); }
                recordList.add(record);
                if (mCallback != null) {
                    mCallback.updateChart(recordList);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Record record = snapshot.getValue(Record.class);
                String key = snapshot.getKey();
                if (record != null) { record.setKey(key); }

                // Find old version of record and replace it
                for (Record r : recordList) {
                    if (record != null && r.getKey().equals(record.getKey())) {
                        recordList.set(recordList.indexOf(r), record);
                        break;
                    }
                }
                if (mCallback != null) {
                    mCallback.updateChart(recordList);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Record record = snapshot.getValue(Record.class);
                if (record != null) {
                    record.setKey(snapshot.getKey());
                }

                // Find old version of record and replace it
                for (Record r : recordList) {
                    if (record != null && r.getKey().equals(record.getKey())) {
                        recordList.remove(r);
                        break;
                    }
                }
                if (mCallback != null) {
                    mCallback.updateChart(recordList);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userSettingsEventListener() {
        mUserSettingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userSettings = snapshot.getValue(Settings.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //endregion

    //region Writers
    public static boolean writeRecord(@NonNull String emojiCode) {
        Record record = new Record(emojiCode);
        boolean newFlag = checkForRecentRecord(record);
        mRecordsRef.push().setValue(record);
        return newFlag;
    }

    private static void removeRecord(String key) {
        mRecordsRef.child(key).removeValue();
    }

    public void setInterface(graphFragInterface reference) {
        mCallback = reference;
    }

    /**
     * Clears all Records for the current user.
     */
    public void removeAllRecords() {
        mRecordsRef.removeValue();
    }
    //endregion

    //region Interface
    public interface graphFragInterface {
        void updateChart(@SuppressWarnings("unused") List<Record> records);
    }
    //endregion

    //region Private Functions

    /**
     * Checks to see if there has been a recent {@link Record} created already.
     *
     * @param newRecord {@link Record} new record to be added.
     * @return {@link Boolean} true: there is not a recent record, false: there is a recent record.
     */
    private static boolean checkForRecentRecord(Record newRecord) {
        // ToDo: Add user preference to control this number
        int CREATE_RECORD_TIME_FRAME_MIN = 15;
        for (Record record: recordList) {
            if (TimeUnit.MILLISECONDS.toMinutes(newRecord.getTimestamp() - record.getTimestamp()) <= CREATE_RECORD_TIME_FRAME_MIN) {
                removeRecord(record.getKey());
                return false;
            }
        }
        return true;
    }
    //endregion
}
