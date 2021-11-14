#!/bin/bash

RES=$2

if [ $# -lt 2 ]; then
    RES=200
fi

mkdir patterns
javac *.java

for i in $(eval echo $1)
do
	java Main FILE=${i} ANIM=false MIN=true SAVE=true TILES=${RES} RES=${RES}
done
