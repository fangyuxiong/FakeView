#!/usr/bin/env bash

h_file=".h"
debug_path="/build/intermediates/classes/debug"
this_dir=`pwd`

function inform {
    echo "Usage: ./generate_h.sh -m moduleName -c className -p outPath"
}

function generate_h_file {
    module=$1
    class_name=$2
    out_path=$3

    cd ${module}${debug_path}
    javah -classpath . -jni ${class_name}
    cd ${this_dir}
    mkdir -p ${out_path}
    mv ${module}${debug_path}/${class_name//\./_}${h_file} ${out_path}
}

mn="null"
cn="null"
op="null"
while getopts "m:c:p:" optname
    do
        case "$optname" in
            "m")
            echo "module: $OPTARG"
            mn=$OPTARG
            ;;
            "c")
            echo "class name: $OPTARG"
            cn=$OPTARG
            ;;
            "p")
            echo "out path: $OPTARG"
            op=$OPTARG
            ;;
            *)
            echo "Unknown error while processing options $optname $OPTARG"
            ;;
        esac
done

if [ ${mn} = "null" ]; then
    inform
    exit
fi
if [ ${cn} = "null" ]; then
    inform
    exit
fi
if [ ${op} = "null" ]; then
    inform
    exit
fi
generate_h_file ${mn} ${cn} ${op}