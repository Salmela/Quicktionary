#!/bin/bash

folder_name=`basename "$PWD"`

if [[ "x$folder_name" != "xQuicktionary-ohja" ]]; then
	echo "You are in wrong directory"
	exit
fi

if [[ "x$1" == "xcreate" ]]; then
	mkdir -p javadoc
	mkdir -p docs
	mkdir -p docs/cobertura
	mkdir -p docs/pit

	cp Quicktionary/.README.md ./README.md
	cp Quicktionary/.LICENSE ./LICENSE.md

	cp Quicktionary/.docs/time_usage docs/TimeUsage.md
	cp Quicktionary/.docs/topic_definition.md docs/TopicDefinition.md
	cp Quicktionary/.docs/quicktionary_classes.md docs/QuicktionaryClasses.md
	#cp Quicktionary/docs/manual docs/Manual.md

	pit_folder=`ls -t Quicktionary/target/pit-reports/ | head -1`
	cp -r Quicktionary/target/pit-reports/$pit_folder docs/pit

	cp -r Quicktionary/target/site/apidocs javadoc

	class_diagram=`ls -t Quicktionary/.docs/class_diagram_*.png | head -1`
	class_diagram=`basename $class_diagram`
	cp Quicktionary/.docs/$class_diagram docs/class_diagram.png

	sequence_diagram=`ls Quicktionary/.docs/*_sequence_diagram.png`
	cp $sequence_diagram docs/
fi

if [[ "x$1" == "xremove" ]]; then
	rm -r `find . | grep -vE "create.sh|.git|Quicktionary"`
fi
