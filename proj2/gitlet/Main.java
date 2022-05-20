package gitlet;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Shawn
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init" -> {
                validateNumArgs(args, 1, 1);
                Repository.setupPersistence();
            }
            case "add" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.stagedForAddition(args[1]);
            }
            case "rm" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.stagedForRemoval(args[1]);
            }
            case "commit" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.commit(args[1]);
            }
            case "log" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 1, 1);
                Repository.log();
            }
            case "global-log" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 1, 1);
                Repository.globalLog();
            }
            case "find" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.find(args[1]);
            }
            case "status" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 1, 1);
                Repository.status();
            }
            case "checkout" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 4);
                switch (args.length) {
                    case 2 -> Repository.checkoutBranch(args[1]);
                    case 3 -> {
                        if (!args[1].equals("--")) {
                            message("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkoutFileInCommit(Head.get(), args[2]);
                    }
                    case 4 -> {
                        if (!args[2].equals("--")) {
                            message("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkoutFileInCommit(args[1], args[3]);
                    }
                }
            }
            case "branch" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.saveBranch(args[1]);
            }
            case "rm-branch" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.removeBranch(args[1]);
            }
            case "reset" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.reset(args[1]);
            }
            case "merge" -> {
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.merge(args[1]);
            }
            default -> {
                message("No command with that name exists.");
                System.exit(0);
            }
        }
    }

    /**
     * Checks the number of arguments versus the expected number interval,
     * print the message "Incorrect operands." and exit if they do not match.
     *
     * @param args Argument array from command line
     * @param min Number of expected minimum arguments (included)
     * @param max Number of expected maximum arguments (included)
     */
    public static void validateNumArgs(String[] args, int min, int max) {
        if (args.length < min || args.length > max) {
            message("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void validateGitletDirectoryExists() {
        if (!Repository.GITLET_DIR.exists()) {
            message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
