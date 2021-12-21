#include "Strides.h"
#include "ArraysUtil.h"

#include <algorithm>

using namespace crema::core;
using namespace std;

Stride::Stride(
    const vector<int> &variables,
    const vector<int> &sizes,
    const vector<int> &strides) : size(variables.size()),
                                  variables(variables),
                                  sizes(sizes),
                                  strides(strides),
                                  combinations(strides.size())
{
    // do nothing
}

Stride::Stride(
    const vector<int> &variables,
    const vector<int> &sizes) : size(variables.size()),
                                variables(variables),
                                sizes(sizes)
{
    const int size = variables.size();
    strides.reserve(size);
    strides.push_back(1);
    for (int i = 0; i < size; ++i)
    {
        strides.push_back(strides[i] * sizes[i]);
    }
    combinations = strides[size];
}

Stride::Stride(Stride domain, int offset)
{
    size = domain.variables.size();

    variables.reserve(size - 1);
    variables.insert(variables.end(), domain.variables.begin(), domain.variables.begin() + offset);
    variables.insert(variables.end(), domain.variables.begin() + offset + 1, domain.variables.end());

    sizes.reserve(size);
    sizes.insert(sizes.end(), domain.sizes.begin(), domain.sizes.begin() + offset);
    sizes.insert(sizes.end(), domain.sizes.begin() + offset + 1, domain.sizes.end());

    strides.reserve(size + 1);
    strides.push_back(1);
    if (offset > 1)
    {
        strides.insert(strides.end(), domain.strides.begin(), domain.strides.begin() + offset - 1);
    }
    else
    {
        offset = 0;
    }

    for (; offset < size; offset++)
    {
        strides.push_back(strides[offset] * sizes[offset]);
    }
    combinations = strides[size];
}

int Stride::indexOf(int variable)
{
    return crema::utils::indexOf(variables, variable);
}

int Stride::getCardinality(int variable)
{
    return sizes[indexOf(variable)];
}

bool Stride::contains(int variable)
{
    return indexOf(variable) >= 0;
}

vector<int> *Stride::getVariables()
{
    return &variables;
}

vector<int> *Stride::getSizes()
{
    return &sizes;
}

int Stride::getSize()
{
    return size;
}

int Stride::getSizeAt(int index)
{
    return sizes[index];
}

void Stride::removed(int variable)
{
    int index = -indexOf(variable) - 1;

    for (; index < variables.size(); ++index)
    {
        --variables[index];
    }
}

vector<int> *Stride::getStrides()
{
    return &strides;
}

vector<int> Stride::statesOf(int offset)
{
    int left_over = offset;
    vector<int> result(variables.size());
    for (int i = 0; i < size && left_over != 0; ++i)
    {
        result[i] = left_over % sizes[i];
        left_over = (left_over - result[i]) / sizes[i];
    }
    return result;
}

/*
ObservationBuilder Stride::observationOf(int offset)
{
    // return ObservationBuilder::observe(getVariables(), statesOf(offset));
}
*/