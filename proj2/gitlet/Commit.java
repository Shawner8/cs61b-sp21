package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.*;

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
    private String firstParent;
    private String secondParent;
    private TreeMap<String, String> files;
    private String uid;

    /** Construct the initial commit. */
    public Commit() {
        message = "initial commit";
        timestamp = new Date(0);
        firstParent = null;
        secondParent = null;
        files = new TreeMap<>();
    }

    /** Construct a child commit of o. */
    public Commit(Commit o, String commitMessage) {
        message = commitMessage;
        timestamp = new Date();
        firstParent = o.getUid();
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

    /** Construct a merged commit. */
    public Commit(Commit firstP, Commit secondP, String commitMessage) {
        message = commitMessage;
        timestamp = new Date();
        firstParent = firstP.getUid();
        secondParent = secondP.getUid();
        files = firstP.files;

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
        if (firstParent == null) {
            return null;
        } else {
            File parentCommit = join(COMMIT_FOLDER, firstParent);
            return readObject(parentCommit, Commit.class);
        }
    }

    TreeMap<String, String> getFiles() {
        return files;
    }

    boolean containsFile(String fileName) {
        return files.containsKey(fileName);
    }

    String getFileReference(String fileName) {
        return files.get(fileName);
    }

    String getFileContent(String fileName) {
        if (containsFile(fileName)) {
            String fileRef = getFileReference(fileName);
            Blob blob = BlobArea.load(fileRef);
            return blob.getFileContent();
        } else {
            return "";
        }
    }

    /** Return SHA1 of the commit. */
    String getUid() {
        if (uid == null) {
            uid = sha1(toString());
        }
        return uid;
    }

    /** Save the commit. */
    void save() {
        File commit = join(COMMIT_FOLDER, getUid());
        writeObject(commit, this);
    }

    /** Load the commit with given uid.
     *  If the commit doesn't exist, return null.
     */
    static Commit load(String commitUID) {
        File commitFile = join(COMMIT_FOLDER, commitUID);
        if (!commitFile.exists()) {
            return null;
        } else {
            return readObject(commitFile, Commit.class);
        }
    }

    @Override
    public String toString() {
        String s = String.format("message: %s\ntimestamp: %s\nparent: %s\nfiles:\n",
                message, timestamp.toString(), firstParent);
        for (String fileName : files.keySet()) {
            s += String.format("|--%s -> %s\n", fileName, files.get(fileName));
        }
        return s;
    }

    String logInfo() {
        String t = String.format(Locale.US, "%1$ta %1$tb %1$te %1$tT %1$tY %1$tz", timestamp);
        if (secondParent == null) {
            return String.format("===\ncommit %s\nDate: %s\n%s\n\n", uid, t, message);
        } else {
            return String.format("===\ncommit %s\nMerge: %s %s\nDate: %s\n%s\n\n",
                    uid, firstParent.substring(0, 7), secondParent.substring(0, 7), t, message);
        }
    }

    /** Return the uid of the split point of commit 1 and commit 2.
     *  Return null if the split point doesn't exist.
     */
    static String findSplitPoint(String uid1, String uid2) {
        HashSet<String> ancestorsOfCommit1 = new HashSet<>();
        Commit commit1 = load(uid1);
        while (commit1 != null) {
            ancestorsOfCommit1.add(commit1.getUid());
            commit1 = commit1.getParentCommit();
        }
        Commit commit2 = load(uid2);
        while (commit2 != null) {
            if (ancestorsOfCommit1.contains(commit2.getUid())) {
                return commit2.getUid();
            }
            commit2 = commit2.getParentCommit();
        }
        return null;
    }
}
