#include "crema/core/SimpleDomain.h"
#include "crema/utils/ArraysUtil.h"

#include <algorithm>

using namespace crema::core;
using namespace std;

SimpleDomain::SimpleDomain(Domain *domain)
{
    this->variables = domain->getVariables();
    this->sizes = domain->getSizes();
    this->size = domain->getSize();
}

SimpleDomain::SimpleDomain(vector<int> *variables, vector<int> *sizes, int size)
{
    this->variables = variables;
    this->sizes = sizes;
    this->size = size;
}

int SimpleDomain::indexOf(int variable)
{
    // return crema::utils::indexOf(this->variables, variable); TODO:
    return 0;
}

int SimpleDomain::getCardinality(int variable)
{
    int offset = indexOf(variable);
    return sizes->at(offset);
}

bool SimpleDomain::contains(int variable)
{
    return indexOf(variable) >= 0;
}

vector<int> *SimpleDomain::getVariables()
{
    return variables;
}

vector<int> *SimpleDomain::getSizes()
{
    return sizes;
}

int SimpleDomain::getSize()
{
    return size;
}

int SimpleDomain::getSizeAt(int index)
{
    return sizes->at(index);
}

void SimpleDomain::removed(int variable)
{
    int index = -(indexOf(variable) + 1);
    for (; index < size; ++index)
    {
        variables->assign(index, variables->at(index) - 1);
    }
}
