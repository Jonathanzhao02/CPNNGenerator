#!/bin/bash
mkdir patterns
javac *.java

for i in $(eval echo $1)
do
	java Main FILE=${i} ANIM=false MIN=true SAVE=true
done

./enhance.sh $1 400
