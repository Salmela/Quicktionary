GUI_SRC=src/main/java/org/quicktionary/gui/

all:
	gcj -g0 -o quicktionary-gui --main=org.quicktionary.gui.Main --bounds-check $(GUI_SRC)/Main.java
