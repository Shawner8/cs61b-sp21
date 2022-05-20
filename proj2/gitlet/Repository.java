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

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

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
            String branchName = "master";
            saveBranch(branchName);
            Branch.set(branchName);
        }
    }

    /** Save a new branch named 'branchName' which points to the head commit. */
    static void saveBranch(String branchName) {
        if (Branch.contains(branchName)) {
            message("A branch with that name already exists.");
            System.exit(0);
        } else {
            Branch newBranch = new Branch(Head.get(), branchName);
            newBranch.save();
        }
    }

    static void removeBranch(String branchName) {
        if (!Branch.contains(branchName)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        } else if (Branch.get().equals(branchName)) {
            message("Cannot remove the current branch.");
            System.exit(0);
        } else {
            Branch.remove(branchName);
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
            if (StagingArea.RemovalArea.contains(file.getName())) {
                StagingArea.RemovalArea.remove(file.getName());
            }
            if (commit.containsFile(blob.getFileName())
                    && commit.getFileReference(blob.getFileName()).equals(blob.getUid())) {
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
            if (!commit.containsFile(fileName)
                    && !StagingArea.AdditionArea.contains(fileName)) {
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
            message("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        } else {
            Commit commit = Branch.load(branchName);
            clearCWD();
            for (String fileName : commit.getFiles().keySet()) {
                checkoutFileInCommit(commit.getUid(), fileName);
            }
            StagingArea.clear();
            Branch.set(branchName);
            Head.set(commit.getUid());
        }
    }

    static void reset(String commitUID) {
        Commit commit = Commit.load(commitUID);
        if (commit == null) {
            message("No commit with that id exists.");
            System.exit(0);
        } else if (!untrackedFiles().isEmpty()) {
            message("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        } else {
            clearCWD();
            for (String fileName : commit.getFiles().keySet()) {
                checkoutFileInCommit(commit.getUid(), fileName);
            }
            StagingArea.clear();
            Branch.update(commitUID);
            Head.set(commitUID);
        }
    }

    static void mergeFile(Commit currentBranch, Commit givenBranch, String fileName) {
        message("Encountered a merge conflict.");
        String fileContentsInCurrentBranch = currentBranch.getFileContent(fileName);
        String fileContentsInGivenBranch = givenBranch.getFileContent(fileName);
        String mergedFileContents = String.format("<<<<<<< HEAD\n%s=======\n%s>>>>>>>\n",
                fileContentsInCurrentBranch, fileContentsInGivenBranch);
        File mergedFile = join(CWD, fileName);
        writeContents(mergedFile, mergedFileContents);
        Blob blob = new Blob(fileName, mergedFileContents);
        StagingArea.AdditionArea.add(blob);
    }

    static void merge(String branchName) {
        if (!StagingArea.isEmpty()) {
            message("You have uncommitted changes.");
            System.exit(0);
        } else if (!Branch.contains(branchName)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        } else if (Branch.get().equals(branchName)) {
            message("Cannot merge a branch with itself.");
            System.exit(0);
        } else if (!untrackedFiles().isEmpty()) {
            message("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        } else {
            Commit currentBranch = Head.load();
            Commit givenBranch = Branch.load(branchName);
            String splitPoint = Commit.findSplitPoint(currentBranch.getUid(), givenBranch.getUid());
            Commit splitCommit = Commit.load(splitPoint);
            if (givenBranch.getUid().equals(splitPoint)) {
                message("Given branch is an ancestor of the current branch.");
                System.exit(0);
            } else if (currentBranch.getUid().equals(splitPoint)) {
                checkoutBranch(branchName);
                message("Current branch fast-forwarded.");
                System.exit(0);
            } else {
                for (String fileName : splitCommit.getFiles().keySet()) {
                    if (currentBranch.containsFile(fileName)
                            && givenBranch.containsFile(fileName)) {
                        if (!givenBranch.getFileReference(fileName).equals(splitCommit.getFileReference(fileName))
                                && currentBranch.getFileReference(fileName).equals(splitCommit.getFileReference(fileName))) {
                            checkoutFileInCommit(givenBranch.getUid(), fileName);
                            Blob blob = new Blob(fileName, givenBranch.getFileContent(fileName));
                            StagingArea.AdditionArea.add(blob);
                        } else if (!givenBranch.getFileReference(fileName).equals(splitCommit.getFileReference(fileName))
                                && !currentBranch.getFileReference(fileName).equals(splitCommit.getFileReference(fileName))
                                && !currentBranch.getFileReference(fileName).equals(givenBranch.getFileReference(fileName))) {
                            // deal with merge conflict: file exists in the given branch and the current branch but has different contents
                            mergeFile(currentBranch, givenBranch, fileName);
                        }
                    } else if (!currentBranch.containsFile(fileName)
                            && givenBranch.containsFile(fileName)) {
                        if (!givenBranch.getFileReference(fileName).equals(splitCommit.getFileReference(fileName))) {
                            // deal with merge conflict: file only exists in the given branch and has different contents from the split point
                            mergeFile(currentBranch, givenBranch, fileName);
                        }
                    } else if (currentBranch.containsFile(fileName)
                            && !givenBranch.containsFile(fileName)) {
                        if (currentBranch.getFileReference(fileName).equals(splitCommit.getFileReference(fileName))) {
                            stagedForRemoval(fileName);
                        } else if (!currentBranch.getFileReference(fileName).equals(splitCommit.getFileReference(fileName))) {
                            // deal with merge conflict: file only exists in the current branch and has different contents from the split point
                            mergeFile(currentBranch, givenBranch, fileName);
                        }
                    }
                }

                for (String fileName : givenBranch.getFiles().keySet()) {
                    if (!splitCommit.containsFile(fileName)) {
                        if (!currentBranch.containsFile(fileName)) {
                            checkoutFileInCommit(givenBranch.getUid(), fileName);
                            Blob blob = new Blob(fileName, givenBranch.getFileContent(fileName));
                            StagingArea.AdditionArea.add(blob);
                        } else if (!currentBranch.getFileReference(fileName).equals(givenBranch.getFileReference(fileName))) {
                            // deal with merge conflict: file exists in the given branch and the current branch but has different contents
                            mergeFile(currentBranch, givenBranch, fileName);
                        }
                    }
                }

                for (String fileName : currentBranch.getFiles().keySet()) {
                    if (!splitCommit.containsFile(fileName)) {
                        if (givenBranch.containsFile(fileName)
                                && !currentBranch.getFileReference(fileName).equals(givenBranch.getFileReference(fileName))) {
                            // deal with merge conflict: file exists in the given branch and the current branch but has different contents
                            mergeFile(currentBranch, givenBranch, fileName);
                        }
                    }
                }

                String commitMessage = String.format("Merged %s into %s.", branchName, Branch.get());
                Commit commit = new Commit(currentBranch, givenBranch, commitMessage);
                commit.save();
                StagingArea.AdditionArea.moveToBlobs();
                StagingArea.clear();
                Head.set(commit.getUid());
                Branch.update(commit.getUid());
            }
        }
    }
}
