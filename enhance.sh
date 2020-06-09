#!/bin/bash

for i in {1..100}
do
	java Main LOAD=$i ANIM=false MIN=true SAVE=true FILE=${i}hi400 TILES=400
	java Main LOAD=$i ANIM=false MIN=true SAVE=true FILE=${i}hi800 TILES=800
	java Main LOAD=$i ANIM=false MIN=true SAVE=true FILE=${i}hi1600 TILES=1600 RES=1600
	java Main LOAD=$i ANIM=false MIN=true SAVE=true FILE=${i}hi3200 TILES=3200 RES=3200
done