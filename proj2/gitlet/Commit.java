package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

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
    static final File COMMIT_FOLDER = Utils.join(Repository.GITLET_DIR, "commits");

    /** The message of this Commit. */
    private String message;

    /* TODO: fill in the rest of this class. */
    private Date timestamp;
    private String parent;
    private HashMap<String, String> files;
    private String uid;

    public Commit(String m, Date t, String p) {
        message = m;
        timestamp = t;
        parent = p;
        files = new HashMap<>();
    }

    /** Return SHA1 of the commit. */
    String uid() {
        return sha1(toString());
    }

    HashMap<String, String> getFiles() {
        return files;
    }

    void save() {
        File commit = join(COMMIT_FOLDER, uid());
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
}
