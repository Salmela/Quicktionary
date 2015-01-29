#!/bin/bash

folder_name=`basename "$PWD"`

if [[ "x$folder_name" != "xQuicktionary-ohja" ]]; then
	echo "You are in wrong directory"
	exit
fi

if [[ "x$1" == "xcreate" ]]; then
	mkdir Quicktionary
	mkdir javadoc
	mkdir docs
	mkdir docs/cobertura
	mkdir docs/pit

	mv Quicktionary/TIME_USAGE docs/TimeUsage.md
	mv Quicktionary/docs/manual docs/Manual.md
	class_diagram=`ls -t Quicktionary/docs/class_diagram_*.png | head -1`
	mv $class_diagram docs/
fi

if [[ "x$1" == "xremove" ]]; then
	rm -r `find . | grep -vE "create.sh|.git|Quicktionary"`
fi
