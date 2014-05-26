#!/usr/bin/env python3

import csv
import gzip
import re
import sys

# Load YAML, prefer C-implementation
from yaml import load, dump
try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper


# Matches an object-name relation, first gourp is the object id, second the artist's name
# <http://rdf.freebase.com/ns/m.03d_2z>   <http://rdf.freebase.com/ns/type.object.name>   "Bloc Party"@id .
type_name_relation = re.compile(r"<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>\s+<http:\/\/rdf.freebase.com\/ns\/type.object.name>\s+\"(.*)")
# Matches a location-location relation, both groups are locations
containedby_relation = re.compile(r'<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>\s+<http:\/\/rdf.freebase.com\/ns\/location.location.containedby>\s+<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>')
# Matches an object-type relation, first group is the object id, second the type
# <http://rdf.freebase.com/ns/m.04jpl>    <http://rdf.freebase.com/ns/type.object.type>   <http://rdf.freebase.com/ns/rail.railway_terminus>      .
type_relation = re.compile(r'<http:\/\/rdf.freebase.com\/ns\/m\.([^>]*)>\s+<http:\/\/rdf.freebase.com\/ns\/type.object.type>\s+<http:\/\/rdf.freebase.com\/ns\/([^>]*)>')

dump_file = sys.argv[1]
ids_file = sys.argv[2]
locations_file = sys.argv[3]

ids = set()
locations = {}
type_idx = 0
types = {}

# Load all location ids
with gzip.open(ids_file, 'rt') as idsf:
    for id in idsf:
        # Be careful to always stip the newline character at the end
        ids.add(id.strip())
        locations[id.strip()] = { 'contained_by': [], 'names': {}, 'types': [] }

# Read the freebase dump line-by-line and match on all known location relations
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
                locations[id]['names'][language] = name[:separator]
            continue
        m = containedby_relation.match(row)
        if m:
            id = m.group(1)
            if id in ids:
                locations[id]['contained_by'].append(m.group(2))
            continue
        m = type_relation.match(row)
        if m:
            id = m.group(1)
            if id in ids:
                type = m.group(2)
                if not(type in types):
                    types[type] = type_idx
                    type_idx += 1
                locations[id]['types'].append(types[m.group(2)])
            continue


for id in locations.keys():
    languages = set(locations[id]['names'].keys())
    # Choose languages by peference
    if 'en' in languages:
        locations[id]['name'] = locations[id]['names']['en']
    elif 'de' in languages:
        locations[id]['name'] = locations[id]['names']['de']
    else:
        if len(languages) > 0:
            print('No name found for artist id {0} with languages'.format(id, languages))
    del locations[id]['names']

with gzip.open(locations_file, 'wt') as f:
    dump(locations, f, Dumper=Dumper)

