#!/bin/bash

for i in $(eval echo $1)
do
	java Main LOAD=${i} ANIM=false MIN=true SAVE=true FILE=${i}hi${2} TILES=${2} RES=${2}
done
