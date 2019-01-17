#!/bin/bash

cd ZimInput

cat ../Madrid_Public_Data/*007*csv | gawk -f ../Convert_007_files_to_zim.awk 
