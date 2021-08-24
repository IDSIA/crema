BASEDIR=$(CURDIR)
OUTPUTDIR=$(BASEDIR)/javadoc
PACKAGE=ch.idsia.crema

html:
	javadoc "$(PACKAGE)" -d "$(OUTPUTDIR)" -encoding UTF-8

.PHONY: html
