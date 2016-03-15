cmake_minimum_required(VERSION 3.0)
project(graph_utils)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake_modules")

include(CheckCXXCompilerFlag)
check_cxx_compiler_flag( "-std=c++11" CXX11_FOUND )
check_cxx_compiler_flag( "-std=c++0x" CXX0X_FOUND )
check_cxx_compiler_flag( "-stdlib=libc++" LIBCPP_FOUND )
if(CXX11_FOUND)
    set( CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
elseif(CXX0X_FOUND)
    set( CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++0x")
else()
    message(STATUS "${CMAKE_CXX_COMPILER} does not support C++11, please use a different compiler")
endif()

if(("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang" OR APPLE) AND
    LIBCPP_FOUND)
    set( CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif()


find_package(Parquet REQUIRED)
include_directories(${PARQUET_INCLUDE_DIR})

add_executable(statistics src/statistics.cpp)
target_link_libraries(statistics ${PARQUET_SHARED_LIB})

# Just a scaffold for doing computations on a graph
add_executable(loadgraph src/loadgraph.cpp)
target_link_libraries(loadgraph ${PARQUET_SHARED_LIB})
