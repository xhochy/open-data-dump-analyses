PREFIX ?= en
JVM_OPTS ?= -Xmx5G
DATADIR ?= /mnt/newspace/data
DATE_STR = latest
PAGES_DUMP_FILE = $(DATADIR)/$(PREFIX)wiki-$(DATE_STR)-page.sql.gz
PAGELINKS_DUMP_FILE= $(DATADIR)/$(PREFIX)wiki-$(DATE_STR)-pagelinks.sql.gz
PAGELINKS_TRIPLE = $(DATADIR)/$(PREFIX)wiki-$(DATE_STR)-pagelinks.bz2
PAGE_REVERSE_INDEX_FILE = $(DATADIR)/$(PREFIX)-page-reverse-index.lz4
PAGE_TRIPLE_FILE = $(DATADIR)/$(PREFIX)-page-triple.bz2
INTRALINK_GRAPH_EDGELIST = $(DATADIR)/$(PREFIX)-intralink-graph-edgelist.lz4
INTRALINK_GRAPH_ADJACENCYARRAY = $(DATADIR)/$(PREFIX)-intralink-graph-adjacencyarray
INTRALINK_GRAPH_ADJACENCYARRAY_PARQUET = $(DATADIR)/$(PREFIX)-intralink-graph-adjacencyarray.parquet
CATEGORY_HISTOGRAM = $(DATADIR)/$(PREFIX)-category-histogram.yaml.lz4
CATEGORY_NAMES = $(DATADIR)/$(PREFIX)-category-names.yaml.lz4
CATEGORY_TOP10_INTRALINK_GRAPH_EDGELIST = $(DATADIR)/$(PREFIX)-category-top10-intralink-graph-edgelist.lz4
CATEGORY_TOP10_INTRALINK_GRAPH_ADJACENCYARRAY = $(DATADIR)/$(PREFIX)-category-top10-intralink-graph-adjacencyarray
CATEGORY_ARTICLE_INTRALINK_GRAPH_EDGELIST = $(DATADIR)/$(PREFIX)-category-article-intralink-graph-edgelist.lz4
CATEGORY_ARTICLE_INTRALINK_GRAPH_ADJACENCYARRAY = $(DATADIR)/$(PREFIX)-category-article-intralink-graph-adjacencyarray
CLASSPATH = target/scala-2.11/wikipedia-dump-analysis-assembly-0.1.0.jar

# Parse the page table and create a mapping ((namespace, title) => id)
$(PAGE_REVERSE_INDEX_FILE): $(PAGES_DUMP_FILE)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.wikipedia.PageReverseIndex "$(PAGES_DUMP_FILE)" "$(PAGE_REVERSE_INDEX_FILE)"

# Parse the page table and create a file with the triples (namespace_id, id, base64(title))
$(PAGE_TRIPLE_FILE): $(PAGES_DUMP_FILE)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.wikipedia.PageTriples "$(PAGES_DUMP_FILE)" "$(PAGE_TRIPLE_FILE)"

pagereverseindex: $(PAGE_REVERSE_INDEX_FILE)

pagetriples: $(PAGE_TRIPLE_FILE)

# Parse the pagelinks table and create a edge list represented graph
$(INTRALINK_GRAPH_EDGELIST): $(PAGE_REVERSE_INDEX_FILE) $(PAGELINKS_DUMP_FILE)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.wikipedia.IntraLinkGraph "$(PAGELINKS_DUMP_FILE)" "$(PAGE_REVERSE_INDEX_FILE)" "$(INTRALINK_GRAPH_EDGELIST)"

$(PAGELINKS_TRIPLE): $(PAGELINKS_DUMP_FILE)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.wikipedia.PageLinksTriple "$(PAGELINKS_DUMP_FILE)" "$(PAGELINKS_TRIPLE)"

pagelinks: $(PAGELINKS_TRIPLE)

# We want to work on adjacency arrays instead of edge lists
$(INTRALINK_GRAPH_ADJACENCYARRAY): $(INTRALINK_GRAPH_EDGELIST)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.EdgeListToAdjacencyArray "$(INTRALINK_GRAPH_EDGELIST)" "$(INTRALINK_GRAPH_ADJACENCYARRAY)"

$(INTRALINK_GRAPH_ADJACENCYARRAY_PARQUET): $(INTRALINK_GRAPH_EDGELIST)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.EdgeListToAdjacencyArrayParquet "$(INTRALINK_GRAPH_EDGELIST)" "$(INTRALINK_GRAPH_ADJACENCYARRAY_PARQUET)"

intralinkgraph: $(INTRALINK_GRAPH_EDGELIST) $(INTRALINK_GRAPH_ADJACENCYARRAY) $(INTRALINK_GRAPH_ADJACENCYARRAY_PARQUET)

$(CATEGORY_HISTOGRAM): $(PAGES_DUMP_FILE) $(PAGE_REVERSE_INDEX_FILE) $(PAGELINKS_DUMP_FILE)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.wikipedia.CategoryHistogram "$(PAGES_DUMP_FILE)" "$(PAGE_REVERSE_INDEX_FILE)" "$(PAGELINKS_DUMP_FILE)" "$(CATEGORY_HISTOGRAM)" "${CATEGORY_NAMES}"

categoryhistogram: $(CATEGORY_HISTOGRAM)

$(CATEGORY_TOP10_INTRALINK_GRAPH_EDGELIST): $(CATEGORY_HISTOGRAM) $(PAGE_REVERSE_INDEX_FILE) $(PAGELINKS_DUMP_FILE)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.wikipedia.CategoryTop10IntraLinkGraph "$(CATEGORY_HISTOGRAM)" "$(PAGE_REVERSE_INDEX_FILE)" "$(PAGELINKS_DUMP_FILE)" "$(CATEGORY_TOP10_INTRALINK_GRAPH_EDGELIST)"

$(CATEGORY_TOP10_INTRALINK_GRAPH_ADJACENCYARRAY): $(CATEGORY_TOP10_INTRALINK_GRAPH_EDGELIST)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.EdgeListToAdjacencyArray "$(CATEGORY_TOP10_INTRALINK_GRAPH_EDGELIST)" "$(CATEGORY_TOP10_INTRALINK_GRAPH_ADJACENCYARRAY)"

categorytop10intralinkgraph: $(CATEGORY_TOP10_INTRALINK_GRAPH_ADJACENCYARRAY)

$(CATEGORY_ARTICLE_INTRALINK_GRAPH_EDGELIST): $(CATEGORY_HISTOGRAM) $(INTRALINK_GRAPH_EDGELIST)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.wikipedia.CategoryLinkedArticleGraph "$(CATEGORY_HISTOGRAM)" "$(INTRALINK_GRAPH_EDGELIST)" "$(CATEGORY_ARTICLE_INTRALINK_GRAPH_EDGELIST)"

$(CATEGORY_ARTICLE_INTRALINK_GRAPH_ADJACENCYARRAY): $(CATEGORY_ARTICLE_INTRALINK_GRAPH_EDGELIST)
	java $(JVM_OPTS) -cp "$(CLASSPATH)" com.xhochy.EdgeListToAdjacencyArray "$(CATEGORY_ARTICLE_INTRALINK_GRAPH_EDGELIST)" "$(CATEGORY_ARTICLE_INTRALINK_GRAPH_ADJACENCYARRAY)"

categoryarticleintralinkgraph: $(CATEGORY_ARTICLE_INTRALINK_GRAPH_ADJACENCYARRAY)

