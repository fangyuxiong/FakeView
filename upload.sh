#!/usr/bin/env bash
b_dev=dev
b_git=release-github
b_mm=release-mm

function checkBranch {
    git checkout $1
    git merge ${b_dev} < merge
}


checkBranch $b_git