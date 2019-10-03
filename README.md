**filesInDirectoryChecker**
filesInDirectoryChecker checks for files by file suffix in a specified directory and comperes the files against a file containing the expected list of files in that directory. 

**Usage:**

`java -jar filesInDirectoryChecker-0.0.1-SNAPSHOT-jar-with-dependencies.jar` 

`suffix= a filesuffix in format .txt`
 
`(*) check-folder-for-files=  path to a directory where look for files in.`

`(*) required-files-file= (*) path to a file containing the expected list of files in that directory`

`on-error-create-file path to a file where error information is written to`

`generate-required-files-file generate content to compare in file referenced in param3 true or false`
 
 (*) required argument