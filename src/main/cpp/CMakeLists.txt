if (NOT BUILD_CXX)
    return()
endif()

# library in c++
message("Building C++ version")

# cmake files
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CREMA_SOURCE_DIR}/cmake")

# crema source and header files
set(CREMA_INLCUDE "${CREMA_SOURCE_DIR}/include")
set(CREMA_SOURCES "${CREMA_SOURCE_DIR}/src")

set(CREMA_INCLUDE_LIB "${CREMA_INLCUDE}/crema")
set(CREMA_SOURCES_LIB "${CREMA_SOURCES}/crema")
set(CREMA_INCLUDE_CORE "${CREMA_INCLUDE_LIB}/core")
set(CREMA_SOURCES_CORE "${CREMA_SOURCES_LIB}/core")
set(CREMA_INCLUDE_UTILS "${CREMA_INCLUDE_LIB}/utils")
set(CREMA_SOURCES_UTILS "${CREMA_SOURCES_LIB}/utils")

message(STATUS "CREMA_SOURCE_DIR:    ${CREMA_SOURCE_DIR}")

message(STATUS "CREMA_INLCUDE:       ${CREMA_INLCUDE}")
message(STATUS "CREMA_SOURCES:       ${CREMA_SOURCES}")

message(STATUS "CREMA_MODULE_CORE:   ${CREMA_INCLUDE_CORE}")
message(STATUS "                     ${CREMA_SOURCES_CORE}")
message(STATUS "CREMA_MODULE_UTILS:  ${CREMA_INCLUDE_UTILS}")
message(STATUS "                     ${CREMA_SOURCES_UTILS}")

# generate files
configure_file("cmake/version.h.in" "${CREMA_INCLUDE_LIB}/version.h")

# add target for shared library
add_library(${PROJECT_NAME} SHARED)
target_include_directories(${PROJECT_NAME}
    PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    # ${CREMA_INLCUDE}
)
target_sources(${PROJECT_NAME}
    PUBLIC
        ${CREMA_INCLUDE_CORE}/Domain.h
        ${CREMA_INCLUDE_CORE}/DomainBuilder.h
        ${CREMA_INCLUDE_CORE}/ObservationBuilder.h
        ${CREMA_INCLUDE_CORE}/SimpleDomain.h
        ${CREMA_INCLUDE_CORE}/Strides.h
        ${CREMA_INCLUDE_UTILS}/ArraysUtil.h
        ${CREMA_INCLUDE_LIB}/version.h
        ${CREMA_INLCUDE}/crema.h
    PRIVATE
        ${CREMA_SOURCES_CORE}/ObservationBuilder.cpp
        ${CREMA_SOURCES_CORE}/SimpleDomain.cpp
        ${CREMA_SOURCES_CORE}/Strides.cpp
        ${CREMA_SOURCES_UTILS}/ArraysUtil.cpp
)

# target properties
set_target_properties(${PROJECT_NAME}
    PROPERTIES
        VERSION ${PROJECT_VERSION}
        PUBLIC_HEADER ${CMAKE_CURRENT_SOURCE_DIR}/include/crema.h 
)
