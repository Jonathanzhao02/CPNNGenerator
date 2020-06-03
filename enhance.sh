#!/bin/bash
javac *.java

for i in {1..100}
do
	java Main LOAD=$i ANIM=false MIN=true SAVE=true FILE=${i}hi400 TILES=400
	java Main LOAD=$i ANIM=false MIN=true SAVE=true FILE=${i}hi800 TILES=800
done