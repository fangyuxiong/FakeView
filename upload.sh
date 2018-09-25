#!/usr/bin/env bash
b_dev=dev
b_git=release-github
b_mm=release-mm

function checkBranch {
    git checkout $1
    git merge ${b_dev} < merge
}

function printError {
    echo '-----------------------------'
    echo $1' failed!!!!!!!!!!!!!!'
    echo '-----------------------------'
}

checkBranch ${b_mm}
if [ $? -ne 0 ]; then
    printError "check branch and merge"
    return 1
fi
./gradlew :fview:uploadArchives
if [ $? -ne 0 ]; then
    printError "upload"
    return 1
fi

checkBranch ${b_git}
if [ $? -ne 0 ]; then
    printError "check branch and merge"
    return 1
fi
./gradlew :fview:bintrayUpload
if [ $? -ne 0 ]; then
    printError "upload"
    return 1
fi
git checkout ${b_dev}