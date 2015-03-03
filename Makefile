sources = $(shell find src | grep .java$$)

script: compileall
	echo "#!/bin/sh" > ./joosc
	echo 'exec java -classpath classes Compiler.Main' \
	     ' src/Parser/joos1w.lr1 $$@' >> ./joosc
	chmod +x ./joosc

compileall: classes
	javac -source 1.6 -target 1.6 \
	      -sourcepath src \
	      -classpath classes -d classes \
	      $(sources)

classes:
	[ -e classes/ ] || mkdir classes
