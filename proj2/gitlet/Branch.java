package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {
    String head;
    String joint;

    public Branch(String commitSHA1) {
        this.head = commitSHA1;
    }
}
