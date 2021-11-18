package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args[0] == null) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init" -> Repository.init();
            case "add" -> {
                if (args[1] == null) {
                    System.out.println("Please enter a file name.");
                    System.exit(0);
                }
                Repository.add(args[1]);
            }
            case "commit" -> {
                if (args[1] == null) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.commit(args[1], null);
            }
            case "rm" -> {
                if (args[1] == null) {
                    System.out.println("Please enter a file name.");
                    System.exit(0);
                }
                Repository.rm(args[1]);
            }
            case "log" -> Repository.log();
            case "global-log" -> Repository.globalLog();
            case "find" -> {
                if (args[1] == null) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.find(args[1]);
            }
            case "status" -> Repository.status();
            case "checkout" -> {
                if (args[2] == null) {
                    Repository.checkoutBranch(args[1]);
                } else if (args[1].equals("--")) {
                    Repository.checkout(args[2]);
                } else if (args[2].equals("--") && args[3] != null) {
                    Repository.checkout(args[1], args[3]);
                } else {
                    System.out.println("Invalid checkout command.");
                    System.exit(0);
                }
            }
            case "branch" -> {
                if (args[1] == null) {
                    System.out.println("Please enter a branch name.");
                    System.exit(0);
                }
                Repository.branch(args[1]);
            }
            case "rm-branch" -> {
                if (args[1] == null) {
                    System.out.println("Please enter a branch name.");
                    System.exit(0);
                }
                Repository.rmBranch(args[1]);
            }
            case "reset"-> {
                if (args[1] == null) {
                    System.out.println("Please enter a commit ID.");
                    System.exit(0);
                }
                Repository.reset(args[1]);
            }
            case "merge" -> {
                if (args[1] == null) {
                    System.out.println("Please enter a branch name.");
                    System.exit(0);
                }
                Repository.merge(args[1]);
            }
        }
    }
}
