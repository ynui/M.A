package appManager;

import XMLgenerated.MagitBlob;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.apache.commons.io.FileUtils.readFileToString;

public class Blob {
    private String data;

    public Blob(File f) {
        this.data = readFileData(f);
    }

    public static MagitBlob getXmlBlob(XmlRepo repo, String blobId) {
        List<MagitBlob> blobsList = repo.getBlobs().getMagitBlob();
        for (MagitBlob b : blobsList) {
            if (b.getId().equals(blobId)) {
                return b;
            }
        }
        return null;
    }

    private String readFileData(File f) {
        try {
            return readFileToString(f, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSha1() {
        return DigestUtils.sha1Hex(this.data);
    }
}
