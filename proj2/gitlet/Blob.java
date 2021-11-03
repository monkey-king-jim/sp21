package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.readContents;

public class Blob implements Serializable {
    public byte[] content;

    public Blob(File f) {
        content = readContents(f);
    }
}
