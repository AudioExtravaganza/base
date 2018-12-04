#makefile written based on this piazza question: https://piazza.com/class/jmgye1qohf5720?cid=31

LATEX	= latex -shell-escape
BIBTEX	= bibtex
DVIPS	= dvips
DVIPDF  = dvipdft
XDVI	= xdvi -gamma 4
GH		= gv

export PATH := /usr/local/apps/tex_live/current/bin/x86_64-linux:$(PATH)

default: pdf concurrency

all: pdf concurrency zip clean

pdf:
	pdflatex -synctex=1 -interaction=nonstopmode main.tex


# Trim out the excess fat.
clean:
	rm -f *.ps *.log *.aux *.out *.dvi *.bbl *.blg *.gz *.acn *.ist *.acr *.alg *.toc *.glg *.pyg