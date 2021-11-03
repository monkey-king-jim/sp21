package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

import static gitlet.Repository.readCurrentCommit;
import static gitlet.Utils.serialize;
import static gitlet.Utils.sha1;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The date of this Commit. */
    private Date date;

    /** The message of this Commit. */
    private String message;

    /** The SHA-1 hashes of the 1st parent of this Commit. */
    public String parentID;

    /** The SHA-1 hashes of the 2nd parent of this Commit.
     * TODO: add reference to 2nd parent
     */

    /**
     * The file name paired with SHA-1 hashes of reference to the blob.
     */
    public HashMap<String, String> blobReferences;

    /* Make the initial commit */
    public Commit() {
        this.date = new Date(0);
        this.message = "initial commit";
    }

    /* Create a new commit */
    public Commit(String msg) {
        this.date = new Date();
        this.message = msg;
        this.parentID = sha1(serialize(readCurrentCommit()));
    }
}
