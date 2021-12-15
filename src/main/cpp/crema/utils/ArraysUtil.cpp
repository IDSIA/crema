#include "ArraysUtil.h"

template <typename T>
int crema::utils::indexOf(const vector<T> &vector, const T &element)
{

    auto it = std::find(vector.begin(), vector.end(), element);

    if (it != vector.end())
        return std::distance(vector.begin(), it);

    return -1;
}

template <typename T>
vector<T> crema::utils::slice(vector<T> array, int idx...)
{
    vector<int> indices = {idx};
    return crema::utils::slice(array, indices);
}

template <typename T>
vector<T> crema::utils::slice(vector<T> array, vector<int> indices)
{
    vector<T> result;
    result.reserve(idx);
    for (int i = 0; i < indices.size(); i++)
    {
        result.push_back(array[indices[i]]);
    }
    return result;
}
