/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.file.nativeimpl;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BRecordType;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.ArrayValueImpl;
import org.ballerinalang.stdlib.file.utils.FileConstants;
import org.ballerinalang.stdlib.file.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_PACKAGE_ID;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * Native function implementations of the file module.
 *
 * @since 1.1.0
 */
public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static final String CURRENT_DIR_PROPERTY_KEY = "user.dir";
    private static final String TEMP_DIR_PROPERTY_KEY = "java.io.tmpdir";
    private static final String RECURSIVE = "RECURSIVE";
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhgijklmnopqrstuvwxyz0123456789";
    private static final Random rnd = new Random();

    public static BString getCurrentDirectory() {
        return StringUtils.fromString(FileUtils.getSystemProperty(CURRENT_DIR_PROPERTY_KEY));
    }

    public static Object createDir(BString dir, BString dirOption) {
        String op = dirOption.getValue();
        try {
            if (op.equals(RECURSIVE)) {
                Files.createDirectories(Paths.get(dir.getValue()));
            } else {
                Files.createDirectory(Paths.get(dir.getValue()));
            }
            return null;
        } catch (FileAlreadyExistsException e) {
            String msg = "File already exists. Failed to create the file: " + dir;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR, msg);
        } catch (SecurityException e) {
            String msg = "Permission denied. Failed to create the file: " + dir;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, msg);
        } catch (IOException e) {
            String msg = "IO error while creating the file " + dir;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, msg);
        } catch (Exception e) {
            String msg = "Error while creating the file " + dir;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, msg);
        }
    }

    public static Object rename(BString oldPath, BString newPath) {
        Path oldFilePath = Paths.get(oldPath.getValue());
        Path newFilePath = Paths.get(newPath.getValue());

        if (Files.notExists(oldFilePath)) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                    "File not found: " + oldFilePath.toAbsolutePath());
        }

        try {
            Files.move(oldFilePath.toAbsolutePath(), newFilePath.toAbsolutePath());
            return null;
        } catch (FileAlreadyExistsException e) {
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                    "File already exists in the new path " + newFilePath);
        } catch (IOException e) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, e);
        } catch (SecurityException e) {
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, e);
        }
    }

    public static Object createFile(BString path) {
        try {
            Files.createFile(Paths.get(path.getValue()));
            return null;
        } catch (FileAlreadyExistsException e) {
            String msg = "File already exists. Failed to create the file: " + path;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR, msg);
        } catch (SecurityException e) {
            String msg = "Permission denied. Failed to create the file: " + path;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, msg);
        } catch (NoSuchFileException e) {
            String msg = "The file does not exist in path " + path;
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, msg);
        } catch (IOException e) {
            String msg = "IO error occurred while creating the file " + path;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, msg);
        } catch (Exception e) {
            String msg = "Error occurred while creating the file " + path;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, msg);
        }
    }

    public static Object getMetaData(BString path) {
        File inputFile = Paths.get(path.getValue()).toAbsolutePath().toFile();
        if (!inputFile.exists()) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR, "File not found: " + path);
        }
        try {
            return FileUtils.getMetaData(inputFile);
        } catch (IOException e) {
            log.error("IO error while creating the file " + path, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, e);
        }
    }

    public static Object remove(BString path, BString dirOption) {
        File removeFile = Paths.get(path.getValue()).toAbsolutePath().toFile();
        String wdBValue = FileUtils.getSystemProperty(CURRENT_DIR_PROPERTY_KEY);
        File wd = Paths.get(wdBValue).toAbsolutePath().toFile();
        String op = dirOption.getValue();

        try {
            if (wd.getCanonicalPath().equals(removeFile.getCanonicalPath())) {
                return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                        "Cannot delete the current working directory " + wd.getCanonicalPath());
            }

            if (!removeFile.exists()) {
                return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                        "File not found: " + removeFile.getCanonicalPath());
            }

            if (op.equals(RECURSIVE)) {
                Path directory = Paths.get(removeFile.getCanonicalPath());
                Files.walkFileTree(directory, new RecursiveFileVisitor());
            } else {
                if (!removeFile.delete()) {
                    return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR,
                            "Error while deleting " + removeFile.getCanonicalPath());
                }
            }
            return null;
        } catch (IOException ex) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, ex);
        } catch (SecurityException ex) {
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, ex);
        }
    }

    static class RecursiveFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    public static Object readDir(BString path) {
        File inputFile = Paths.get(path.getValue()).toAbsolutePath().toFile();

        if (!inputFile.exists()) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                    "File not found: " + path);
        }

        if (!inputFile.isDirectory()) {
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                    "File in path " + path + " is not a directory");
        }

        return readFileTree(inputFile);
    }

    private static Object readFileTree(File inputFile) {
        Object[] results;
        try (Stream<Path> walk = Files.walk(inputFile.toPath(), FileConstants.MAX_DEPTH)) {
            results = walk.map(x -> {
                try {
                    Object objectValue = FileUtils.getMetaData(x.toFile());
                    return objectValue;
                } catch (IOException e) {
                    throw new BallerinaException("Error while accessing file meta data", e);
                }
            }).skip(1).toArray(Object[]::new);
            return new ArrayValueImpl(results,
                    new BArrayType(new BRecordType("MetaData", FILE_PACKAGE_ID, 0, true, 0)));
        } catch (IOException | BallerinaException ex) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, ex);
        } catch (SecurityException ex) {
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, ex);
        }
    }

    public static Object copy(BString sourcePath, BString destinationPath, BString... copyOptions) {
        Path srcPath = Paths.get(sourcePath.getValue());
        Path destPath = Paths.get(destinationPath.getValue());
        List<CopyOption> options = new ArrayList<>();
        if(copyOptions.length > 0) {
            for (BString op:copyOptions) {
                if (op.getValue().equals("REPLACE_EXISTING")) {
                    options.add(StandardCopyOption.REPLACE_EXISTING);
                }
                else if (op.getValue().equals("COPY_ATTRIBUTES")) {
                    options.add(StandardCopyOption.COPY_ATTRIBUTES);
                }
                else if (op.getValue().equals("NO_FOLLOW_LINKS")) {
                    options.add(LinkOption.NOFOLLOW_LINKS);
                }
                else {
                    return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR, "Invalid copy option.");
                }
            }
        }
        CopyOption[] ops = new CopyOption[options.size()];
        ops = options.toArray(ops);
        if (Files.notExists(srcPath)) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                    "File not found: " + sourcePath);
        }
        try {
            Files.walkFileTree(srcPath, new RecursiveFileCopyVisitor(srcPath, destPath, ops));
        } catch (IOException ex) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, ex);
        }
        return null;
    }

    static class RecursiveFileCopyVisitor extends SimpleFileVisitor<Path> {

        final Path source;
        final Path target;
        final CopyOption[] copyOptions;

        RecursiveFileCopyVisitor(Path source, Path target, CopyOption... copyOptions) {
            this.source = source;
            this.target = target;
            this.copyOptions = copyOptions;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path newDirectory = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, newDirectory, copyOptions);
            } catch (Exception e) {
                log.debug(e.getMessage());
                return SKIP_SUBTREE; // skip processing
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path newFile = target.resolve(source.relativize(file));
            try {
                Files.copy(file, newFile, copyOptions);
            } catch (Exception e) {
                log.debug(e.getMessage());
                return SKIP_SUBTREE; // skip processing
            }
            return CONTINUE;
        }
    }

    public static Object createTemp(BString suffix, BString prefix, BString dir) {
        String filename = prefix.getValue() + generateName() + suffix.getValue();
        try {
            Path path;
            if (dir.getValue().equals("")) {
                String tmpDir = System.getProperty(TEMP_DIR_PROPERTY_KEY);
                path = Files.createFile(Paths.get(tmpDir, filename));
            } else {
                path = Files.createFile(Paths.get(dir.getValue(), filename));
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    File rmFile = path.toAbsolutePath().toFile();
                    rmFile.delete();
                }));
            }
            return StringUtils.fromString(path.toString());
        } catch (Exception e) {
            String msg = "Error occurred while creating temporary file";
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, msg);
        }
    }

    public static Object createTempDir(BString suffix, BString prefix, BString dir) {
        String filename = prefix.getValue() + generateName() + suffix.getValue();
        try {
            Path path;
            if (dir.getValue().equals("")) {
                String tmpDir = System.getProperty(TEMP_DIR_PROPERTY_KEY);
                path = Files.createDirectory(Paths.get(tmpDir, filename));
            } else {
                path = Files.createDirectory(Paths.get(dir.getValue(), filename));
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    File rmFile = path.toAbsolutePath().toFile();
                    rmFile.delete();
                }));
            }
            return StringUtils.fromString(path.toString());
        } catch (Exception e) {
            String msg = "Error occurred while creating temporary file";
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, msg);
        }
    }

    private static String generateName() {
        char[] filename = new char[10];
        char[] symbols = CHARS.toCharArray();
        for (int idx = 0; idx < filename.length; ++idx)
            filename[idx] = symbols[rnd.nextInt(symbols.length)];
        return new String(filename);
    }

    public static Object test(BString path, BString testOption) {
        String op = testOption.getValue();
        Path strPath = Paths.get(path.getValue());
        try {
            switch(op) {
                case "EXISTS":
                    return Files.exists(strPath);
                case "IS_DIR":
                    return Files.isDirectory(strPath);
                case "IS_SYMLINK":
                    return Files.isSymbolicLink(strPath);
                case "READABLE":
                    return Files.isReadable(strPath);
                case "WRITABLE":
                    return Files.isWritable(strPath);
                default:
                    return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR, "Unsupported test " +
                            "option.");
            }
        } catch (Exception e) {
            String msg = "Error occurred while testing file path.";
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, msg);
        }
    }
}

