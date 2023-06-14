package fr.upjv.Utils;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;

public class SerializableTimestamp implements Serializable {
    private long seconds;
    private int nanoseconds;

    public SerializableTimestamp() {
        // Constructeur vide requis pour la désérialisation
    }

    public SerializableTimestamp(Timestamp timestamp) {
        this.seconds = timestamp.getSeconds();
        this.nanoseconds = timestamp.getNanoseconds();
    }

    public Timestamp toTimestamp() {
        return new Timestamp(seconds, nanoseconds);
    }

    public Date toDate() {
        return toTimestamp().toDate();
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public int getNanoseconds() {
        return nanoseconds;
    }

    public void setNanoseconds(int nanoseconds) {
        this.nanoseconds = nanoseconds;
    }
}
