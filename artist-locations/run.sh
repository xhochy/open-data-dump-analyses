#!/bin/sh

DUMP_FILE=$1
ARTIST_IDS_FILE="artist-ids.gz"
ARIST_NAMES_FILE="artist-names.gz"
LOCATION_IDS_FILE="locations-ids.gz"
LOCATION_INFO_FILE="locations-info.gz"

# 1: Extract all artist ids and originsfrom Freebase
# ./extract-artist-ids.py "${DUMP_FILE}" "${ARTIST_IDS_FILE}"
# 2: Extract all artist names from Freebase
# ./extract-artist-names.py "${DUMP_FILE}" "${ARTIST_IDS_FILE}" "${ARIST_NAMES_FILE}"
# 3: Extract all (possibly) relevant location ids
# ./extract-relevant-locations.py "${DUMP_FILE}" "${LOCATION_IDS_FILE}"
# 4: Extract information about these locations from Freebase
./extract-location-info.py "${DUMP_FILE}" "${LOCATION_IDS_FILE}" "${LOCATION_INFO_FILE}"
# 5: Extract all location parent relations
# TODO
