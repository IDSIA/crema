#include "crema/version.h"
#include <iostream>

bool test_domain() 
{
    // if something goes wrong => return false
    return true;
}

bool test_another()
{
    return false;
}

int main(){
    std::cout << "Crema version: " << Crema_VERSION << std::endl;
    return 0;
}