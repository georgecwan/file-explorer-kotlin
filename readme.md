George Wan
20948342 g8wan

kotlinc-jvm 1.8.21 (jre 17.0.4)
macOS Ventura 13.4

Code inspiration from the a1starter project
in the cs349-sample-code repository and documentation
from docs.oracle.com and kotlinlang.org.

The naming requirements for the 'rename' command depends
on the java.io.file.renameTo function, which is platform-dependent.

With the current implementation, users should not be able to navigate outside
the test directory, and they should not be able to move files outside the test
directory either.

The quit menu item will close the application and the build command will need
to be run again to restart the application.

The test folder contains an extra image which was for the purposes of testing
vertical restraints on the image preview.

The project folder was originally created as a subfolder "Explorer" inside
the a1 folder. I moved all the project files out into the a1 folder and deleted
the Explorer folder. This did not cause any issues while I was testing, so I
hope that this will not cause any issues.

While testing, a `+[CATransaction synchronize] called within transaction
` error would appear in the console when DirectoryChooser was opened. From
[this thread](https://stackoverflow.com/questions/74859461/java93422850-catransaction-synchronize-called-within-transaction-when-a),
I believe that this is a macOS issue and not an issue with my code.
