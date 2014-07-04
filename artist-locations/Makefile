JVM_OPTS ?= -Xmx5G
DATADIR ?= /mnt/newspace/data/freebase
DUMP_FILE ?= $(DATADIR)/freebase-rdf-2014-03-23-00-00.gz
ARTIST_IDS_FILE = $(DATADIR)/artist-ids.gz
ARIST_NAMES_FILE = $(DATADIR)/artist-names.gz
LOCATION_IDS_FILE = $(DATADIR)/locations-ids.gz
LOCATION_NAMES_FILE = $(DATADIR)/locations-names.gz
LOCATION_TYPES_FILE = $(DATADIR)/locations-types.gz
LOCATION_HIERACY_FILE = $(DATADIR)/locations-hierachy.gz
CLASSPATH = target/scala-2.11/artist-location-extraction-assembly-0.1.0.jar

# 1: Extract all artist ids and originsfrom Freebase
$(ARTIST_IDS_FILE): $(DUMP_FILE)
	./extract-artist-ids.py $(DUMP_FILE) $(ARTIST_IDS_FILE)

artist_ids: $(ARTIST_IDS_FILE)

# 2: Extract all artist names from Freebase
$(ARIST_NAMES_FILE): $(DUMP_FILE) $(ARTIST_IDS_FILE)
	./extract-artist-names.py $(DUMP_FILE) $(ARTIST_IDS_FILE) $(ARIST_NAMES_FILE)

artist_names: $(ARIST_NAMES_FILE)

# 3: Extract all (possibly) relevant location ids
$(LOCATION_IDS_FILE): $(DUMP_FILE)
	./extract-relevant-locations.py $(DUMP_FILE) $(LOCATION_IDS_FILE)

location_ids: $(LOCATION_IDS_FILE)

# 4: Extract information about these locations from Freebase
$(LOCATION_NAMES_FILE) $(LOCATION_TYPES_FILE): $(DUMP_FILE) $(LOCATION_IDS_FILE)
	java $(JVM_OPTS) -cp $(CLASSPATH) com.xhochy.freebase.ExtractLocationInfo $(DUMP_FILE) $(LOCATION_IDS_FILE) $(LOCATION_NAMES_FILE) $(LOCATION_TYPES_FILE)

location_names: $(LOCATION_NAMES_FILE)

# 5: Extract all location parent relations
# java -Xmx5G -cp "${CLASSPATH}" com.xhochy.freebase.ExtractLocationHierachy "${DUMP_FILE}" "${LOCATION_IDS_FILE}" "${LOCATION_HIERACHY_FILE}"



