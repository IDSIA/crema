cmake_minimum_required(VERSION 3.10)

project(Crema VERSION 0.3.0)

# specify the C++17 standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

set(CMAKE_CURRENT_CMAKE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

# configure_file("${CMAKE_CURRENT_CMAKE_DIR}/crema.h.in" "${CMAKE_CURRENT_SOURCE_DIR}/crema/crema.h")

# target_include_directories(${PROJECT_NAME} PUBLIC "${PROJECT_BINARY_DIR}")

set(Boost_USE_STATIC_LIBS        ON)  # only find static libs
set(Boost_USE_DEBUG_LIBS        OFF)  # ignore debug libs and
set(Boost_USE_RELEASE_LIBS       ON)  # only find release libs
set(Boost_USE_MULTITHREADED      ON)
set(Boost_USE_STATIC_RUNTIME    OFF)
find_package(Boost 1.78.0 COMPONENTS graph) # TODO: put as required

if(Boost_FOUND)
    include_directories(${Boost_INCLUDE_DIRS}) 
    add_executable(progname file1.cxx file2.cxx) 
    target_link_libraries(progname ${Boost_LIBRARIES})
endif()
