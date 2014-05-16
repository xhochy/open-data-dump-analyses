#!/bin/sh

DUMP_FILE=$1

# 1: Extract all artist ids and originsfrom Freebase
./extract-artist-ids.py "${DUMP_FILE}" artist-ids.gz
# 2: Extract all artist names from Freebase
# TODO
# 3: Extract all artist locations and their parents from Freebase
# TODO
# 4: Extract information about these locations from Freebase
# TODO
