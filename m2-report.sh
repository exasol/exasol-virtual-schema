#!/bin/bash

function section () {
    echo -e "\n--------------------------------------------------"
    echo -e "$1:\n"
}

function cygwin-path () {
    if [[ $(uname) == CYGWIN* ]]; then
	tr '\\' '/' | sed -e 's|^[cC]:|/cygdrive/c|'
    else
	cat
    fi
}

function xml-content () {
    sed -E "s/.*>(.*)<.*/\1/"
}

function version-of () {
    local dependency=$1
    grep -A 1 $dependency | tail -n 1 | sed -E "s/.*>(.*)<.*/\1/"
}

function effective-pom () {
    local args=""
    if [ -n "$1" ]; then args="-f $1"; fi
    local output="$2"
    if [ -n "$output" ]; then
	mvn -q help:effective-pom $args -Doutput=$output
    else
	output=effective-pom-temp-file.xml
	mvn -q help:effective-pom $args -Doutput=$output
	if [ -n "$1" ]; then
	    output=$(dirname $(echo $1 | cygwin-path))/$output
	    fi
	cat $output
	rm $output
    fi
}

function local-m2-repo () {
    if [ -z "$LOCAL_M2" ]; then
	LOCAL_M2=$(
	    mvn -q help:evaluate \
		-DforceStdout \
		-Dexpression=settings.localRepository \
		| tr '\\' '/' )
	LOCAL_M2_CYGWIN=$(echo $LOCAL_M2 | cygwin-path)
    fi
    if [ "$1" == "-c" ]; then
	echo $LOCAL_M2_CYGWIN
    else
	echo $LOCAL_M2
    fi
}

# function m2 () {
#     local R=$(mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout)
#     R=$(cygwin-path $R)
#     g=$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout | tr . / )
#     a=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout | tr . /)
#     echo $R/$g/$a
# }

# # ls -l $(m2-path org.apache.maven.plugins:maven-clean-plugin:2.5)
# function m2-path () {
#     local A=(${1//:/ })
#     local group=${A[0]//./\/}
#     local artifact=${A[1]//./\/}
#     local version=${A[2]}
#     echo $(local-m2-repo)/$group/$artifact/$version
# }

# local-pom org.apache.maven.plugins:maven-clean-plugin:2.5
function local-pom () {
    local args=""
    if [ "$1" == "-c" ]; then args=$1; shift; fi
    local A=(${1//:/ })
    local group=${A[0]//./\/}
    local artifact=${A[1]//./\/}
    local version=${A[2]}
    echo $(local-m2-repo $args)/$group/$artifact/$version/$artifact-$version.pom
}

# mvn-effective-url org.apache.maven.plugins:maven-clean-plugin:2.5
function mvn-effective-url () {
    local A=(${1//:/ })
    local artifact=${A[1]//./\/}
    local pom=$(local-pom $1)
    effective-pom $pom | grep -A 1 "<descr" | grep "<url>" | xml-content
}

# parent-url org.apache.maven.plugins:maven-clean-plugin:2.5
function parent-pom () {
    local args=""
    if [ "$1" == "-c" ]; then args=$1; shift; fi
    local pom=$(local-pom -c $1)
    local parent=$(grep -A 3 "<parent" $pom)
    local group=$(echo "$parent" | grep "<groupId>" | xml-content)
    local artifact=$(echo "$parent" | grep "<artifactId>" | xml-content)
    local version=$(echo "$parent" | grep "<version>" | xml-content)
    local-pom $args $group:$artifact:$version
}

# mvn-project-url $(parent-pom -c org.apache.maven.plugins:maven-clean-plugin:2.5)
function mvn-project-url () {
    grep "<descr" -A 1 $1 | grep "<url>" | xml-content
}

function pom-xpath () {
    echo -e "setns pom=http://maven.apache.org/POM/4.0.0\nxpath $1"  | xmllint --shell $2
}

function experimental () {
    # describe a specific plugin
    # does not include license information or URL
    mvn help:describe -Dplugin=org.apache.maven.plugins:maven-clean-plugin -Ddetail

    # write file target/generated-sources/license/THIRD-PARTY.txt
    # does not include plugins
    mvn license:add-third-party

    # write report about plugins to file target/site/plugins.html
    mvn project-info-reports:plugins

    # write report about plugins to file target/site/licenses.html
    mvn project-info-reports:licenses

    # xmllint --xpath "//xpath[artifactId/text() = maven-clean-plugin]/version/text()" pom.xml
    # xmllint --xpath "//dependency[./artifactId/text() = 'exasol-jdbc']" pom.xml
    # xmllint --xpath "/books" books.xml
    # pom-xpath "//pom:dependency[pom:artifactId/text() = 'maven-clean-plugin']" pom.xml

    # pom-xpath "//pom:plugin[pom:artifactId/text() = 'maven-clean-plugin']/pom:version/text()" effective-pom.xml
    # effective-pom pom.xml effective-pom.xml
}


# --------------------------------------------------
echo -e "Content of file dependencies.md:\n"
grep -i "maven.clean.plugin" dependencies.md | sed -e "s/  */ /g"
echo ""

echo "- location of local maven repository (= cache):" $(local-m2-repo)

V=$(effective-pom | version-of maven-clean-plugin)
echo "- plugin version from effective pom: $V"

PLUGIN=org.apache.maven.plugins:maven-clean-plugin:$V

echo "- plugin URL from plugin's effective POM:" \
$(mvn-effective-url $PLUGIN)

PARENT=$(parent-pom $PLUGIN)
echo "- location of pom for <parent> in local repository:" $PARENT
echo "- URL of parent:" $(mvn-project-url $(echo $PARENT | cygwin-path))
