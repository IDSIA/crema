#include "Strides.h"

#include <algorithm>

using namespace crema::core;
using namespace std;

Stride::Stride(
    const vector<int> &variables,
    const vector<int> &sizes,
    const vector<int> &strides) : cardinality(variables.size()),
                                  variables(variables),
                                  sizes(sizes),
                                  strides(strides)
{
    // do nothing
}

Stride::Stride(
    const vector<int> &variables,
    const vector<int> &sizes) : cardinality(variables.size()),
                                variables(variables),
                                sizes(sizes)
{
    const int size = variables.size();
    strides[0] = 1;
    for (int i = 0; i < size; ++i)
    {
        strides[i + 1] = strides[i] * sizes[i];
    }
    cardinality = strides[size];
}

int Stride::indexOf(int variable)
{
    auto begin = variables.begin();
    auto end = variables.end();
    auto it = find(begin, end, variable);

    if (it != end)
    {
        return it - begin;
    }
    return -1;
}

int Stride::getCardinality(int variable)
{
    return sizes[indexOf(variable)];
}
