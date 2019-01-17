#!/bin/bash

java  -jar ZimCLI.jar -razo "$@" z=60 zl=45 \
      I=ZimInput/_00_StaticTypes.zim \
      I=ZimInput/_01_Stations.zim \
      I=ZimInput/_02_Tracks.zim \
      I=ZimInput/_03_Paths.zim \
      I=ZimInput/_04_Schedules.zim \
      I=ZimInput/_05_Demand.zim \
      R=out/madrid_08.zo
