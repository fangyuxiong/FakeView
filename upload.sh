#!/usr/bin/env bash
b_dev=dev
b_git=release_github
b_mm=release_mm

function checkBranch {
    git checkout $1
    git merge ${b_dev} < merge
}