#!/bin/bash

java -jar ZimCLI.jar "$@" I=Testing_Pipe_Zboxen.zim R=_Testing_Pipe_Zboxen.zo

cat _Testing_Pipe_Zboxen.zo | gawk -f Report_to_ps.awk > _Testing_Pipe_Zboxen.eps

ps2pdf -dEPSCrop _Testing_Pipe_Zboxen.eps 02_Testing_Pipe_Zboxen.pdf

