if (NOT TEST_CXX)
    return()
endif()

# testing in c++
message("Testing C++ version")
enable_testing()

# tests for library in c++
set(CREMA_TESTS "${CREMA_TEST_DIR}/crema")

message(STATUS "CREMA_TEST_DIR:      ${CREMA_TEST_DIR}")
message(STATUS "CREMA_TESTS:         ${CREMA_TESTS}")

# add target for testing purposes
# add_executable(main ${CREMA_TESTS}/main.cpp)
# target_link_libraries(main PRIVATE ${PROJECT_NAME})

add_cpp_test("crema/example.cpp")
