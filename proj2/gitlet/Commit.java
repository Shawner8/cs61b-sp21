package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayDeque;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  Commit class is a composition idea of both region(folder) "commits/" and commit object itself.
 *  COMMIT_FOLDER archive:
 *  --commits/
 *       |--3b7b0e38bd1382fddcadddf37a78e45ae6669462
 *       |--3c4a7f4d8227d924cfbd81d083dcd408921fd7fc
 *
 *  This class can be optimized to support abbr. commitUID search by reorganize the
 *  object storage archive like the example showed below:
 *  --commits/
 *       |--3b/
 *           |--7b0e38bd1382fddcadddf37a78e45ae6669462
 *       |--3c/
 *           |--4a7f4d8227d924cfbd81d083dcd408921fd7fc
 *
 *  @author Shawn
 */
public class Commit implements Serializable {

    /** Folder that commits live in. */
    static final File COMMIT_FOLDER = join(Repository.GITLET_DIR, "commits");

    private String message;
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
        secondParent = null;
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

    /** Return the first parent commit of this commit.
     *  If this commit has no parent, returns null.
     */
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

    /** Return whether the commit contains the file with name "fileName". */
    boolean containsFile(String fileName) {
        return files.containsKey(fileName);
    }

    /** Return the UID of the file saved in the commit. */
    String getFileReference(String fileName) {
        return files.get(fileName);
    }

    /** Return the content of the file saved in the commit. */
    String getFileContent(String fileName) {
        if (containsFile(fileName)) {
            String fileRef = getFileReference(fileName);
            Blob blob = BlobArea.load(fileRef);
            return blob.getFileContent();
        } else {
            return "";
        }
    }

    /** Return SHA1 of the commit.
     *  Lazy cache.
     */
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

    /** Return the log information of the commit. */
    String logInfo() {
        String t = String.format(Locale.US, "%1$ta %1$tb %1$te %1$tT %1$tY %1$tz", timestamp);
        if (secondParent == null) {
            return String.format("===\ncommit %s\nDate: %s\n%s\n\n", uid, t, message);
        } else {
            return String.format("===\ncommit %s\nMerge: %s %s\nDate: %s\n%s\n\n",
                    uid, firstParent.substring(0, 7), secondParent.substring(0, 7), t, message);
        }
    }

    /** Find all the ancestors of the given commit and saved in the HashSet. */
    static void findAncestors(String commitUID, TreeSet<String> ancestors) {
        if (commitUID != null) {
            Commit commit = load(commitUID);
            ancestors.add(commitUID);
            findAncestors(commit.firstParent, ancestors);
            findAncestors(commit.secondParent, ancestors);
        }
    }

    /** Return the uid of the split point of commit 1 and commit 2.
     *  Return null if the split point doesn't exist.
     *  Use BFS to determine the closest common ancestor.
     */
    static String findSplitPoint(String uid1, String uid2) {
        TreeSet<String> ancestorsOfCommit1 = new TreeSet<>();
        findAncestors(uid1, ancestorsOfCommit1);
        ArrayDeque<String> ancestorsOfCommit2 = new ArrayDeque<>();
        ancestorsOfCommit2.addLast(uid2);
        while (!ancestorsOfCommit2.isEmpty()) {
            String ancestor = ancestorsOfCommit2.removeFirst();
            if (ancestorsOfCommit1.contains(ancestor)) {
                return ancestor;
            } else {
                Commit commit = Commit.load(ancestor);
                if (commit.firstParent != null) {
                    ancestorsOfCommit2.addLast(commit.firstParent);
                }
                if (commit.secondParent != null) {
                    ancestorsOfCommit2.addLast(commit.secondParent);
                }
            }
        }
        return null;
    }
}
