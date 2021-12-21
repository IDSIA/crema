#ifndef IDSIA_CREMA_CORE_STRIDES_H
#define IDSIA_CREMA_CORE_STRIDES_H

#include "Domain.h"
#include "ObservationBuilder.h"

#include <vector>

using namespace std;

namespace crema
{
    namespace core
    {
        /**
         * @brief An implementation of the domain that stores also the strides of the variables. As in every other place we assume a global ordering of the variables.
         * 
         * Methods are provided to get an index iterator that traverses the domain in a non sorted way. This is mostly used for user interfaces where the user might want a specific ordering (for instance the conditioning order: var | conditioning).
         *
         * While the class should be considered unmutable, when the factor is part of a model and we delete a variable from it, the indices of the variables will be updated accordingly.
         *
         * @author davidhuber
         * 
         */
        class Stride : public Domain
        {
        private:
            int combinations;
            int size;

            vector<int> variables;
            vector<int> sizes;
            vector<int> strides;

        public:
            Stride(const vector<int> &variables, const vector<int> &sizes, const vector<int> &strides);
            /**
             * @brief Creates the domain based on the list of variables and their cardinality.
             * 
             * @param variables 
             * @param sizes 
             */
            Stride(const vector<int> &variables, const vector<int> &sizes);

            /**
             * @brief Creates a stride with a single variable excluded. Note that the variable must not be missing in the provided domain.
             * 
             * @param domain 
             * @param offset 
             */
            Stride(Stride domain, int offset);

            int indexOf(int variable) override;

            int getCardinality(int variable) override;

            bool contains(int variable) override;

            vector<int> *getVariables() override;
            vector<int> *getSizes() override;

            int getSize() override;
            int getSizeAt(int index) override;

            void removed(int variable) override;

            vector<int> *getStrides();

            vector<int> statesOf(int offset);

            // ObservationBuilder observationOf(int offset);
        };
    }
}

#endif // IDSIA_CREMA_CORE_STRIDES_H
