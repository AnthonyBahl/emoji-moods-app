package edu.weber.cs.w01113559.emojimoodtracker.data.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class Record {

    private Long timestamp;
    private String emojiCode;
    private String key;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEmojiCode() {
        return emojiCode;
    }

    public void setEmojiCode(String emojiCode) {
        this.emojiCode = emojiCode;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Exclude
    public Date getDate() {
        return new Date(timestamp);
    }

    public Record() {
        // Required default constructor
    }

    public Record(Drawable emoji, Context context) {
        this.timestamp = System.currentTimeMillis();
        this.emojiCode = emojiEncoding.decodeEmoji(emoji, context);
    }

    public Record(String emojiCode) {
        this.timestamp = System.currentTimeMillis();
        this.emojiCode = emojiCode;
    }
}
