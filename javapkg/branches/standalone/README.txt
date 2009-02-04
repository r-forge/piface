Brief notes on 'javapkg' files
------------------------------
(Russ Lenth - Feb 2, 2009)

* The 'src' directory tree contains source files for piface. The folders are (unless I screwed-up) organized in the same way as the java packages -- e.g., the source files for the package rvl.piface.apps are in rvl/piface/apps.

* Originally, I included the classes directory with the java class files.  This turns out to be useless because a working copy of it contains a whole lot of .svn directories that you don't want in the .jar file.  Thus, I deleted that directory.  Make it in your own working copy.


* The 'resources' directory contains resources used by these classes.  Currently, each app expects these resources to be in the same location as its .class file.  We should probably change this, but for now, any changes to these these need also to be copied to the directories classes/rvl/piface and classes/rvl/piface/apps.

* 'javadoc' contains the generated documentation.  It is created using

    javadoc -d javadoc @src-files

where 'src-files' is the same as used in the compile procedure.  Currently, I note there were several warnings in running this, due to incorrect @key names, etc.  So these need to be fixed too.

* To rebuild piface.jar, do:

    ./make-src-files-list
    mkdir classes  # (if necessary)
    javac -g -d classes -cp classes -target 1.5 @src-files 
    
    cp resources/* classes/rvl/piface
    cp resources/apps/* classes/rvl/piface/apps
    jar cvf piface.jar -C classes rvl
    jar umvf src/MANIFEST.MF piface.jar
    chmod 755 piface.jar

Currently, there are warnings with the compilation, due to support for the ancient JDK1.2 and even 1.1.  We should modernize all the code and move toward 1.5 source/target specifications.  This will involve changing a lot of Vector classes and such to generic types (e.g. Vector<String>), rather than casting to the required types; removing support for AWT1.1 models; etc.

(It seems like the command "javac @javac-args" should work; but it doesn't.)

* To run the apps, use a command like:

    java -cp piface.jar rvl.piface.apps.TwoTGUI

or to run with the default app menu:

    java -jar piface.jar
