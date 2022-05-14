package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  The structure of a Gitlet Repository is as follows:
 *
 *  .gitlet/ -- top level folder for all persistent
 *      - stage/ -- folder containing all of the persistent data in stage area
 *      - commits/ -- folder containing all of the persistent data for commits
 *      - blobs/ -- folder containing all of the persistent data for blobs
 *      - branches/ -- folder containing all of the persistent data for references to commit
 *      - BRANCH -- file containing the reference to the current branch
 *      - HEAD -- file containing the reference to the current commit
 *
 *  @author Shawn
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */
    static void setupPersistence() {
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            GITLET_DIR.mkdir();
            StageArea.init();
            Commit.COMMIT_FOLDER.mkdir();
            BlobArea.init();
            Branch.BRANCH_FOLDER.mkdir();

            Commit initCommit = new Commit("initial commit", new Date(0), null);
            String uid = initCommit.uid();
            initCommit.save();
            Head.set(uid);
            Branch branch = new Branch(uid, "master");
            branch.save();
            branch.set();
        }
    }

    static void staging(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            message("File does not exist.");
            System.exit(0);
        } else {
            Blob blob = new Blob(file);
            Commit commit = Head.load();
            HashMap<String, String> commitFiles = commit.getFiles();
            if (commitFiles.containsKey(blob.getFileName()) &&
                    commitFiles.get(blob.getFileName()).equals(blob.getUid())) {
                StageArea.remove(blob.getFileName());
            } else {
                StageArea.add(blob);
            }
        }
    }
}
