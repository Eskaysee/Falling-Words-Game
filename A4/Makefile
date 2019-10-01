BINDIR=./bin
SRCDIR=./src
DOCDIR=./doc


src_files=$(wildcard $(SRCDIR)/*.java)
out_files=$(src_files:$(SRCDIR)/%.java=$(BINDIR)/%.class)

compiler=javac
compiler_flags=-d $(BINDIR) -sourcepath $(SRCDIR)

build: $(out_files)

$(BINDIR)/%.class: $(SRCDIR)/%.java
	$(compiler) $(compiler_flags) $^

clean:
	rm -f ${BINDIR}/*.class

docs:
	javadoc  -classpath ${BINDIR} -d ${DOCDIR} ${SRCDIR}/*.java

cleandocs:
	rm -rf ${DOCDIR}/*


