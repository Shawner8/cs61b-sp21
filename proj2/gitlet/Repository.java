package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  The structure of a Gitlet Repository is as follows:
 *
 *  .gitlet/ -- top level folder for all persistent
 *      - staging/ -- folder containing all of the persistent data in staging area
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
            StagingArea.init();
            Commit.COMMIT_FOLDER.mkdir();
            BlobArea.init();
            Branch.BRANCH_FOLDER.mkdir();

            Commit initCommit = new Commit();
            String uid = initCommit.uid();
            initCommit.save();
            Head.set(uid);
            Branch branch = new Branch(uid, "master");
            branch.save();
            branch.set();
        }
    }

    static void stagedForAddition(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            message("File does not exist.");
            System.exit(0);
        } else {
            Blob blob = new Blob(file);
            Commit commit = Head.load();
            if (commit.containsFile(blob.getFileName()) &&
                    commit.getFileReference(blob.getFileName()).equals(blob.uid())) {
                StagingArea.AdditionArea.remove(blob.getFileName());
            } else {
                StagingArea.AdditionArea.add(blob);
            }
        }
    }

    static void stagedForRemoval(String fileName) {
        File file = join(CWD, fileName);
        Commit commit = Head.load();
        if (StagingArea.AdditionArea.contains(file.getName())) {
            StagingArea.AdditionArea.remove(file.getName());
        } else if (commit.containsFile(file.getName())) {
            StagingArea.RemovalArea.add(file.getName());
            restrictedDelete(file);
        } else {
            message("No reason to remove the file.");
            System.exit(0);
        }
    }

    static void commit(String commitMessage) {
        if (StagingArea.isEmpty()) {
            message("No changes added to the commit.");
            System.exit(0);
        } else if (commitMessage.isBlank()) {
            message("Please enter a commit message.");
            System.exit(0);
        } else {
            Commit commit = new Commit(Head.load(), commitMessage);
            commit.save();
            StagingArea.AdditionArea.moveToBlobs();
            StagingArea.init();
            Head.set(commit.uid());
            Branch.update(commit.uid());
        }
    }
}
