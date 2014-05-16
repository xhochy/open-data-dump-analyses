#!/usr/bin/env python3

import csv
import gzip
import re
import sys

# Matches a artist-location relation, first gourp is the artist id, second the origin id
origin_relation = re.compile(r"<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>\s+<http://rdf.freebase.com/ns/music.artist.origin>\s+<([^>]*)>")

ids = set()
# Read the freebase dump line-by-line and match an artist based on having a
# music.artist.origin relation.
with gzip.open(sys.argv[1], 'rt') as freebase:
    for row in freebase:
        m = origin_relation.match(row)
        if m:
            ids.add(m.group(1))

print("# Parsed {0} artist ids.".format(len(ids)))

# Write out arist ids (one line per id)
with gzip.open(sys.argv[2], 'wt') as idfile:
    for id in ids:
        print(id, file=idfile)

