#include <iostream>

#include <parquet/api/reader.h>
#include <parquet/api/schema.h>

using namespace parquet_cpp;

void readRowGroup(std::shared_ptr<RowGroupReader> &group_reader) {
  for (int col = 0; col < group_reader->num_columns(); col++) {
    std::shared_ptr<ColumnReader> column = group_reader->Column(col);
    std::cout << column->descr()->max_repetition_level() << std::endl;
    std::cout << column->descr()->name() << std::endl;
    // if (column->descr()->name() == "degree") {
    //   std::shared_ptr<Int32Reader> column_reader = std::dynamic_pointer_cast<Int32Reader>(column);
    //   while (column_reader->HasNext()) {
    //     readBatch(column_reader, stats);
    //   }
    //   // There is only one degree column, so we can skip the other ones in the RowGroup.
    //   break;
    // }
  }
}

int main(int argc, char** argv) {
  std::string filename = argv[1];

  try {
    std::unique_ptr<ParquetFileReader> reader = ParquetFileReader::OpenFile(filename);
    std::cout << static_cast<const schema::GroupNode*>(reader->descr()->schema().get())->field(2)->name() << std::endl;
    return 0;

    // Each vertex is a row in our Parquet file.
    std::cout << "# of vertices: " << reader->num_rows() << std::endl;

    for (int rg = 0; rg < reader->num_row_groups(); rg++) {
      std::shared_ptr<RowGroupReader> group_reader = reader->RowGroup(rg);
      readRowGroup(group_reader);
    }


    reader->Close();
  } catch (const std::exception& e) {
    std::cerr << "Parquet error: "
              << e.what()
              << std::endl;
    return -1;
  }

  return 0;
}

