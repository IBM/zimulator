
 {
     # 4,Plaza de Castilla,https://en.wikipedia.org/wiki/Plaza_de_Castilla_(Madrid_Metro)
     split($0,csv,",");
     comm="wget -q -O - \"" csv[3] "\" | ../Tractor/AssignmentExtractor - ':' '{},' '\"lat\",\"lon\"' ";
     comm | getline wiff;
     close(comm);
     print $0 "," wiff;    
 }
