/**
 * Simple Backup
 * Never lose your work. Automatically back up your project with every export.
 * 
 * Copyright (c) 2023 Dominique Schmitz http://domizai.com
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * @author Dominique Schmitz http://domizai.com
 * @modified 08.02.2025
 * @version 0.0.1 (1)
 */

package ch.domizai.simplebackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Date;
import java.text.SimpleDateFormat;
import processing.core.PApplet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Backup your project every time you make an export.
 */
public class SimpleBackup {
    /** The version of the library is {@value #VERSION} */
    public final static String VERSION = "0.0.1";
    /** The default destination directory is {@value #DEFAULT_DEST} */
    public final static String DEFAULT_DEST = "simplebackup";
    /** The default date pattern is {@value #DEFAULT_DATE_PATTERN} */
    public final static String DEFAULT_DATE_PATTERN = "yyMMddHHmmss";
    /** The default size limit in bytes is {@value #DEFAULT_SIZE_LIMIT} */
    public final static long DEFAULT_SIZE_LIMIT = 100_000; // bytes

    private final Path sketchPath;
    private final String separator;
    private long sizelimit = DEFAULT_SIZE_LIMIT; 
    private boolean verbose = true;
    private Set<SketchPath> filesToCopy = new HashSet<>(); 
    private Set<SketchPath> filesToIgnore = new HashSet<>(); 
    private SketchPath dest;
    private boolean reminded = true;

    /**
     * Constructor, usually called in the setup() method in your sketch to
     * initialize and start the library.
     *
     * @param applet Pass 'this' when constructing a SimpleBackup instance.
     */
    public SimpleBackup(PApplet applet) {
        sketchPath = Path.of(applet.sketchPath());
        separator = FileSystems.getDefault().getSeparator();
        dest = new SketchPath(Paths.get(DEFAULT_DEST));
    }

