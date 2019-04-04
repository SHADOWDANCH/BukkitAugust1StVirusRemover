package ua.shadowdan.august1stremover;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author SHADOWDAN
 * @since 04.04.2019
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please specify working directory.");
            return;
        }
        File workingDirectory = new File(args[0]);
        if (!workingDirectory.exists() || !workingDirectory.isDirectory()) {
            System.out.println("Working directory doesn't exists!");
            return;
        }
        File backup = new File(workingDirectory.getPath() + File.separator + "_BACKUP_");
        if (backup.exists()) {
            System.out.println("_BACKUP_ folder already exists! Remove or rename it then launch app again.");
            return;
        }
        if (!backup.mkdirs()) {
            System.out.println("Failed to create _BACKUP_ folder!");
            return;
        }
        File[] filesInWorkingDir = workingDirectory.listFiles();
        if (filesInWorkingDir == null) {
            System.out.println("Failed to fetch files list of working directory!");
            return;
        }
        for (File file : filesInWorkingDir) {
            if (file == null || !file.isFile())
                continue;
            String fileName = file.getName();
            String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
            String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
            if (!fileExtension.equalsIgnoreCase(".jar"))
                continue;

            File backedupFile = new File(backup.getPath() + File.separator + fileName);
            Files.move(file.toPath(), backedupFile.toPath());
            System.out.println("Repacking " + fileName + " ...");
            repack(backedupFile, new File(file.getParent() + File.separator + fileNameWithoutExt + "-clean" + fileExtension));
        }
    }

    public static void repack(File src, File dest) throws Exception {
        ZipFile zipFile = new ZipFile(src);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(dest));

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry == null) // Strip data after zip payload
                continue;

            InputStream entryInputStream = zipFile.getInputStream(entry);

            outputStream.putNextEntry(entry);
            byte[] readBuffer = new byte[entryInputStream.available()];
            int amountRead;
            while ((amountRead = entryInputStream.read(readBuffer)) > 0) {
                outputStream.write(readBuffer, 0, amountRead);
            }
            entryInputStream.close();
            outputStream.closeEntry();
        }
        outputStream.close();
    }
}
