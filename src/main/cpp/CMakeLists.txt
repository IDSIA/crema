cmake_minimum_required(VERSION 3.15)

project(
    crema 
    VERSION 0.3.0
    DESCRIPTION "Crema: A Toolbox for Credal Models Algorithms"
    LANGUAGES CXX
)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

# cmake files
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/")

# crema source and header files
set(CREMA_SOURCE_DIR "${CMAKE_SOURCE_DIR}/crema")
set(CREMA_MODULE_CORE "${CREMA_SOURCE_DIR}/core")
set(CREMA_MODULE_UTILS "${CREMA_SOURCE_DIR}/utils")

configure_file("${CMAKE_MODULE_PATH}/version.h.in" "${CREMA_SOURCE_DIR}/version.h")

add_library(${PROJECT_NAME} SHARED)
target_include_directories(${PROJECT_NAME}
    PUBLIC
    ${CREMA_MODULE_CORE}
    ${CREMA_MODULE_UTILS}
    ${CREMA_SOURCE_DIR}
)
target_sources(${PROJECT_NAME}
    PUBLIC
        ${CREMA_MODULE_CORE}/Domain.h
        ${CREMA_MODULE_CORE}/DomainBuilder.h
        ${CREMA_MODULE_CORE}/ObservationBuilder.h
        ${CREMA_MODULE_CORE}/SimpleDomain.h
        ${CREMA_MODULE_CORE}/Strides.h
        ${CREMA_MODULE_UTILS}/ArraysUtil.h
        ${CREMA_SOURCE_DIR}/crema.h
        ${CREMA_SOURCE_DIR}/version.h
    PRIVATE
        ${CREMA_MODULE_CORE}/ObservationBuilder.cpp
        ${CREMA_MODULE_CORE}/SimpleDomain.cpp
        ${CREMA_MODULE_CORE}/Strides.cpp
        ${CREMA_MODULE_UTILS}/ArraysUtil.cpp
)

add_executable(main ${PROJECT_SOURCE_DIR}/main.cpp)
target_link_libraries(main PRIVATE ${PROJECT_NAME})

# boost configuration
# set(BOOST_REQUESTED_VERSION "1.78.0")
# set(Boost_ROOT "${CMAKE_SOURCE_DIR}/lib/Boost")
# set(Boost_USE_STATIC_LIBS OFF)
# set(Boost_USE_MULTITHREADED ON)
# set(Boost_USE_STATIC_RUNTIME OFF)

# debug
# message(STATUS "PROJECT_NAME:        ${PROJECT_NAME}")
# message(STATUS "CMAKE_SOURCE_DIR:    ${CMAKE_SOURCE_DIR}")
# message(STATUS "CMAKE_MODULE_PATH:   ${CMAKE_MODULE_PATH}")
# message(STATUS "CREMA_SOURCE_DIR:    ${CREMA_SOURCE_DIR}")
# message(STATUS "BOOST_ROOT:          ${Boost_ROOT}")

# if (POLICY CMP0074)
#   cmake_policy(SET CMP0074 NEW)
# endif()

# find_package(Boost "${BOOST_REQUESTED_VERSION}" REQUIRED COMPONENTS graph)
# message(STATUS "Boost version: ${Boost_VERSION}")

# if (Boost_FOUND)
#     message(STATUS "Boost has been found!")
#     include_directories(${Boost_INCLUDE_DIR})
#     ADD_DEFINITIONS( "-DHAS_BOOST")
# else()
#     message(STATUS "Boost NOT found!")
# endif()

# if(NOT TARGET Boost::graph)
#     add_library(Boost::graph IMPORTED INTERFACE)
#     set_property(TARGET Boost::graph PROPERTY INTERFACE_INCLUDE_DIRECTORIES ${Boost_INCLUDE_DIR})
#     set_property(TARGET Boost::graph PROPERTY INTERFACE_LINK_LIBRARIES ${Boost_LIBRARIES})
# endif()
