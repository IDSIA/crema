cmake_minimum_required(VERSION 3.15)

project(
    Crema 
    VERSION 0.3.0
    DESCRIPTION "Crema: A Toolbox for Credal Models Algorithms"
    LANGUAGES CXX
)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/")

set(CREMA_SOURCE_DIR "${CMAKE_SOURCE_DIR}/crema")

set(BOOST_REQUESTED_VERSION "1.78.0")
set(Boost_ROOT "${CMAKE_SOURCE_DIR}/lib")
set(Boost_USE_STATIC_LIBS OFF)
set(Boost_USE_MULTITHREADED ON)
set(Boost_USE_STATIC_RUNTIME OFF)

message(STATUS "PROJECT_NAME:        ${PROJECT_NAME}")
message(STATUS "CMAKE_SOURCE_DIR:    ${CMAKE_SOURCE_DIR}")
message(STATUS "CMAKE_MODULE_PATH:   ${CMAKE_MODULE_PATH}")
message(STATUS "CREMA_SOURCE_DIR:    ${CREMA_SOURCE_DIR}")
message(STATUS "BOOST_ROOT:          ${Boost_ROOT}")

if (POLICY CMP0074)
  cmake_policy(SET CMP0074 NEW)
endif()

find_package(Boost REQUIRED COMPONENTS graph)
message(STATUS "Boost version: ${Boost_VERSION}")

if (Boost_FOUND)
    message(STATUS "Boost has been found!")
    include_directories(${Boost_INCLUDE_DIR})
    ADD_DEFINITIONS( "-DHAS_BOOST")
else()
    message(STATUS "Boost NOT found!")
endif()

configure_file("${CMAKE_MODULE_PATH}/version.h.in" "${CREMA_SOURCE_DIR}/version.h")

set(MODULES)
list(APPEND MODULES "core")     # main classes
list(APPEND MODULES "utils")    # utility methods

target_link_libraries(${PROJECT_NAME} PUBLIC Boost::filesystem)

add_library(${PROJECT_NAME} SHARED "${CREMA_SOURCE_DIR}/crema.h")
add_executable(${PROJECT_NAME}_example "${PROJECT_SOURCE_DIR}/main.cpp")
