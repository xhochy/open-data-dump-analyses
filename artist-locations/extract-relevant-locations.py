#!/usr/bin/env python3

import csv
import gzip
import re
import sys

# Matches an artist-location relation, first group is the artist id, second the origin id
# <http://rdf.freebase.com/ns/m.03d_2z>   <http://rdf.freebase.com/ns/music.artist.origin>        <http://rdf.freebase.com/ns/m.04jpl>
music_artist_origin_relation = re.compile(r"<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>\s+<http:\/\/rdf.freebase.com\/ns\/music.artist.origin>\s+<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>")
# Matches a location-location relation, both groups are locations
containedby_relation = re.compile(r'<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>\s+<http:\/\/rdf.freebase.com\/ns\/location.location.containedby>\s+<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>')

dump_file = sys.argv[1]
ids_file = sys.argv[2]

ids = set()
# Read the freebase dump line-by-line and match on all known location relations
with gzip.open(dump_file, 'rt') as freebase:
    for row in freebase:
        m = music_artist_origin_relation.match(row)
        if m:
            ids.add(m.group(2))
            continue
        m = containedby_relation.match(row)
        if m:
            ids.add(m.group(1))
            ids.add(m.group(2))
            continue

print("# Parsed {0} location ids.".format(len(ids)))

# Write out location ids (one line per id)
with gzip.open(ids_file, 'wt') as idfile:
    for id in ids:
        print(id, file=idfile)


