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
        // TODO: what if args is empty?
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validateNumArgs(args, 1, 1);
                Repository.setupPersistence();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.stagedForAddition(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "rm":
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.stagedForRemoval(args[1]);
                break;
            case "commit":
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.commit(args[1]);
                break;
            case "log":
                validateGitletDirectoryExists();
                validateNumArgs(args, 1, 1);
                Repository.log();
                break;
            case "global-log":
                validateGitletDirectoryExists();
                validateNumArgs(args, 1, 1);
                Repository.globalLog();
                break;
            case "find":
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateGitletDirectoryExists();
                validateNumArgs(args, 1, 1);
                Repository.status();
                break;
            case "checkout":
                validateGitletDirectoryExists();
                validateNumArgs(args, 2, 4);
                switch (args.length) {
                    case 2:
                        Repository.checkoutBranch(args[1]);
                        break;
                    case 3:
                        Repository.checkoutFileInCommit(Head.get(), args[2]);
                        break;
                    case 4:
                        Repository.checkoutFileInCommit(args[1], args[3]);
                        break;
                }
                break;
            default:
                message("No command with that name exists.");
                System.exit(0);
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
