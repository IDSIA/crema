#ifndef IDSIA_CREMA_CORE_OBSERVATIONBUILDER_H
#define IDSIA_CREMA_CORE_OBSERVATIONBUILDER_H

#include <unordered_map>
#include <vector>

using namespace std;

namespace crema
{
    namespace core
    {
        class ObservationBuilder : public unordered_map<int, int>
        {
        private:
            vector<int> vars;

            // ObservationBuilder(vector<int> keys);
            // ObservationBuilder(vector<int> keys, vector<int> values);

        public:
            // static ObservationBuilder observe(int var, int state);
            // static ObservationBuilder observe(vector<int> vars, vector<int> states);
            // static vector<ObservationBuilder> observe(vector<int> vars, vector<vector<int>> data);
            // // static vector<ObservationBuilder> observe(vector<string> vars, vector<vector<double>> data); TODO: remove this
            // static vector<ObservationBuilder> observe(vector<int> vars, vector<vector<double>> data);

            // ObservationBuilder *and_(int var, int state); // TODO: and is a reserved keyword!

            // ObservationBuilder *states(int states...);

            // static ObservationBuilder vars_(int vars...);

            // // static ObservationBuilder observe(int var, int state);

            // static vector<int> getVariables(vector<unordered_map<int, int>> obs);

            // static vector<vector<double>> toDoubles(vector<unordered_map<int, int>> obs, int variables...);

            // static vector<ObservationBuilder> filter(vector<unordered_map<int, int>> obs, int variables...);

            // static vector<unordered_map<int, int>> filter(vector<unordered_map<int, int>> data, vector<int> keys, vector<int> vals);
        };

    } // namespace core

} // namespace crema

#endif // IDSIA_CREMA_CORE_OBSERVATIONBUILDER_H
