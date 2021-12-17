cmake_minimum_required(VERSION 3.10)

project(
    Crema 
    VERSION 0.3.0
    LANGUAGES CXX
)

# specify the C++17 standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

set(CMAKE_CURRENT_CMAKE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

set_property(GLOBAL PROPERTY USE_FOLDERS ON)
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/")
set(BOOST_REQUESTED_VERSION "1.78.0")

find_package(Boost COMPONENTS graph)

file(GLOB_RECURSE SRC_FILES
    "${PROJECT_SOURCE_DIR}/crema/*.h"
    "${PROJECT_SOURCE_DIR}/crema/*.cpp"
)

include_directories(${BOOST_INCLUDE_DIRS})

add_executable(${PROJECT_NAME} ${SRC_FILES})

target_link_libraries(${PROJECT_NAME} PRIVATE ${Boost_LIBRARIES})
set_target_properties(${PROJECT_NAME} PROPERTIES RUNTIME_OUTPUT_DIRECTORY_DEBUG "${PROJECT_BINARY_DIR}/bin/src/")
set_target_properties(${PROJECT_NAME} PROPERTIES RUNTIME_OUTPUT_DIRECTORY_RELEASE "${PROJECT_BINARY_DIR}/bin/src/")
target_compile_features(${PROJECT_NAME} PRIVATE cxx_std_17)

# target_include_directories(${PROJECT_NAME} PUBLIC "${PROJECT_BINARY_DIR}")
