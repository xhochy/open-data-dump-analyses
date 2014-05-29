#!/bin/sh

DUMP_FILE=$1
ARTIST_IDS_FILE="artist-ids.gz"
ARIST_NAMES_FILE="artist-names.gz"
LOCATION_IDS_FILE="locations-ids.gz"
LOCATION_NAMES_FILE="locations-names.gz"
LOCATION_TYPES_FILE="locations-types.gz"
LOCATION_HIERACY_FILE="locations-hierachy.gz"
CLASSPATH="target/scala-2.11/artist-location-extraction-assembly-0.1.0.jar"

# 1: Extract all artist ids and originsfrom Freebase
# ./extract-artist-ids.py "${DUMP_FILE}" "${ARTIST_IDS_FILE}"
# 2: Extract all artist names from Freebase
# ./extract-artist-names.py "${DUMP_FILE}" "${ARTIST_IDS_FILE}" "${ARIST_NAMES_FILE}"
# 3: Extract all (possibly) relevant location ids
# ./extract-relevant-locations.py "${DUMP_FILE}" "${LOCATION_IDS_FILE}"
# 4: Extract information about these locations from Freebase
java -Xmx5G -cp "${CLASSPATH}" com.xhochy.freebase.ExtractLocationInfo "${DUMP_FILE}" "${LOCATION_IDS_FILE}" "${LOCATION_NAMES_FILE}" "${LOCATION_TYPES_FILE}"
# 5: Extract all location parent relations
# java -Xmx5G -cp "${CLASSPATH}" com.xhochy.freebase.ExtractLocationHierachy "${DUMP_FILE}" "${LOCATION_IDS_FILE}" "${LOCATION_HIERACHY_FILE}"
