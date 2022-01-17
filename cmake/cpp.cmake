# library in c++

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# add_cpp_test()
# CMake function to generate and build C++ test.
# Parameters:
#  the C++ filename
# e.g.:
# add_cpp_test(foo.cpp)
function(add_cpp_test FILE_NAME)
    message(STATUS "Configuring test ${FILE_NAME}: ...")
    get_filename_component(TEST_NAME ${FILE_NAME} NAME_WE)
    get_filename_component(TEST_DIR ${FILE_NAME} DIRECTORY)
    get_filename_component(COMPONENT_DIR ${FILE_NAME} DIRECTORY)
    get_filename_component(COMPONENT_NAME ${COMPONENT_DIR} NAME)

    add_executable(${TEST_NAME} ${FILE_NAME})
    
    target_include_directories(${TEST_NAME} PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
    target_compile_features(${TEST_NAME} PRIVATE cxx_std_17)
    target_link_libraries(${TEST_NAME} PRIVATE ${PROJECT_NAME})

    add_test(NAME cpp_${COMPONENT_NAME}_${TEST_NAME} COMMAND ${TEST_NAME})    
endfunction()

# setup source and test folders
set(CREMA_SOURCE_DIR "${PROJECT_SOURCE_DIR}/src/main/cpp")
set(CREMA_TEST_DIR "${PROJECT_SOURCE_DIR}/src/test/cpp")

add_subdirectory("${CREMA_SOURCE_DIR}")

if(CMAKE_PROJECT_NAME STREQUAL PROJECT_NAME AND ${TEST_CXX})
    add_subdirectory("${CREMA_TEST_DIR}")
endif()
