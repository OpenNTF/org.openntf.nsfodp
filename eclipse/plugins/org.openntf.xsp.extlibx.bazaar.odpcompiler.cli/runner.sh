#!/bin/sh

set -e

mvn install
cd target

export Notes_ExecDirectory="/Applications/IBM Notes.app/Contents/MacOS"
export PATH=$PATH:$Notes_ExecDirectory
export LD_LIBRARY_PATH=$Notes_ExecDirectory
export DYLD_LIBRARY_PATH=$Notes_ExecDirectory

java -Djava.library.path="$Notes_ExecDirectory" -jar org.openntf.xsp.extlibx.bazaar.odpcompiler.cli-2.0.0-SNAPSHOT.jar \
	-notesBin "/Volumes/Terminus-VCC C/notes" \
	-pluginsDir "/Users/jesse/Documents/Java/IBM/Notes9.0.1fp10/plugins" \
	-odp "/Users/jesse/Projects/SourceTree/endeavor/nsf/nsf-dashboard" \
	-updateSite "/Users/jesse/Projects/SourceTree/endeavor/endeavour-plugin/releng/net.cmssite.endeavour60.updatesite/target/site"