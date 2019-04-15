make:
	latex main.tex
	bibtex main.aux
	latex main.tex
	dvips main.dvi
	ps2pdf main.ps
	rm -f main.aux
	rm -f main.dvi
	rm -f main.log
	rm -f main.ps
	rm -f main.toc
	rm -f main.blg
	clear
	@echo "Compilation has completed. .ps, .aux, .log, .toc, .blg and .dvi files have been deleted. main.pdf is ready to read."
