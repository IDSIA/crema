#include <iostream>

#include "core/Strides.h"

#include "crema.h"

int main()
{
    std::vector<int> variables = {1,2};
    std::vector<int> sizes = {2, 2};

    // crema::core::Stride *d = new crema::core::Stride(variables, sizes);

    std::cout << "Crema" << std::endl;

    for (int v: variables)
        std::cout << v << " ";
    std::cout << std::endl;
    
    for (int s: sizes)
        std::cout << s << " ";
    std::cout << std::endl;
}
