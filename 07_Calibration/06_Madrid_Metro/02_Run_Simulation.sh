#!/bin/bash

java  -jar ZimCLI.jar -razo "$@" zl=45 \
      I=ZimInput/00_StaticTypes.zim \
      I=ZimInput/_01_Stations.zim \
      I=ZimInput/_02_Tracks.zim \
      I=ZimInput/_03_Paths.zim \
      I=ZimInput/_04_Schedules.zim \
      I=ZimInput/_05_Demand.zim \
      R=out/madrid_01.zo
