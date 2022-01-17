#ifndef IDSIA_CREMA_CORE_SIMPLEDOMAIN_H
#define IDSIA_CREMA_CORE_SIMPLEDOMAIN_H

#include "Domain.h"

#include <vector>

using namespace std;

namespace crema
{
    namespace core
    {
        /**
         * @brief Domain implementation that does not include strides. You are likely to need @link Strides instead of this class.
         * 
         * @author davidhuber
         */
        class SimpleDomain : public Domain
        {
        private:
            vector<int> *variables;
            vector<int> *sizes;
            int size;

        public:
            SimpleDomain(Domain *domain);
            SimpleDomain(vector<int> *variables, vector<int> *sizes, int size);

            int indexOf(int variable) override;

            int getCardinality(int variable) override;

            bool contains(int variable) override;

            vector<int> *getVariables() override;
            vector<int> *getSizes() override;

            int getSize() override;
            int getSizeAt(int index) override;

            void removed(int variable) override;
        };
    }
}

#endif // IDSIA_CREMA_CORE_SIMPLEDOMAIN_H
