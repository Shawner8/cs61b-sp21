package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  Commit class is a composition idea of both region(folder) "commits/" and commit object itself.
 *
 *  @author Shawn
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** Folder that commits live in. */
    static final File COMMIT_FOLDER = join(Repository.GITLET_DIR, "commits");

    /** The message of this Commit. */
    private String message;

    /* TODO: fill in the rest of this class. */
    private Date timestamp;
    private String parent;
    private TreeMap<String, String> files;
    private String uid;

    /** Construct the initial commit. */
    public Commit() {
        message = "initial commit";
        timestamp = new Date(0);
        parent = null;
        files = new TreeMap<>();
    }

    /** Construct a child commit of o. */
    public Commit(Commit o, String commitMessage) {
        message = commitMessage;
        timestamp = new Date();
        parent = o.getUid();
        files = o.files;

        TreeMap<String, String> additionMap = StagingArea.AdditionArea.getMap();
        for (String fileName : additionMap.keySet()) {
            files.put(fileName, additionMap.get(fileName));
        }

        TreeSet<String> removalSet = StagingArea.RemovalArea.getSet();
        for (String fileName : removalSet) {
            files.remove(fileName);
        }
    }

    String getMessage() {
        return message;
    }

    Date getTimestamp() {
        return timestamp;
    }

    Commit getParentCommit() {
        if (parent == null) {
            return null;
        } else {
            File parentCommit = join(COMMIT_FOLDER, parent);
            return readObject(parentCommit, Commit.class);
        }
    }

    boolean containsFile(String fileName) {
        return files.containsKey(fileName);
    }

    String getFileReference(String fileName) {
        return files.get(fileName);
    }

    /** Return SHA1 of the commit. */
    String getUid() {
        if (uid == null) {
            uid = sha1(toString());
        }
        return uid;
    }

    void save() {
        File commit = join(COMMIT_FOLDER, getUid());
        writeObject(commit, this);
    }

    @Override
    public String toString() {
        String s = String.format("message: %s\ntimestamp: %s\nparent: %s\nfiles:\n",
                message, timestamp.toString(), parent);
        for (String fileName : files.keySet()) {
            s += String.format("|--%s -> %s\n", fileName, files.get(fileName));
        }
        return s;
    }

    String logInfo() {
        String t = String.format(Locale.US, "%1$ta %1$tb %1$te %1$tT %1$tY %1$tz", timestamp);
        return String.format("===\ncommit %s\nDate: %s\n%s\n\n", uid, t, message);
    }
}
