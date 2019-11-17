package ydkim2110.com.androidbarberstaffapp.Model;

import com.google.firebase.Timestamp;

public class MyNotification {

    private String uid, content, title;
    private boolean read;
    private Timestamp serverTimestamp;

    public MyNotification() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Timestamp getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(Timestamp serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
}
