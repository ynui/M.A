package appManager;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.*;

public class ZipHandler {

    public static void zipFile(File file, String name, String dest) throws IOException {
        try {
            String zipFileName = dest.endsWith("/") ?
                    dest
                            .concat(name) :
                    dest
                            .concat("/")
                            .concat(name);
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            zos.putNextEntry(new ZipEntry(file.getName()));

            byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            zos.close();
            fos.close();
        } catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", file.getPath());
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
    }

    public static List<String> unzipFolderToCompList(String name, String dest) {
        List<String> folderRep = new ArrayList<>();
        if (!(name.equals("") || name.equals("null"))) {
            File f = appManager.findFileInFolderByName(Paths.get(dest).toString(), name);
            String dataFromZipped = unzipFileToString(f);
            folderRep = Arrays.asList(dataFromZipped.split("\n"));
        }
        return folderRep;
    }

    public static String unzipFileToString(File f) {
        String out;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(f);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream stream = zipFile.getInputStream(entry);
                return inputStreamToString(stream);
            }
        } catch (Exception e) {
            System.out.println(f.getName());
            e.printStackTrace();
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String inputStreamToString(InputStream inputStream) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }


    public static void extract(File file, File destination) throws IOException {
        ZipInputStream in = null;
        FileOutputStream out = null;
        try {
            // Open the ZIP file
            in = new ZipInputStream(new FileInputStream(file));

            // Get the first entry
            ZipEntry entry = null;

            while ((entry = in.getNextEntry()) != null) {
                String outFilename = entry.getName();
                Files.createDirectories(destination.toPath());
                // Open the output file
                out = new FileOutputStream(new File(destination, outFilename));

                // Transfer bytes from the ZIP file to the output file
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Close the stream
                out.close();

            }
        } finally {
            // Close the stream
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
