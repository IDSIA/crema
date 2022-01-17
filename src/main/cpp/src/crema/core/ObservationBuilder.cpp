#include "crema/core/ObservationBuilder.h"
#include "crema/utils/ArraysUtil.h"

#include <algorithm>
#include <cmath>
#include <cfloat>

using namespace crema::core;
using namespace std;

/*
ObservationBuilder::ObservationBuilder(vector<int> keys) : vars(keys)
{
}

ObservationBuilder::ObservationBuilder(vector<int> keys, vector<int> values)
{
}

ObservationBuilder ObservationBuilder::observe(int var, int state)
{
    return ObservationBuilder({var}, {state});
}

ObservationBuilder ObservationBuilder::observe(vector<int> vars, vector<int> states)
{
    return ObservationBuilder(vars, states);
}

vector<ObservationBuilder> ObservationBuilder::observe(vector<int> vars, vector<vector<int>> data)
{
    vector<ObservationBuilder> observations(data.size());

    for (int i = 0; i < data.size(); i++)
    {
        vector<int> data_i = crema::utils::slice(data[i], data[i]);
        vector<int> vars_i = crema::utils::slice(vars, data[i]);
        observations.push_back(observe(vars_i, data_i));
    }

    return observations;
}

vector<ObservationBuilder> ObservationBuilder::observe(vector<int> vars, vector<vector<double>> data)
{
    vector<ObservationBuilder> observations(data.size());

    for (int i = 0; i < data.size(); i++)
    {
        vector<int> valid;
        std::copy_if(data[i].begin(), data[i].end(), std::back_inserter(valid), [](double x)
                     { return !std::isnan(x); });

        vector<int> slice_vars = crema::utils::slice(vars, valid);
        vector<double> slice_data = crema::utils::slice(data[i], valid);
        vector<int> slice_data_i(slice_data.begin(), slice_data.end());
        observations.push_back(ObservationBuilder::observe(slice_vars, slice_data_i));
    }

    return observations;
}
*/