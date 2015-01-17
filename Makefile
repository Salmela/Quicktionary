SRC_DIRS=src/main/java/org/quicktionary/gui/ \
         src/main/java/org/quicktionary/backend/
SRC = $(shell find $(SRC_DIRS) -name '*.java')

all:
	gcj -g0 -o quicktionary-gui --main=org.quicktionary.gui.Main --bounds-check $(SRC)
