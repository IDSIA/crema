#include <vector>

using namespace std;

namespace crema
{

    namespace utils
    {

        /**
         * @brief Generic function to find the position of an element in a vector and return its position.
         * 
         * @tparam T the type of the vector
         * @param vector the vector to search in
         * @param element the element to search for
         * @return int the position of the element in the vector if found, otherwise -1
         */
        template <typename T>
        int indexOf(const vector<T> &vector, const T &element);

        template <typename T>
        vector<T> slice(vector<T> array, int idx...);
        template <typename T>
        vector<T> slice(vector<T> array, vector<int> indices);

    } // namespace utils

} // namespace crema
