#!/bin/sh

PREFIX=$1
PAGES_DUMP_FILE=$2
PAGELINKS_DUMP_FILE=$3
PAGE_REVERSE_INDEX_FILE="${PREFIX}-page-reverse-index.lz4"
INTRALINK_GRAPH_EDGELIST="${PREFIX}-intralink-graph-edgelist.lz4"
INTRALINK_GRAPH_ADJACENCYARRAY="${PREFIX}-intralink-graph-adjacencyarray"
CLASSPATH="target/scala-2.11/intrawiki-link-graph-assembly-0.1.0.jar"

# 1: Parse the page table and create a mapping ((namespace, title) => id)
java -Xmx5G -cp "${CLASSPATH}" com.xhochy.wikipedia.PageReverseIndex "${PAGES_DUMP_FILE}" "${PAGE_REVERSE_INDEX_FILE}"
# 2: Parse the pagelinks table and create a edge list represented graph
java -Xmx5G -cp "${CLASSPATH}" com.xhochy.wikipedia.IntraLinkGraph "${PAGELINKS_DUMP_FILE}" "${PAGE_REVERSE_INDEX_FILE}" "${INTRALINK_GRAPH_EDGELIST}"
# 3: We want to work on adjacency arrays instead of edge lists
java -Xmx5G -cp "${CLASSPATH}" com.xhochy.EdgeListToAdjacencyArray "${INTRALINK_GRAPH_EDGELIST}" "${INTRALINK_GRAPH_ADJACENCYARRAY}"
