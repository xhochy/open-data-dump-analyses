#!/usr/bin/env python3

import csv
import gzip
import re
import sys

# Matches a artist-location relation, first gourp is the artist id, second the origin id
# <http://rdf.freebase.com/ns/m.03d_2z>   <http://rdf.freebase.com/ns/type.object.name>   "Bloc Party"@id .
type_name_relation = re.compile(r"<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>\s+<http:\/\/rdf.freebase.com\/ns\/type.object.name>\s+\"(.*)")

dump_file = sys.argv[1]
ids_file = sys.argv[2]
names_file = sys.argv[3]

ids = set()
names = {}

# Load all artist ids
with gzip.open(ids_file, 'rt') as idsf:
    for id in idsf:
        # Be careful to always stip the newline character at the end
        ids.add(id.strip())
        names[id.strip()] = {}

# Read the freebase dump line-by-line and match an artist based on having a
# music.artist.origin relation.
with gzip.open(dump_file, 'rt') as freebase:
    for row in freebase:
        m = type_name_relation.match(row)
        if m:
            id = m.group(1)
            if id in ids:
                # Last two chracters are space and a dot and some more spaces
                name = m.group(2)[:-2].strip()
                # Language and name are separated by '"@'
                separator = name.rfind('"@')
                language = name[separator+2:]
                names[id][language] = name[:separator]

resolved_names = {}
for id in names.keys():
    languages = set(names[id].keys())
    # Choose languages by peference
    if 'en' in languages:
        resolved_names[id] = names[id]['en']
    elif 'de' in languages:
        resolved_names[id] = names[id]['de']
    else:
        if len(languages) > 0:
            print('No name found for artist id {0} with languages'.format(id, languages))

# We have now gathered all possible artist names and picked our preferred name,
# so save them to a ;-CSV for further use
with gzip.open(names_file, 'wt') as namesf:
    names_writer = csv.writer(namesf, delimiter=';')
    for id in resolved_names.keys():
        names_writer.writerow([id, resolved_names[id]])

