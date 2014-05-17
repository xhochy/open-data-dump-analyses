#!/bin/sh

DUMP_FILE=$1
ARTIST_IDS_FILE="artist-ids.gz"
ARIST_NAMES_FILE="artist-names.gz"

# 1: Extract all artist ids and originsfrom Freebase
./extract-artist-ids.py "${DUMP_FILE}" artist-ids.gz
# 2: Extract all artist names from Freebase
./extract-artist-names.py "${DUMP_FILE}" "${ARTIST_IDS_FILE}" "${ARIST_NAMES_FILE}"
# 3: Extract all artist locations and their parents from Freebase
# TODO
# 4: Extract information about these locations from Freebase
# TODO
