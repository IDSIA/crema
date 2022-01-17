cmake_minimum_required(VERSION 3.15)

# project definition
project(
    crema 
    VERSION 0.3.0
    DESCRIPTION "Crema: A Toolbox for Credal Models Algorithms"
    LANGUAGES CXX
)

message("${PROJECT_NAME} version: ${PROJECT_VERSION}")

message(STATUS "PROJECT_NAME:        ${PROJECT_NAME}")
message(STATUS "CMAKE_SOURCE_DIR:    ${CMAKE_SOURCE_DIR}")
message(STATUS "PROJECT_SOURCE_DIR:  ${PROJECT_SOURCE_DIR}")

# setup tests
set_property(GLOBAL PROPERTY CTEST_TARGETS_ADDED 1)
include(CTest)

# building and testing options
option(BUILD_CXX "Build C++ library" ON)
message(STATUS "Build C++ library:   ${BUILD_CXX}")

option(TEST_CXX "Test for C++ library" ON)
message(STATUS "Build C++ library:   ${TEST_CXX}")

option(BUILD_JAVA "Build Java Library" OFF)
message(STATUS "Build Java:          ${BUILD_JAVA}")

option(BUILD_PYTHON "Build Python Library" OFF)
message(STATUS "Build Python:        ${BUILD_PYTHON}")

# Include local modules
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")
message(STATUS "Module path:         ${CMAKE_MODULE_PATH}")

include(cpp)
include(java)
include(python)
