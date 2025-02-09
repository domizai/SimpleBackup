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
 * @modified 09.02.2025
 * @version 0.0.1 (1)
 */

package ch.domizai.simplebackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * SimpleBackup creates a backup of your project during runtime.
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
    /** Files that are ignored by default */
    public final static Set<String> DEFAULT_IGNORE = Set.of(".DS_Store", ".ini");

    private final String separator = FileSystems.getDefault().getSeparator();
    private final Path sketchPath;
    private long sizelimit = DEFAULT_SIZE_LIMIT; 
    private boolean verbose = true;
    private boolean reminded = false;
    private SketchPath dest;
    private Set<SketchPath> walkedFiles = new HashSet<>(); 
    private Set<SketchPath> omittedFiles = new HashSet<>(); 

    /**
     * Constructor, usually called in the setup() method in your sketch to
     * initialize and start the library.
     *
     * @param applet Pass 'this' when constructing a SimpleBackup instance.
     */
    public SimpleBackup(PApplet applet) {
        sketchPath = Path.of(applet.sketchPath());
        dest = new SketchPath(Path.of(DEFAULT_DEST));
    }

    /**
     * Add files or directories to copy.
     * 
     * @param paths A list of files or directories to copy.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup copy(List<String> paths) {
        if (!reminded) {
            printVerbose("Remember to save your sketch before backing up.");
            reminded = true;
        }
        walkedFiles.addAll(walk(paths));
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
        try {
            dest = makeSketchPath(backupDir, false);
        } catch (IllegalArgumentException e) {
            warning(e.getMessage());
            return this;
        }
        if (dest.relative.getNameCount() == 0 || dest.relative.getName(0).toString().isEmpty()) {
            dest = new SketchPath(Path.of(DEFAULT_DEST));
            warning("Empty destination directory name. Using default '" + DEFAULT_DEST + "'");
        }
        return this;
    }

    /**
     * Add files or directories to omit and prevent them from being copied.
     * 
     * @param paths A list of files or directories to omit.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup omit(List<String> paths) {
        omittedFiles.addAll(walk(paths));
        return this;
    }

    /**
     * Add files or directories to omit and prevent them from being copied.
     * 
     * @param paths A list of files or directories to omit.
     * @return {@code this} object (allows method chaining).
     */
    public SimpleBackup omit(String... paths) {
        omit(List.of(paths));
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
        SketchPath subDirPath;
        try {
            subDirPath = makeSketchPath(subDir, false);
        } catch (IllegalArgumentException e) {
            warning(e.getMessage());
            return false;
        }
        // Check if there are files to copy
        Set<SketchPath> filesToCopy = getFilesToCopy();
        if (filesToCopy.isEmpty()) {
            warning("Nothing to copy.");
            return false;
        }
        // Check total size of files to copy
        long size = sizeOf(filesToCopy);
        if (size > sizelimit) {
            warning("Not copying files. Attempting to copy " + size + " bytes. Limit is " + sizelimit + " bytes. Increase the limit with sizelimit (in bytes).");
            return false;
        }
        // Create destination directory if it doesn't exist
        if (!Files.exists(dest.absolute)) {
            try {
                printVerbose("Creating destination directory '" + dest + "'");
                Files.createDirectories(dest.absolute);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        // Create subdirectory 
        SketchPath finalDest = new SketchPath(this.dest.relative.resolve(subDirPath.relative));
        if (!Files.exists(finalDest.absolute)) {
            try {
                printVerbose("Creating subdirectory '" + subDirPath + "'");
                Files.createDirectories(finalDest.absolute);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        // Copy files
        AtomicInteger copied = new AtomicInteger();
        filesToCopy.forEach(fileFrom -> {
            SketchPath fileTo = new SketchPath(finalDest.relative.resolve(fileFrom.relative));
            SketchPath dirTo = new SketchPath(fileTo.absolute.getParent());
            
            if (Files.exists(fileTo.absolute)) {
                warning("File '" + fileTo + "' already exists. Not copying.");
                return;
            }
            try {
                Files.createDirectories(dirTo.absolute);
                Files.copy(fileFrom.absolute, fileTo.absolute);
                printVerbose("Copied '" + fileFrom);
                copied.incrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // Print summary
        int filesCopied = copied.get();
        printVerbose("Copied " + filesCopied + " of " + filesToCopy.size() + " files to '" + finalDest + "'");
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
     * Get the list of relative paths to the files that will be copied when calling {@link #backupNow()}.
     * This serves as a preview of the files that will be copied.
     * 
     * @return A list of relative paths of files to be copied.
     */
    public List<String> getFiles() {
        return getFilesToCopy().stream().map(p -> p.toString()).collect(Collectors.toList());
    }

    /**
     * Get the list of relative paths to the files that will be ignored when calling {@link #backupNow()}.
     * This serves as a preview of the files that will be omitted.
     * 
     * @return A list of relative paths of omitted files.
     */
    public List<String> getOmittedFiles() {
        return omittedFiles.stream().map(p -> p.toString()).collect(Collectors.toList());
    }

    /**
     * Get the list of relative paths to the files that will be ignored when calling {@link #backupNow()}.
     * Ignored files are files in the destination directory and files in the default ignore list {@link #DEFAULT_IGNORE}.
     * 
     * @return A list of relative paths of ignored files.
     */
    public List<String> getIgnoredFiles() {
        Set<SketchPath> ignoredFiles = new HashSet<>(walkedFiles);
        ignoredFiles.removeAll(getFilesToCopy());
        ignoredFiles.removeAll(omittedFiles);
        return ignoredFiles.stream().map(p -> p.toString()).collect(Collectors.toList());
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
        return sizeOf(getFilesToCopy());
    }

    /**
     * Construct a set of files to copy.
     */
    private Set<SketchPath> getFilesToCopy() {
        Set<SketchPath> filesToCopy = new HashSet<>(walkedFiles);
        removeFilesFromDestination(filesToCopy);
        removeDefaultFiles(filesToCopy);
        filesToCopy.removeAll(omittedFiles);
        return filesToCopy;
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
     * Returns the total size (in bytes) of the files in the given set.
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
            SketchPath file;

            try {
                file = makeSketchPath(path, true);
            } catch (IllegalArgumentException e) {
                warning(e.getMessage());
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
     * Removes any files from the set that are within the destination directory.
     */
    private void removeFilesFromDestination(Set<SketchPath> files) {
        files.removeIf(f -> f.relative.startsWith(dest.relative));
    }

    /**
     * Removes files from the set that are in the default omit list {@link #DEFAULT_IGNORE}.
     */
    private void removeDefaultFiles(Set<SketchPath> files) {
        files.removeIf(file -> DEFAULT_IGNORE.stream().anyMatch(file.relative.toString()::endsWith));
    }

    /**
     * Incoming paths must be relative.
     */
    private String stripPrecedingSeparator(String path) {
        return path.replaceAll("^\\" + separator + "+", "");
    }

    /**
     * Creates a SketchPath object if the path is within the sketch directory. 
     */
    private SketchPath makeSketchPath(String path, boolean checkIfExists) throws IllegalArgumentException {
        Path p = sketchPath.resolve(stripPrecedingSeparator(path)).normalize();
        // Incoming paths must be a child of the sketch directory.
        if (!p.startsWith(sketchPath)) {
            throw new IllegalArgumentException("Path '" + path + "' is not within the sketch directory.");
        }
        if (checkIfExists && !Files.exists(p)) {
            throw new IllegalArgumentException("Path '" + path + "' does not exist.");
        }
        return new SketchPath(p);
    }

    /** 
     * Helper class to store relative and absolute paths to the sketch directory.
     */
    private class SketchPath {
        final Path relative, absolute;
        
        /**
         * Only use this constructor if the path is guaranteed to exist and to be within the sketch directory.
         */
        SketchPath(Path path) {
            relative = path.isAbsolute() ? sketchPath.relativize(path) : path;
            absolute = sketchPath.resolve(relative).normalize();
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
