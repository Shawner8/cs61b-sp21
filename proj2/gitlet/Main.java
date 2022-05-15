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
                validateNumArgs(args, 1);
                Repository.setupPersistence();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validateGitletDirectoryExists();
                validateNumArgs(args, 2);
                Repository.stagedForAddition(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "rm":
                validateGitletDirectoryExists();
                validateNumArgs(args, 2);
                Repository.stagedForRemoval(args[1]);
                break;
            case "commit":
                validateGitletDirectoryExists();
                validateNumArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "log":
                validateGitletDirectoryExists();
                validateNumArgs(args, 1);
                Repository.log();
                break;
            case "global-log":
                validateGitletDirectoryExists();
                validateNumArgs(args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateGitletDirectoryExists();
                validateNumArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateGitletDirectoryExists();
                validateNumArgs(args, 1);
                Repository.status();
                break;
            default:
                message("No command with that name exists.");
                System.exit(0);
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * print the message "Incorrect operands." and exit if they do not match.
     *
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
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
