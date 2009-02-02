Brief notes on 'javapkg' files
------------------------------
(Russ Lenth - Feb 2, 2009)

* The 'src' directory tree contains source files for piface. The folders are (unless I screwed-up) organized in the same way as the java packages -- e.g., the source files for the package rvl.piface.apps are in rvl/piface/apps.

* The 'classes' directory contains the java classes.  These were built using the command (with 'javapkg' as the cuurrent directory):

    javac -g -d classes -cp classes -target 1.5 @src-files 

Here, the file 'src-files' contains a list of all the .java sources. The script 'make-src-files-list' will rebuild this file. (Sorry, I am not a good user of makefiles -- I need to reform but it hasn't happened yet.) Currently, there are warnings with this compilation, due to support for the ancient JDK1.2 and even 1.1.  We should modernize all the code and move toward 1.5 source/target specifications.  This will involve changing a lot of Vector classes and such to generic types (e.g. Vector<String>), rather than casting to the required types; removing support for AWT1.1 models; etc.

(It seems like the command "javac @javac-args" should work; but it doesn't.)

* The 'resources' directory contains resources used by these classes.  Currently, each app expects these resources to be in the same location as its .class file.  We should probably change this, but for now, any changes to these these need also to be copied to the directories classes/rvl/piface and classes/rvl/piface/apps.

* 'javadoc' contains the generated documentation.  It is created using

    javadoc -d javadoc @src-files

where 'src-files' is the same as used in the compile procedure.  Currently, I note there were several warnings in running this, due to incorrect @key names, etc.  So these need to be fixed too.

    