    /**
     * Add files or directories to copy.
     * 
     * @param paths A list of files or directories to copy.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup copy(List<String> paths) {
        filesToCopy.addAll(walk(paths));
        filesToCopy = filterSpecificFiles(filesToCopy);
        filesToCopy = filterFilesToIgnore(filesToCopy);
        filesToCopy = filterFilesFromDestination(filesToCopy);

        if (!reminded) {
            printVerbose("Remember to save your sketch before backing up.");
            reminded = true;
        }
        return this;
    }

    /**
     * Add files or directories to copy.
     * 
     * @param paths A list of files or directories to copy.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup copy(String... paths) {
        copy(List.of(paths));
        return this;
    }

    /**
     * Set the destination directory.
     * 
     * @param backupDir The destination directory.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup to(String backupDir) {
        dest = new SketchPath(relativize(backupDir));
        if (dest.relative.getNameCount() == 0 || dest.relative.getName(0).toString().isEmpty()) {
            throw new IllegalArgumentException("Destination directory is empty.");
        }
        filesToCopy = filterFilesFromDestination(filesToCopy);
        return this;
    }

    /**
     * Add files or directories to ignore.
     * 
     * @param paths A list of files or directories to ignore.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup ignore(List<String> paths) {
        filesToIgnore.addAll(walk(paths));
        filesToCopy = filterFilesToIgnore(filesToCopy);
        return this;
    }

    /**
     * Add files or directories to ignore.
     * 
     * @param paths A list of files or directories to ignore.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup ignore(String... paths) {
        ignore(List.of(paths));
        return this;
    }

    /**
     * Copy files to a subdirectory within the destination directory.
     * If no destination directory is set using {@link #to(String backupDir)}, the files will be copied to {@value #DEFAULT_DEST}.
     * 
     * @param subDir The subdirectory in the destination directory.
     * @return {@code true} if all files in the list were copied, false otherwise.
     */
    public boolean backupNow(String subDir) {
        SketchPath subDirPath = new SketchPath(relativize(subDir));

        if (filesToCopy.isEmpty()) {
            warning("Nothing to copy.");
            return false;
        }

        // Check total size of files to copy
        long size = sizeOf(filesToCopy);
        if (size > sizelimit) {
            warning("Not copying files. Attempting to copy " + size + " bytes. Limit is " + sizelimit + " bytes. Increase the limit with sizelimit(bytes).");
            return false;
        }

        // Create destination directory if it doesn't exist
        SketchPath dest = new SketchPath(this.dest.relative.resolve(subDirPath.relative));
        if (!Files.exists(dest.absolute)) {
            try {
                Files.createDirectories(dest.absolute);
                printVerbose("No destination directory specified. Using " + dest);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // Copy files
        AtomicInteger copied = new AtomicInteger();
        filesToCopy.forEach(fileFrom -> {
            SketchPath fileTo = new SketchPath(dest.relative.resolve(fileFrom.relative));
            SketchPath dirTo = new SketchPath(fileTo.absolute.getParent());
            
            if (Files.exists(fileTo.absolute)) {
                warning("File " + fileTo + " already exists. Not copying.");
                return;
            }
            try {
                Files.createDirectories(dirTo.absolute);
                Files.copy(fileFrom.absolute, fileTo.absolute);
                printVerbose("Copied " + fileFrom + " to " + dirTo);
                copied.incrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        int filesCopied = copied.get();
        printVerbose("Copied " + filesCopied + " of " + filesToCopy.size() + " files to " + dest);
        return filesCopied < filesToCopy.size() ? false : true;
    }

    /**
     * Copy files to a subdirectory within the destination directory.
     * If no destination directory is set using {@link #to(String backupDir)}, the files will be copied to {@value #DEFAULT_DEST}.
     * The subdirectory name follows the format {@value #DEFAULT_DATE_PATTERN} and is determined at backup time.
     * 
     * @return {@code true} if all files in the list were copied, false otherwise.
     */
    public boolean backupNow() {
        return backupNow(currentDateAndTime());
    }

    /**
     * Get the list of relative paths to the files to be copied when calling {@link #backupNow()}.
     * This serves as a preview of the files that will be copied.
     * 
     * @return A list of relative paths to the files to be copied.
     */
    public List<String> getFilesToCopy() {
        return filesToCopy.stream().map(p -> p.toString()).collect(Collectors.toList());
    }

    /**
     * Get the list of relative paths to the files to be ignored when calling {@link #backupNow()}.
     * This serves as a preview of the files that will be ignored.
     * 
     * @return A list of relative paths to the files to be ignored.
     */
    public List<String> getFilesToIgnore() {
        return filesToIgnore.stream().map(p -> p.toString()).collect(Collectors.toList());
    }

    /**
     * Set the size limit for copying files. 
     * Default is {@value #DEFAULT_SIZE_LIMIT} bytes.
     * This is a safety measure to prevent copying large files by accident.
     * 
     * @param sizelimit The size limit in bytes.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup sizelimit(long sizelimit) {
        this.sizelimit = sizelimit;
        return this;
    }

    /**
     * Set the verbose flag. True by default.
     * 
     * @param verbose The verbose flag.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    /**
     * Get the current date and time using the default pattern {@value #DEFAULT_DATE_PATTERN}
     * 
     * @return The current date and time formatted according to the default pattern.
     */
    public static String currentDateAndTime() {
        return currentDateAndTime(DEFAULT_DATE_PATTERN);
    }

    /**
     * Get the current date and time using the given pattern.
     * 
     * @param pattern The pattern describing the date and time format.
     * @return The current date and time formatted according to the pattern.
     */
    public static String currentDateAndTime(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    /**
     * Return the version of the library.
     *
     * @return The version of the library.
     */
    public static String version() {
        return VERSION;
    }
    
    /**
     * Returns the total size of the files to be copied when calling {@link #backupNow()}.
     * 
     * @return The total size of the files to be copied in bytes.
     */
    public long getSize() {
        return sizeOf(filesToCopy);
    }

    /**
     * Incoming paths must be relative.
     */
    private Path relativize(String path) {
        Path p = Path.of(path.trim().replaceAll("\\.\\.", "")).normalize();
        return p.isAbsolute() ? Path.of(separator).relativize(p) : p;
    }

    /**
     * Print a message if verbose is true.
     */
    private void printVerbose(String msg) {
        if (verbose) {
            PApplet.println(msg);
        }
    }

    /**
     * Prints a message regardless of the verbose flag.
     */
    private void warning(String msg) {
        PApplet.println("WARNING: " + msg);
    }

    /**
     * Returns the total size of the files in the given set.
     */
    private long sizeOf(Set<SketchPath> files) {
        return files.stream().mapToLong(path -> path.absolute.toFile().length()).sum();
    }

    /** 
     * Recursively walks the list of paths and returns a set of existing files (without directories).
     */
    private Set<SketchPath> walk(List<String> paths) {
        Set<SketchPath> files = new HashSet<>();
        
        for (String path : paths) {
            SketchPath file = new SketchPath(relativize(path));

            if (!Files.exists(file.absolute)) {
                warning("Path " + file + " does not exist.");
                continue;
            }
            
            if (Files.isDirectory(file.absolute)) {
                try (Stream<Path> stream = Files.walk(file.absolute)) {
                    files.addAll(stream
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> new SketchPath(p))
                        .collect(Collectors.toSet()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * Removes any files from filesToCopy that are within the destination directory
     */
    private Set<SketchPath> filterFilesFromDestination(Set<SketchPath> filesToCopy) {
        filesToCopy.removeIf(fileFrom -> fileFrom.relative.startsWith(dest.relative));
        return filesToCopy;  
    }

    /**
     * Removes any files from filesToCopy that we don't want to copy.
     */
    private Set<SketchPath> filterFilesToIgnore(Set<SketchPath> filesToCopy) {
        filesToCopy.removeAll(filesToIgnore);
        return filesToCopy;
    }

    /**
     * Removes specific files from filesToCopy that we don't want to copy.
     */
    private Set<SketchPath> filterSpecificFiles(Set<SketchPath> filesToCopy) {
        filesToCopy.removeIf(file -> file.relative.endsWith(".DS_Store"));  
        return filesToCopy;
    }

    /** 
     * Helper class to store relative and absolute paths to the sketch directory 
     */
    private class SketchPath {
        Path relative;
        Path absolute;
        
        SketchPath(Path path) {
            relative = path.isAbsolute() ? sketchPath.relativize(path) : path;
            absolute = sketchPath.resolve(relative);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SketchPath) {
                return relative.equals(((SketchPath) obj).relative);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return relative.hashCode();
        }

        @Override
        public String toString() {
            return relative.toString();
        }
    }
}
