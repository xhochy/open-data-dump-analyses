#!/usr/bin/env python

import csv
import gzip
import re
import sys
import yaml

try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper

# http://rdf.freebase.com/ns/location.location.containedby
# http://rdf.freebase.com/ns/type.object.type
# * http://rdf.freebase.com/ns/location.citytown
# * http://rdf.freebase.com/ns/location.country
# <http://rdf.freebase.com/ns/m.03d_2z>   <http://rdf.freebase.com/ns/type.object.name>   "Bloc Party"@da .

identifier = re.compile('http://rdf.freebase.com/ns/m\.(.*)')
idmatch = re.compile('<http://rdf.freebase.com/ns/m.([^>]*)>\s+')
objecttype = re.compile('<http://rdf.freebase.com/ns/m.([^>]*)>\s+<http://rdf.freebase.com/ns/type.object.type>\s+<http://rdf.freebase.com/ns/([^>]*)>')
containedby = re.compile('<http://rdf.freebase.com/ns/m.([^>]*)>\s+<http://rdf.freebase.com/ns/location.location.containedby>\s+<http://rdf.freebase.com/ns/m\.([^>]*)>')
objectname = re.compile('<http://rdf.freebase.com/ns/m.([^>]*)>\s+<http://rdf.freebase.com/ns/type.object.name>\s+"([^"]*)"@([^\s]*)')

locations = {}
next_locations = {}
all_locations = {}

def resolve_name(langs):
    #if "uk" in langs:
    #    return langs["uk"]
    if "us" in langs:
        return langs["us"]
    elif "en" in langs:
        return langs["en"]
    else:
        return ""

# Load all requested locations from the second column of a .csv.gz file
with gzip.open(sys.argv[1], 'rb') as idfile:
    reader = csv.reader(idfile)
    for row in reader:
        m = identifier.match(row[1])
        if m:
            locations[m.group(1)] = {"type": [], "containedby": [], "children": [], "resolved": [], "name": {}, "resolvedname": ""}

while len(locations) > 0:
    print("Iteration with {0} locations left".format(len(locations)))
    with gzip.open(sys.argv[2], 'rb') as f:
        for line in f:
            m = idmatch.match(line)
            if m:
                id = m.group(1)
                if id in locations:
                    mot = objecttype.match(line)
                    mcb = containedby.match(line)
                    mname = objectname.match(line)
                    if mot:
                        type = mot.group(2)
                        locations[id]["type"].append(type)
                    elif mcb:
                        container = mcb.group(2)
                        locations[id]["containedby"].append(container)
                    elif mname:
                        name = mname.group(2)
                        lang = mname.group(3)
                        if lang in ["uk", "en", "us"]:
                            locations[id]["name"][lang] = name

    next_locations = {}
    all_locations.update(locations)

    for k in locations.keys():
        if "location.country" in locations[k]["type"]:
            # We have reached a country, we're done.
            all_locations[k]["resolved"] = k
            all_locations[k]["resolvedname"] = resolve_name(locations[k]["name"])
            # Propagate to children
            for child in locations[k]["children"]:
                all_locations[child]["resolved"] = k
                all_locations[child]["resolvedname"] = resolve_name(locations[k]["name"])
        else:
            for parent in locations[k]["containedby"]:
                if parent in all_locations and all_locations[parent]["resolvedname"] != "":
                    all_locations[k]["resolved"] = parent
                    all_locations[k]["resolvedname"] = all_locations[parent]["resolvedname"]
                elif parent in all_locations:
                    # We need to add it as a child of all higher level parents
                    pass
                else:
                    if parent in next_locations:
                        next_locations[parent]["children"].append(k)
                    else:
                        next_locations[parent] = {"type": [], "containedby": [], "children": [k], "resolved": [], "name": {}, "resolvedname": ""}
                    next_locations[parent]["children"] += locations[k]["children"]

    locations = next_locations

print(next_locations.keys())

with gzip.open(sys.argv[3], 'wb') as f:
    writer = csv.writer(f)
    for k in all_locations.keys():
        writer.writerow([k, ";".join(all_locations[k].get("type", [])),
            ";".join(all_locations[k].get("containedby", [])),
            ";".join(all_locations[k]["children"]),
            ";".join(all_locations[k]["resolved"]),
            resolve_name(all_locations[k]["name"]),
            all_locations[k]["resolvedname"]])

