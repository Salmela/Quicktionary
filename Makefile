SRC_DIRS=src/main/java/org/quicktionary/gui/ \
         src/main/java/org/quicktionary/backend/
SRC = $(shell find $(SRC_DIRS) -name '*.java')

all:
	rm -f ./gui ./backend ./tests
	ln -s ./src/main/java/org/quicktionary/gui ./gui
	ln -s ./src/main/java/org/quicktionary/backend ./backend
	ln -s ./src/test/java/org/quicktionary/ ./tests
	gcj -g -O0 -o quicktionary-gui --main=org.quicktionary.gui.Main --bounds-check $(SRC)
