#!/bin/bash
set -x #echo on
set -e #fail fast

if [[ -z "$1" ]] ; then
    echo "Version M.m.p is mandatory"
    exit 1
fi

VERSION=$1
mvn versions:set -DnewVersion=$VERSION
mvn clean install
mvn deploy -Prelease
mvn versions:commit
git add .
git commit -m "Release $VERSION"
git tag -a v$VERSION -m "v$VERSION"
mvn versions:set -DnewVersion=1.0-SNAPSHOT
git add .
git commit -m "Prepare for next development iteration"
