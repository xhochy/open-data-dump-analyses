#!/usr/bin/env python

import csv
import gzip
import re
import sys

relation = re.compile("<([^>]*)>\s+<http://rdf.freebase.com/ns/music.artist.origin>\s+<([^>]*)>")

with gzip.open(sys.argv[1]) as f:
    with gzip.open(sys.argv[2], 'wb') as csvfile:
        writer = csv.writer(csvfile, delimiter=',')
        for line in f:
            m = relation.match(line)
            if m is not None:
                artist_id = m.group(1)
                origin_id = m.group(2)
                writer.writerow([artist_id, origin_id])

