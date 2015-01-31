#!/bin/bash

folder_name=`basename "$PWD"`
if [[ "x$folder_name" != "xQuicktionary" && "x$folder_name" != "xquicktionary" ]]; then
	echo "You are in wrong directory"
	echo "You are in $folder_name"
	exit
fi

compiler=`command -v gcj /dev/null`
if [[ "x$compiler" == "x" ]]; then
	compiler=`find /usr/bin -name "gcj-[1-9]*"`
fi

read -d '' text <<EOF
SRC_DIRS=src/main/java/org/quicktionary/gui/ \\
         src/main/java/org/quicktionary/backend/
SRC = \$(shell find \$(SRC_DIRS) -name '*.java')

all:
	$compiler -g -O0 -o quicktionary-gui --main=org.quicktionary.gui.Main --bounds-check \$(SRC)
	$compiler -g -O0 -o test --main=org.quicktionary.gui.Test --bounds-check \$(SRC)
EOF

echo "$text" > Makefile
rm -f ./gui ./backend ./tests LICENSE README.md docs
ln -s ./src/main/java/org/quicktionary/gui ./gui
ln -s ./src/main/java/org/quicktionary/backend ./backend
ln -s ./src/test/java/org/quicktionary/ ./tests

ln -s .LICENSE LICENSE
ln -s .README.md README.md
ln -s .docs docs
