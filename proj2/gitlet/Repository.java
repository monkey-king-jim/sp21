package gitlet;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.StringConcatFactory;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static gitlet.Utils.*;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * The commits directory.
     */
    public static final File GITLET_COMMITS = join(GITLET_DIR, "commits");
    /**
     * The blobs directory.
     */
    public static final File GITLET_BLOBS = join(GITLET_DIR, "blobs");
    /**
     * The stage area directory.
     */
    public static final File GITLET_STAGE = join(GITLET_DIR, "stage");
    /**
     * The stage info directory.
     */
    public static final File GITLET_STAGE_INFO = join(GITLET_STAGE, "info");
    /**
     * The branches directory.
     */
    public static final File GITLET_BRANCHES = join(GITLET_DIR, "branches");
    /**
     * The current branch directory.
     */
    public static final File GITLET_BRANCHES_CURRENT = join(GITLET_BRANCHES, "current");

    /**
     * The TreeMap object that tracks the staging activities.
     */
    public static final File STAGE_RECORDS = join(GITLET_STAGE_INFO, "stage_records");
    /**
     * The Set object that tracks the rm activities.
     */
    public static final File RM_RECORDS = join(GITLET_STAGE_INFO, "rm_records");
    /**
     * A current branch object.
     */
    public static final File CURRENT_BRANCH = join(GITLET_BRANCHES_CURRENT, "current_branch");
    /**
     * The stage records object.
     */
    public static TreeMap<String, String> stageRecords = new TreeMap<>();
    /**
     * The rm records object.
     */
    public static HashSet<String> rmRecords = new HashSet<>();
    /**
     * The current branch string.
     */
    public static String currentBranch;

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            GITLET_DIR.mkdir();
            GITLET_COMMITS.mkdir();
            GITLET_BLOBS.mkdir();
            GITLET_STAGE_INFO.mkdirs();
            GITLET_BRANCHES_CURRENT.mkdirs();

            writeObject(STAGE_RECORDS, stageRecords);
            writeObject(RM_RECORDS, rmRecords);

            Commit initCommit = new Commit();
            String init_Commit_SHA1 = sha1(serialize(initCommit));
            initCommit.sha1 = init_Commit_SHA1;
            writeObject(join(GITLET_COMMITS, init_Commit_SHA1), initCommit);     /* write initCommit to .commits/ */

            Branch masterBranch = new Branch(init_Commit_SHA1);
            currentBranch = "master";
            writeObject(join(GITLET_BRANCHES, currentBranch), masterBranch);
            writeObject(CURRENT_BRANCH, currentBranch);     /* record current branch as "master" */
        }
    }

    public static void add(String fileName) {
        File f = join(CWD, fileName);

        if (!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Blob blob = new Blob(f);
        String blobSHA1 = sha1(serialize(blob));

        Commit currentCommit = readCurrentCommit();
        stageRecords = readObject(STAGE_RECORDS, TreeMap.class);
        rmRecords = readObject(RM_RECORDS, HashSet.class);
        if (stageRecords.containsKey(fileName)) {
            /* remove the blob in the staging area if there's another version to be added */
            restrictedDelete(join(GITLET_STAGE, stageRecords.get(fileName)));
            /* if adding a file is the same as the one in current commit, remove this record from stage record */
            if (currentCommit.blobReferences.get(fileName).equals(blobSHA1)) {
                stageRecords.remove(fileName);
                writeObject(STAGE_RECORDS, stageRecords);
                System.exit(0);
            }
        }

        if (rmRecords.contains(fileName)) {
            rmRecords.remove(fileName);
            writeObject(RM_RECORDS, rmRecords);
        }

        writeObject(join(GITLET_STAGE, blobSHA1), blob);
        stageRecords.put(fileName, blobSHA1);
        writeObject(STAGE_RECORDS, stageRecords);
    }

    public static Commit readCurrentCommit() {
        return readObject(join(GITLET_COMMITS, readCurrentBranch().head), Commit.class);
    }

    private static String readCurrentBranchName() {return readObject(CURRENT_BRANCH, String.class);}

    public static Branch readCurrentBranch() { return readObject(join(GITLET_BRANCHES, readCurrentBranchName()), Branch.class);}

    public static void commit(String msg, String parent2ID) {
        stageRecords = readObject(STAGE_RECORDS, TreeMap.class);
        rmRecords = readObject(RM_RECORDS, HashSet.class);
        if (stageRecords.isEmpty() && rmRecords.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        
        Commit newCommit = new Commit(msg);
        Commit parentCommit = readObject(join(GITLET_COMMITS, newCommit.parentID), Commit.class);
        newCommit.blobReferences = parentCommit.blobReferences;

        if (newCommit.blobReferences == null) {
            newCommit.blobReferences = new TreeMap<>();
        }
        /* overwrite file versions got from the parent commit according to the stage records */
        newCommit.blobReferences.putAll(stageRecords);
        stageRecords.clear();
        writeObject(STAGE_RECORDS, stageRecords);

        for (String record : rmRecords) {
            newCommit.blobReferences.remove(record);
        }
        rmRecords.clear();
        writeObject(RM_RECORDS, rmRecords);

        /* move blob objects from stage area to the blob directory */
        List<String> blobs = plainFilenamesIn(GITLET_STAGE);
        for (String blob : blobs) {
            try {
                move(join(GITLET_STAGE, blob).toPath(), join(GITLET_BLOBS, blob).toPath(), REPLACE_EXISTING);
            } catch (IOException ex) {
                System.out.println("error when moving blob from stage to blob dir");
            }
        }

        /* TODO: maybe not use sha1 as an attribute? */
//        newCommit.sha1 = sha1(serialize(newCommit));
        String newCommitSHA1 = sha1(serialize(newCommit));

        if (parent2ID != null) {
            newCommit.parent2ID = parent2ID;
        }
//        writeObject(join(GITLET_COMMITS, newCommit.sha1), newCommit);
        writeObject(join(GITLET_COMMITS, newCommitSHA1), newCommit);

        Branch currentBranch = readCurrentBranch();
        currentBranch.head = newCommitSHA1;
        writeObject(join(GITLET_BRANCHES, readCurrentBranchName()), currentBranch);
    }

    public static void rm(String fileName) {
        stageRecords = readObject(STAGE_RECORDS, TreeMap.class);
        Commit currentCommit = readCurrentCommit();
        if (!stageRecords.containsKey(fileName) && !currentCommit.blobReferences.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (stageRecords.containsKey(fileName)) {
            stageRecords.remove(fileName);
            writeObject(STAGE_RECORDS, stageRecords);
        }
        if (currentCommit.blobReferences.containsKey(fileName)) {
            rmRecords = readObject(RM_RECORDS, HashSet.class);
            rmRecords.add(fileName);
            writeObject(RM_RECORDS, rmRecords);
            if (join(CWD, fileName).exists()) {
                restrictedDelete(join(CWD, fileName));
            }
        }
    }

    public static void log() {
        Commit tempCommit = readCurrentCommit();
        DateFormat dateFormat = new SimpleDateFormat("EEE MM d HH:mm:ss yyyy Z");
        while (tempCommit != null) {
            System.out.println("===");
            System.out.println("commit " + tempCommit.sha1);
            if (tempCommit.parent2ID != null) {
                System.out.println("Merge: " + tempCommit.parentID.substring(0,8) + tempCommit.parent2ID.substring(0, 8));
            }
            System.out.println("Date " + dateFormat.format(tempCommit.date));
            System.out.println(tempCommit.message);

            if (tempCommit.parentID == null) {
                break;
            }
            tempCommit = readObject(join(GITLET_COMMITS, tempCommit.parentID), Commit.class);
        }
    }

    public static void globalLog() {
        DateFormat dateFormat = new SimpleDateFormat("EEE MM d HH:mm:ss yyyy Z");
        for (String commit : plainFilenamesIn(GITLET_COMMITS)) {
            Commit tempCommit = readObject(join(GITLET_COMMITS, commit), Commit.class);
            System.out.println("===");
            System.out.println("commit " + tempCommit.sha1);
            if (tempCommit.parent2ID != null) {
                System.out.println("Merge: " + tempCommit.parentID.substring(0,8) + tempCommit.parent2ID.substring(0, 8));
            }
            System.out.println("Date " + dateFormat.format(tempCommit.date));
            System.out.println(tempCommit.message);
        }
    }

    public static void find(String msg) {
        int count = 0;
        for (String commit : plainFilenamesIn(GITLET_COMMITS)) {
            Commit tempCommit = readObject(join(GITLET_COMMITS, commit), Commit.class);
            if (msg.equals(tempCommit.message)) {
                System.out.println(commit);
                count ++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        /* TODO: use array to sort the outputs in lexicographic order */
        System.out.println("=== Branches ===");
        List<String> branches = plainFilenamesIn(GITLET_BRANCHES).stream().sorted().collect(Collectors.toList());
        for (String branchName : branches) {
            if (branchName.equals(readCurrentBranchName())) {
                System.out.println("*"+branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        stageRecords = readObject(STAGE_RECORDS, TreeMap.class);
        for (String fileName : stageRecords.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        rmRecords = readObject(RM_RECORDS, HashSet.class);
        for (String fileName : rmRecords) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        /* TODO: extra credit */
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void checkout(String fileName) {
        Commit tempCommit = readCurrentCommit();
        if (!tempCommit.blobReferences.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String blobID = tempCommit.blobReferences.get(fileName);
        Blob blob = readObject(join(GITLET_BLOBS, blobID), Blob.class);
        writeContents(join(CWD, fileName), blob.content);
    }

    public static void checkout(String commitID, String fileName) {
        TreeMap<String, String> commitIDs = new TreeMap<>();
        commitID = commitID.substring(0, 7);

        for (String s :
                plainFilenamesIn(GITLET_COMMITS)) {
            commitIDs.put(s.substring(0, 7), s);
        }
        if (!commitIDs.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit tempCommit = findCommitBySHA1(commitIDs.get(commitID));

        if (!tempCommit.blobReferences.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String blobID = tempCommit.blobReferences.get(fileName);
        Blob blob = readObject(join(GITLET_BLOBS, blobID), Blob.class);
        writeContents(join(CWD, fileName), blob.content);
    }

    public static void checkoutBranch(String branchName) {
        if (!join(GITLET_BRANCHES, branchName).exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (readCurrentBranchName().equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        /* find commit at the head of the given branch */
        Commit headCommit = findCommitByBranch(branchName);
        Commit currentCommit = readCurrentCommit();

        for (String fileName : headCommit.blobReferences.keySet()) {
            if (!currentCommit.blobReferences.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        for (String fileName : headCommit.blobReferences.keySet()) {
            checkout(headCommit.sha1, fileName);
        }

        currentBranch = branchName;
        writeObject(CURRENT_BRANCH, currentBranch);

        for (String fileName : currentCommit.blobReferences.keySet()) {
            if (!headCommit.blobReferences.containsKey(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }

        stageRecords = readObject(STAGE_RECORDS, TreeMap.class);
        stageRecords.clear();
        writeObject(STAGE_RECORDS,stageRecords);

        rmRecords = readObject(RM_RECORDS, HashSet.class);
        rmRecords.clear();
        writeObject(RM_RECORDS,rmRecords);
    }

    private static Commit findCommitByBranch(String branchName) {
        String commitSHA1 = readObject(join(GITLET_BRANCHES, branchName), Branch.class).head;
        return findCommitBySHA1(commitSHA1);
    }

    private static Commit findCommitBySHA1(String commitSHA1) {return readObject(join(GITLET_COMMITS, commitSHA1), Commit.class);}

    public static void branch(String branchName) {
        if (join(GITLET_BRANCHES, branchName).exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        Commit curCommit = readCurrentCommit();
        Branch newBranch = new Branch(curCommit.sha1);
        newBranch.joint = curCommit.sha1;
        writeObject(join(GITLET_BRANCHES, branchName), newBranch);
    }

    public static void rmBranch(String branchName) {
        if (!join(GITLET_BRANCHES, branchName).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (CURRENT_BRANCH.getName().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        restrictedDelete(join(GITLET_BRANCHES, branchName));
    }

    public static void reset(String commitID) {
        TreeMap<String, String> commitIDs = new TreeMap<>();
        commitID = commitID.substring(0, 7);

        for (String s :
                plainFilenamesIn(GITLET_COMMITS)) {
            commitIDs.put(s.substring(0, 7), s);
        }
        if (!commitIDs.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit curCommit = readCurrentCommit();
        Commit givenCommit = readObject(join(GITLET_COMMITS, commitIDs.get(commitID)), Commit.class);

        currentBranch = readCurrentBranchName();
        Branch curBranch = readCurrentBranch();
        curBranch.head = givenCommit.sha1;
        writeObject(CURRENT_BRANCH, curBranch);
        checkoutBranch(currentBranch);
    }

    public static void merge(String branchName) {
        Branch givenBranch = readObject(join(GITLET_BRANCHES, branchName), Branch.class);
        if (givenBranch.joint.equals(givenBranch.head)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        Branch curBranch = readCurrentBranch();
        if (givenBranch.joint.equals(curBranch.head)) {
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        Commit givenBranchHead = readObject(join(GITLET_COMMITS, givenBranch.head), Commit.class);
        Commit curBranchHead = readObject(join(GITLET_COMMITS, curBranch.head), Commit.class);
        Commit jointBranchHead = readObject(join(GITLET_COMMITS, givenBranch.joint), Commit.class);

        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(givenBranchHead.blobReferences.keySet());
        allFiles.addAll(curBranchHead.blobReferences.keySet());
        allFiles.addAll(jointBranchHead.blobReferences.keySet());

        for (String fileName : allFiles) {
            if (jointBranchHead.blobReferences.containsKey(fileName)) {
                if (curBranchHead.blobReferences.containsKey(fileName) && givenBranchHead.blobReferences.containsKey(fileName)) {
                    if (!jointBranchHead.blobReferences.get(fileName).equals(givenBranchHead.blobReferences.get(fileName))) {
                        /* #1 */
                        if (jointBranchHead.blobReferences.get(fileName).equals(curBranchHead.blobReferences.get(fileName))) {
                            checkout(givenBranchHead.sha1, fileName);
                            add(fileName);
                            /* #3 both modified in the same way */
                        } else if (givenBranchHead.blobReferences.get(fileName).equals(curBranchHead.blobReferences.get(fileName))) {
                            continue;
                            /* #8 */
                        } else {
                            replaceConflicted(fileName, curBranchHead, givenBranchHead);
                        }
                    }
                } else if (curBranchHead.blobReferences.containsKey(fileName) && !givenBranchHead.blobReferences.containsKey(fileName)) {
                    /* #6 */
                    if (jointBranchHead.blobReferences.get(fileName).equals(curBranchHead.blobReferences.get(fileName))) {
                        rm(fileName);
                    }
                } else if (!curBranchHead.blobReferences.containsKey(fileName) && givenBranchHead.blobReferences.containsKey(fileName)) {
                    /* #7 */
                    if (jointBranchHead.blobReferences.get(fileName).equals(givenBranchHead.blobReferences.get(fileName))) {
                        continue;
                    }
                }

                    /* #4 */
            } else if (!givenBranchHead.blobReferences.containsKey(fileName)) {
                continue;
                /* #5 */
            } else if (!curBranchHead.blobReferences.containsKey(fileName)) {
                checkout(givenBranchHead.sha1, fileName);
                add(fileName);
                /* #3 */
            } else if (givenBranchHead.blobReferences.get(fileName).equals(curBranchHead.blobReferences.get(fileName))) {
                continue;
                /* #8 */
            } else {
                replaceConflicted(fileName, curBranchHead, givenBranchHead);
            }
        }

        commit(String.format("Merged %s into %s.", branchName, readCurrentBranchName()), givenBranch.head);
    }

    private static void replaceConflicted(String fileName, Commit curCommit, Commit givenCommit) {
        Blob headBlob = readObject(join(GITLET_BLOBS, curCommit.blobReferences.get(fileName)), Blob.class);
        Blob givenBlob = readObject(join(GITLET_BLOBS, givenCommit.blobReferences.get(fileName)), Blob.class);

        writeContents(join(CWD, fileName), "<<<<<<< HEAD", headBlob.content, "=======", givenBlob.content, ">>>>>>>");
        System.out.println("Encountered a merge conflict.");
    }
}

