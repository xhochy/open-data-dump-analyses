#!/usr/bin/env python

import csv
import gzip
import re
import sys
import yaml

from yaml import load, dump
try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper

# <http://rdf.freebase.com/ns/m.03d_2z>   <http://rdf.freebase.com/ns/type.object.name>   "Bloc Party"@da .

identifier = re.compile('http://rdf.freebase.com/ns/m\.(.*)')
namerow = re.compile('<http://rdf.freebase.com/ns/m.([^>]*)>\s+<http://rdf.freebase.com/ns/type.object.name>')

ids = {}

with gzip.open(sys.argv[1], 'rb') as idfile:
    reader = csv.reader(idfile)
    for row in reader:
        m = identifier.match(row[0])
        ids[m.group(1)] = []

with gzip.open(sys.argv[2], 'rb') as f:
    for line in f:
        mid = namerow.match(line)
        if mid:
            id = mid.group(1)
            if id in ids:
                name = line.replace(mid.group(0),"").strip().rstrip('.').strip()
                ids[id].append(name)

uniqueids = {}
for key, values in ids.items():
    parsed_values = [(v.rsplit("@")[1], v.rsplit("@")[0][1:-1].strip()) for v in values]
    langs = {}
    for value in parsed_values:
        langs[value[0]] = value[1]
    if "uk" in langs:
        uniqueids[key] = langs["uk"]
    elif "us" in langs:
        uniqueids[key] = langs["us"]
    elif "en" in langs:
        uniqueids[key] = langs["en"]
    else:
        print("We did not find a matching language for '{0}', only these: {1}".format(key, langs.keys()))

with gzip.open(sys.argv[3], 'wb') as f:
    f.write(yaml.dump(uniqueids, Dumper=Dumper))
