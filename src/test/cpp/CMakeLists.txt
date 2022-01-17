if (NOT TEST_CXX)
    return()
endif()

# testing in c++
include(cpp.test)

# message("Testing C++ version")
# enable_testing()

# include(GoogleTest)

# tests for library in c++
set(CREMA_TESTS "${CREMA_TEST_DIR}/crema")

message(STATUS "CREMA_TEST_DIR:      ${CREMA_TEST_DIR}")
message(STATUS "CREMA_TESTS:         ${CREMA_TESTS}")

# add target for testing purposes
# add_executable(main ${CREMA_TESTS}/main.cpp)
# target_link_libraries(main PRIVATE ${PROJECT_NAME})

# add_cpp_test("crema/example.cpp")
package_add_test_with_libraries(
    example 
    "crema/example.cpp" 
    ${PROJECT_NAME}
    "${CREMA_TESTS}"
)
