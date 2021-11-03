package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Utils.*;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardCopyOption.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

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
     * The stage directory.
     */
    public static final File GITLET_STAGE = join(GITLET_DIR, "stage");
    /**
     * The branches directory.
     */
    public static final File GITLET_BRANCHES = join(GITLET_DIR, "branches");
    /**
     * The current branch directory.
     */
    public static final File GITLET_BRANCHES_CURRENT = join(GITLET_BRANCHES, "current");

    /**
     * The HashMap object that tracks the staging activities.
     */
    public static final File STAGE_RECORDS = join(GITLET_STAGE, "stage_records");
    /**
     * The Set object that tracks the rm activities.
     */
    public static final File RM_RECORDS = join(GITLET_STAGE, "rm_records");
    /**
     * A current branch pointer.
     */
    public static final File CURRENT_BRANCH = join(GITLET_BRANCHES_CURRENT, "current_branch");
    /**
     * The stage records object.
     */
    public static HashMap<String, String> stageRecords = new HashMap<>();
    /**
     * The rm records object.
     */
    public static HashSet<String> rmRecords = new HashSet<>();
    /**
     * The current branch string.
     */
    public static String currentBranch = "master";


    /* TODO: fill in the rest of this class. */

    public static void init() {
        /* If there is already a Gitlet version-control system in the current directory, it should abort */
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            GITLET_DIR.mkdir();
            GITLET_COMMITS.mkdir();
            GITLET_BLOBS.mkdir();
            GITLET_STAGE.mkdir();
            GITLET_BRANCHES_CURRENT.mkdirs();

            writeObject(STAGE_RECORDS, stageRecords);   /* write a HashMap into STAGE_RECORDS to keep track staged files */
            writeObject(RM_RECORDS, rmRecords);

            Commit initCommit = new Commit();
            String init_Commit_SHA1 = sha1(serialize(initCommit));
            writeObject(join(GITLET_COMMITS, init_Commit_SHA1), initCommit);     /* write initCommit to .commits/ */

            /* TODO: branch just the head commit? write branch just the SHA-1 code? */
            File master = join(GITLET_COMMITS, init_Commit_SHA1);
            writeObject(join(GITLET_BRANCHES, "master"), master);   /* create a branch called master that points to the iniital commit */
            writeObject(CURRENT_BRANCH, currentBranch);     /* record current branch as master */
        }
    }

    public static void add(String fileName) {
        File f = join(CWD, fileName);

        /* handle the failure case where the file does not exist */
        if (!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Blob blob = new Blob(f);
        String blobSHA1 = sha1(serialize(blob));

        Commit currentCommit = readCurrentCommit();  /* locate current commit */
        stageRecords = readObject(STAGE_RECORDS, HashMap.class);
        rmRecords = readObject(RM_RECORDS, HashSet.class);
        if (stageRecords.containsKey(fileName)) {
            join(GITLET_STAGE, stageRecords.get(fileName)).delete();    /* remove the blob in the staging area if another version to be add */
            if (currentCommit.blobReferences.containsValue(blobSHA1)) {
                stageRecords.remove(fileName);      /* if adding a file is the same as the one in current commit, remove this record from stage record */
                writeObject(STAGE_RECORDS, stageRecords);
                System.exit(0);
            }
        }

        if (rmRecords.contains(fileName)) {
            rmRecords.remove(fileName);
        }

        writeObject(join(GITLET_STAGE, blobSHA1), blob);    /* write a blob contains the new version of the file using its SHA-1 code */
        stageRecords.put(fileName, blobSHA1);   /* record this add in stage records */
    }

    public static Commit readCurrentCommit() {
        String cur = readObject(CURRENT_BRANCH, String.class);   /* get current branch name */
        File branch = readObject(join(GITLET_BRANCHES, cur), File.class);  /* locate current branch object file */
        return readObject(branch, Commit.class);    /* read such file as a commit object */
    }

    public static void commit(String msg) {
        Commit newCommit = new Commit(msg);
        Commit parentCommit = readObject(join(GITLET_COMMITS, newCommit.parentID), Commit.class);
        newCommit.blobReferences = parentCommit.blobReferences;

        stageRecords = readObject(STAGE_RECORDS, HashMap.class);    /* get stage records */
        newCommit.blobReferences.putAll(stageRecords);  /* overwrite file versions from parent according to the stage records */
        stageRecords.clear();
        writeObject(STAGE_RECORDS, stageRecords);

        rmRecords = readObject(RM_RECORDS, HashSet.class);  /* get rm records */
        for (String record : rmRecords) {
            newCommit.blobReferences.remove(record);
        }
        rmRecords.clear();
        writeObject(RM_RECORDS, rmRecords);



    }
}

