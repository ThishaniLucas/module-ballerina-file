// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

# Returns the current working directory.
# ```ballerina
# string dirPath = file:getCurrentDir();
# ```
# 
# + return - Current working directory or else an empty string if the current working directory cannot be determined
public isolated function getCurrentDir() returns string = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "getCurrentDirectory"
} external;

# Creates a new directory with the specified file name.
# If the `parentDirs` flag is true, it creates a directory in the specified path with any necessary parents.
# ```ballerina
# string | error results = file:createDir("foo/bar");
# ```
#
# + dir - Directory name
# + option - Indicates whether the `createDir` should create non-existing parent directories
# + return - Absolute path value of the created directory or else an `file:Error` if failed
public function createDir(@untainted string dir, DirOption option = "NON_RECURSIVE") returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "createDir"
} external;

# Removes the specified file or directory.
# If the recursive flag is true, it removes the path and any children it contains.
# ```ballerina
# file:Error? results = file:remove("foo/bar.txt");
# ```
#
# + path - String value of the file/directory path
# + option - Indicates whether the `remove` should recursively remove all the files inside the given directory
# + return - An `file:Error` if failed to remove
public function remove(@untainted string path, DirOption option = "NON_RECURSIVE") returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "remove"
} external;

# Renames(Moves) the old path with the new path.
# If the new path already exists and it is not a directory, this replaces the file.
# ```ballerina
# file:error? results = file:rename("/A/B/C", "/A/B/D");
# ```
#
# + oldPath - String value of the old file path
# + newPath - String value of the new file path
# + return - An `file:Error` if failed to rename
public function rename(@untainted string oldPath, @untainted string newPath) returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "rename"
} external;

# Creates a file in the specified file path.
# Truncates if the file already exists in the given path.
# ```ballerina
# string | error results = file:create("bar.txt");
# ```
#
# + path - String value of the file path
# + return - Absolute path value of the created file or else an `file:Error` if failed
public function create(@untainted string path) returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "createFile"
} external;

# Returns the metadata information of the file specified in the file path.
# ```ballerina
# file:MetaData | error result = file:getMetaData("foo/bar.txt");
# ```
#
# + path - String value of the file path.
# + return - The `MetaData` instance with the file metadata or else an `file:Error`
public isolated function getMetaData(@untainted string path) returns MetaData|Error = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "getMetaData"
} external;

# Reads the directory and returns a list of files and directories 
# inside the specified directory.
# ```ballerina
# file:MetaData[] | error results = file:readDir("foo/bar");
# ```
#
# + path - String value of the directory path.
# + return - The `MetaData` array or else an `file:Error` if there is an error while changing the mode.
public function readDir(@untainted string path) returns MetaData[]|Error = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "readDir"
} external;

# Copy the file/directory in the old path to the new path.
# If a file already exists in the new path, this replaces that file.
# ```ballerina
# file:Error? results = file:copy("/A/B/C", "/A/B/D", true);
# ```
#
# + sourcePath - String value of the old file path
# + destinationPath - String value of the new file path
# + options - Parameter to denote how the copy operation should be done
# + return - An `file:Error` if failed to copy
public function copy(@untainted string sourcePath, @untainted string destinationPath,
                     CopyOption... options) returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "copy"
} external;

# ```ballerina
# string|error tmpFile = file:createTemp();
# ```
#
# + suffix - Optional file suffix
# + prefix - Optional file prefix
# + dir - The directory path where the temp file should be created. If not specified, temp file will be created in the default temp directory of the OS.
# + return - Temporary file path or an error if one occured
public function createTemp(string suffix = "", string prefix = "", string dir  = "")
                                 returns string|Error = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "createTemp"
} external;

# Creates a temporary directory.
# ```ballerina
# string|error tmpDir = file:createTempDir();
# ```
#
# + suffix - Optional directory suffix
# + prefix - Optional directory prefix
# + dir - The directory path where the temp directory should be created. If not specified, temp directory will be created in the default temp directory of the OS.
# + return - Temporary directory path or an error if one occured
public function createTempDir(string suffix = "", string prefix = "", string dir  = "")
                                 returns string|Error = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "createTempDir"
} external;

# Tests a file path against a test condition .
# ```ballerina
# boolean|error result = check file:test("foo/bar.txt", file:EXISTS);
# ```
#
# + path - String value of the file path
# + testOption - The option to be tested upon the path (whether exists, readable, etc)
# + return - True/false depending on the option to be tested, or else an error if one occurs
public function test(@untainted string path, TestOption testOption) returns boolean|Error = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "test"
} external;
