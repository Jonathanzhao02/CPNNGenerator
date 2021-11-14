#!/bin/bash

for i in $(eval echo {1..$1})
do
	java Main LOAD=$i ANIM=false MIN=true SAVE=true FILE=${i}hi400 TILES=400 RES=400
done
