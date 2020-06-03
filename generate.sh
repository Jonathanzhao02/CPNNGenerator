#!/bin/bash
javac *.java

for i in {1..100}
do
	java Main FILE=$i ANIM=false MIN=true SAVE=true
done

bash enhance.sh
