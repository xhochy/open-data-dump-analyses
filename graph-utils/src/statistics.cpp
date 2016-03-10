#include <iostream>

#include <parquet/api/reader.h>
#include <parquet/api/schema.h>

using namespace parquet_cpp;

struct graph_statistics {
  int32_t min_degree;
  int32_t max_degree;
  int degree_sum;
};

void readBatch(std::shared_ptr<Int32Reader> &column_reader, graph_statistics &stats) {
  int16_t def_levels[4];
  int16_t rep_levels[4];
  int32_t values[4];
  int64_t values_read;
  int levels_read = column_reader->ReadBatch(4, def_levels, rep_levels, values, &values_read);
  // Even though the degrees column may be optional, it is enough to stick to values read. Only if we want to have the column in sync with other columns, we need to respect the definition levels.
  for (int i = 0; i < values_read; i++) {
    stats.degree_sum += values[i];
    stats.min_degree = std::min(values[i], stats.min_degree);
    stats.max_degree = std::max(values[i], stats.max_degree);
  }
}

void readRowGroup(std::shared_ptr<RowGroupReader> &group_reader, graph_statistics &stats) {
  for (int col = 0; col < group_reader->num_columns(); col++) {
    std::shared_ptr<ColumnReader> column = group_reader->Column(col);
    if (column->descr()->name() == "degree") {
      std::shared_ptr<Int32Reader> column_reader = std::dynamic_pointer_cast<Int32Reader>(column);
      while (column_reader->HasNext()) {
        readBatch(column_reader, stats);
      }
      // There is only one degree column, so we can skip the other ones in the RowGroup.
      break;
    }
  }
}

int main(int argc, char** argv) {
  std::string filename = argv[1];
  // TODO: neighbours should be a simple "repeated int32" not a nested repeated{required}

  try {
    std::unique_ptr<ParquetFileReader> reader = ParquetFileReader::OpenFile(filename);

    // Each vertex is a row in our Parquet file.
    std::cout << "# of vertices: " << reader->num_rows() << std::endl;

    // For this we only need to utilise the degree column.
    // A benefit of Parquet Format: We do not need to read in the neighbours.
    graph_statistics stats{std::numeric_limits<std::int32_t>::max(), std::numeric_limits<std::int32_t>::min(), 0};
    for (int rg = 0; rg < reader->num_row_groups(); rg++) {
      std::shared_ptr<RowGroupReader> group_reader = reader->RowGroup(rg);
      readRowGroup(group_reader, stats);
    }

    // TODO Hande the min(degree) case for files with "optional int32 degree"
    
    std::cout << "# of edges: " << stats.degree_sum << std::endl;
    std::cout << "min(degree): " << stats.min_degree << std::endl;
    std::cout << "avg(degree): " << (stats.degree_sum / static_cast<float>(reader->num_rows())) << std::endl;
    std::cout << "max(degree): " << stats.max_degree << std::endl;
  } catch (const std::exception& e) {
    std::cerr << "Parquet error: "
              << e.what()
              << std::endl;
    return -1;
  }

  return 0;
}

