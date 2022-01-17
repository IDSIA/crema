#include "crema/version.h"

#include "gtest/gtest.h"

#include <iostream>

namespace crema
{
    class ExampleTest : public ::testing::Test {
        protected:
        void SetUp() override {
            std::cout << "Setting up!" << std::endl;
        }

        void TearDown() override {
            std::cout << "Tearing down!" << std::endl;
        }
    };
    
    TEST_F(ExampleTest, TestIfItWorks) {
        std::cout << "Crema version: " << Crema_VERSION << std::endl;
        EXPECT_EQ(7, 7);
    }

    TEST_F(ExampleTest, TestIfItWorksAgain) {
        EXPECT_EQ(2, 2);
    }

}; // namespace crema

int main(int argc, char **argv) 
{
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
