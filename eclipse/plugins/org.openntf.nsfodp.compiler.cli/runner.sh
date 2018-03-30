#!/bin/sh
#
# Copyright © 2018 Jesse Gallagher
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


set -e

mvn clean install
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