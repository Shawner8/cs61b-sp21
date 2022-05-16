package gitlet;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

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
            String uid = initCommit.getUid();
            initCommit.save();
            Head.set(uid);
            Branch branch = new Branch(uid, "master");
            branch.save();
            Branch.set(branch.getName());
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
                    commit.getFileReference(blob.getFileName()).equals(blob.getUid())) {
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
            StagingArea.clear();
            Head.set(commit.getUid());
            Branch.update(commit.getUid());
        }
    }

    static void log() {
        Commit commit = Head.load();
        while (commit != null) {
            System.out.print(commit.logInfo());
            commit = commit.getParentCommit();
        }
    }

    static void globalLog() {
        List<String> commits = plainFilenamesIn(Commit.COMMIT_FOLDER);
        for (String commitUID : commits) {
            File commitFile = join(Commit.COMMIT_FOLDER, commitUID);
            Commit commit = readObject(commitFile, Commit.class);
            System.out.print(commit.logInfo());
        }
    }

    static void find(String message) {
        boolean exists = false;
        List<String> commits = plainFilenamesIn(Commit.COMMIT_FOLDER);
        for (String commitUID : commits) {
            File commitFile = join(Commit.COMMIT_FOLDER, commitUID);
            Commit commit = readObject(commitFile, Commit.class);
            if (commit.getMessage().equals(message)) {
                exists = true;
                System.out.println(commitUID);
            }
        }
        if (!exists) {
            message("Found no commit with that message.");
        }
    }

    static void status() {
        System.out.println("=== Branches ===");
        String currentBranch = Branch.get();
        List<String> branches = plainFilenamesIn(Branch.BRANCH_FOLDER);
        branches.sort(String::compareTo);
        for (String branchName : branches) {
            if (branchName.equals(currentBranch)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        TreeMap<String, String> additionFiles = StagingArea.AdditionArea.getMap();
        for (String fileName : additionFiles.navigableKeySet()) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        TreeSet<String> removalFiles = StagingArea.RemovalArea.getSet();
        for (String fileName : removalFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    static void checkoutFileInCommit(String commitUID, String fileName) {
        Commit commit = Commit.load(commitUID);
        if (commit == null) {
            message("No commit with that id exists.");
            System.exit(0);
        } else if (!commit.containsFile(fileName)) {
            message("File does not exist in that commit.");
            System.exit(0);
        } else {
            File file = join(CWD, fileName);
            String fileContent = commit.getFileContent(fileName);
            writeContents(file, fileContent);
        }
    }

    static List<String> untrackedFiles() {
        Commit commit = Head.load();
        LinkedList<String> untrackedFiles = new LinkedList<>();
        for (String fileName : plainFilenamesIn(CWD)) {
            if (!commit.containsFile(fileName)) {
                untrackedFiles.add(fileName);
            }
        }
        return untrackedFiles;
    }

    static void clearCWD() {
        for (String fileName : plainFilenamesIn(CWD)) {
            File file = join(CWD, fileName);
            restrictedDelete(file);
        }
    }

    static void checkoutBranch(String branchName) {
        if (!Branch.contains(branchName)) {
            message("No such branch exists.");
            System.exit(0);
        } else if (Branch.get().equals(branchName)) {
            message("No need to checkout the current branch.");
            System.exit(0);
        } else if (!untrackedFiles().isEmpty()) {
            message("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        } else {
            Commit commit = Branch.load(branchName);
            clearCWD();
            for (String fileName : commit.getFiles().keySet()) {
                checkoutFileInCommit(commit.getUid(), fileName);
            }
            StagingArea.clear();
            Branch.set(branchName);
        }
    }
}
