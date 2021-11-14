#!/bin/bash

RES=$2

if [ $# -lt 2 ]; then
    RES=400
fi

for i in $(eval echo $1)
do
	java Main LOAD=${i} ANIM=false MIN=true SAVE=true FILE=${i}_${RES} TILES=${RES} RES=${RES}
done
