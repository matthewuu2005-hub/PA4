import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * File Manipulation
 * Calculates the total size of directories and all their contents recursively
 */
public class DirectoryManipulation {
    
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#,##0.##");
    private DirectoryStatistics stats = new DirectoryStatistics();
    
    /**
     * Calculate the total size of a directory recursively
     * @param directoryPath Path to the directory
     * @return Total size in bytes
     * @throws IllegalArgumentException if path is invalid
     * @throws SecurityException if access is denied
     */
    public long calculateDirectorySize(String directoryPath) throws IllegalArgumentException, SecurityException {
        File directory = new File(directoryPath);
        return calculateDirectorySize(directory);
    }
    
    /**
     * Calculate the total size of a directory recursively
     * @param directory File object representing the directory
     * @return Total size in bytes
     */
    public long calculateDirectorySize(File directory) throws IllegalArgumentException, SecurityException {
        validateDirectory(directory);
        return calculateDirectorySizeRecursive(directory);
    }
    
    /**
     * Recursive method to calculate directory size with detailed statistics
     * @param file Current file or directory being processed
     * @param depth Current recursion depth
     * @return Size of the current file/directory and all its contents
     */
    private long calculateDirectorySizeRecursive(File file) {
         if (file.isFile()) {
            long size = file.length();
            stats.incrementFileCount();
            stats.addToTotalSize(size);
            stats.updateLargestFile(size, file.getAbsolutePath());
            String ext = getFileExtension(file.getName());
            stats.addExtensionSize(ext, size);
            return size;
        } else {
            stats.incrementDirectoryCount();
            long total = 0;
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    try {
                        total += calculateDirectorySizeRecursive(f);
                    } catch (SecurityException e) {
                        stats.addInaccessiblePath(f.getAbsolutePath());
                    }
                }
            }
            return total;
        }
    }
    
    /**
     * Get detailed analysis of directory with statistics
     * @param directory name of the directory to analyze
     * @return DirectoryStatistics object with detailed information
     */
    public DirectoryStatistics analyzeDirectory(String directory) {
         File dir = new File(directory);
        return analyzeDirectory(dir);
    }

    /**
     * Get detailed analysis of directory with statistics
     * @param directory File object to analyze
     * @return DirectoryStatistics object with detailed information
     */
    private DirectoryStatistics analyzeDirectory(File directory) {
         calculateDirectorySize(directory);
        return stats;
    }
    
    /**
     * Validate that the given file is a valid directory
     * @param directory File to validate
     */
    private void validateDirectory(File directory) {
        if(directory == null || !directory.exists() || !directory.isDirectory()){
            throw new IllegalArgumentException("Invalid directory");
        }
        if(!directory.canRead()){
            throw new SecurityException("Cannot read");
        }
    }
    
    /**
     * Get file extension from filename
     * @param fileName Name of the file
     * @return File extension (without dot) or "no extension"
     */
    private String getFileExtension(String fileName) {
       int i = fileName.lastIndexOf('.');
        return (i != -1 && i < fileName.length() - 1) ? fileName.substring(i + 1) : "no extension";
    }
    
    /**
     * Format bytes into human-readable format
     * @param bytes Number of bytes
     * @return Formatted string (e.g., "1.5 GB", "234.7 MB")
     */
    public static String formatBytes(long bytes) {
         String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        int i = 0;
        double size = bytes;
        while (size >= 1024 && i < units.length - 1) {
            size /= 1024;
            i++;
        }
        return String.format("%.2f %s", size, units[i]);
    }
    
    /**
     * Print detailed directory analysis report
     * @param stats DirectoryStatistics object
     * @param directoryPath Original directory path
     */
    public void printDetailedReport(DirectoryStatistics stats, String directoryPath) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("    Directory Analysis Results");
        System.out.println("═══════════════════════════════════════");
        System.out.println("Directory: " + directoryPath);
        System.out.println("Analysis Date: " + new Date());
        System.out.println();
        
        System.out.println("SUMMARY:");
        System.out.println("--------");
        System.out.println("Total Size: " + formatBytes(stats.getTotalSize()) + " (" + SIZE_FORMAT.format(stats.getTotalSize()) + " bytes)");
        System.out.println("Files: " + SIZE_FORMAT.format(stats.getFileCount()));
        System.out.println("Directories: " + SIZE_FORMAT.format(stats.getDirectoryCount()));
        System.out.println();
        
        if (stats.getLargestFileSize() > 0) {
            System.out.println("LARGEST FILE:");
            System.out.println("-------------");
            System.out.println("Size: " + formatBytes(stats.getLargestFileSize()));
            System.out.println("File: " + stats.getLargestFileName());
            System.out.println();
        }
        
        if (stats.getExtensionCount() > 0) {
            System.out.println("SIZE BY FILE TYPE:");
            System.out.println("------------------");
            
            // Sort extensions by size (descending)
            sortExtension();
            Pair[] extensions = stats.getExtensionSizes();
            long extCount = stats.getExtensionCount();
            for (int i=0; i< (int)extCount ; i++) {
                double percentage = (double) extensions[i].getSize() / stats.getTotalSize() * 100;
                System.out.printf("%-15s: %15s (%5.1f%%)%n", 
                    extensions[i].getType(), 
                    formatBytes(extensions[i].getSize()), 
                    percentage);
            }
            System.out.println();
        }
        
        System.out.println("═══════════════════════════════════════");
    }
    /**
     * sorts the array etxensionSizes by size
     */
    private void sortExtension(){
         Pair[] arr = stats.getExtensionSizes();
        int n = (int) stats.getExtensionCount();
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j].getSize() < arr[min].getSize()) {
                    min = j;
                }
            }
            Pair temp = arr[i];
            arr[i] = arr[min];
            arr[min] = temp;
        }
    }

    /**
     * 
     * @param directory the name of the file or directory
     * @param filename the name of the file the method searches for
     * @return true if the file was found anywhere in the hierarchy, false if the file was not found
     * prints the absolute path of all the locations where filename was found
     */
    public boolean findFile(String directory, String filename){
         File dir = new File(directory);
        return findFileRecursive(dir, filename);
    }

    private boolean findFileRecursive(File file, String filename) {
        boolean found = false;
        if (file.isFile()) {
            if (file.getName().equals(filename)) {
                System.out.println(file.getAbsolutePath());
                return true;
            }
        } else {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    found |= findFileRecursive(f, filename);
                }
            }
        }
        return found;
    }
    
    /**
     * deletes all the empty files or folders in the hierarchy of name
     * @param name of the file or directory 
     * prints the list of empty files deleted
     */
    public boolean cleanDirectory(String name){
       File dir = new File(name);
       return cleanDirectoryRecursive(dir);
    }

     private boolean cleanDirectoryRecursive(File file) {
        boolean removed = false;
        if (file.isFile() && file.length() == 0) {
            if (file.delete()) {
                System.out.println("Deleted empty file: " + file.getAbsolutePath());
                removed = true;
            }
        } else if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    removed = cleanDirectoryRecursive(f);
                }
            }
            if (file.listFiles().length == 0) {
                if (file.delete()) {
                    System.out.println("Deleted empty folder: " + file.getAbsolutePath());
                    removed |= true;
                }
            }
        }
        return removed;
    }


    /**
     * Searches for the given word in all the files in the hierarchy of files/folder under directory
     * @param directory the name of the file or directory
     * @param word the word being looked up
     * prints the files where the word was found and 
     * the number of occurences of the word in each file where the word was found
     */
    public boolean findWord(String directory, String word){
         File dir = new File(directory);
        return findWordRecursive(dir, word);
    }

    private boolean findWordRecursive(File file, String word) {
        boolean found = false;
        if (file.isFile()) {
            int count = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    int index = -1;
                    while ((index = line.indexOf(word, index + 1)) != -1) {
                        count++;
                    }
                }
            } catch (IOException e) {
                return false;
            }
            if (count > 0) {
                System.out.println(file.getName() + ": " + count);
                found = true;
            }
        } else {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    found |= findWordRecursive(f, word);
                }
            }
        }
        return found;
    }
}