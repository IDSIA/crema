#ifndef IDSIA_CREMA_CORE_DOMAIN_H
#define IDSIA_CREMA_CORE_DOMAIN_H

#include <vector>

using namespace std;

namespace crema
{
    namespace core
    {
        class Domain
        {
        public:
            virtual ~Domain() = default;

            /**
             * @brief Get the cardinality of a variable in the domain
             * 
             * @param variable 
             * @return int 
             */
            virtual int getCardinality(int variable) = 0;

            /**
             * @brief Get the cardinality of the variable at the specified offset in the domain.
             * This method is usually used with the result of indexOf().
             * 
             * @param index 
             * @return int 
             */
            virtual int getSizeAt(int index) = 0;

            /**
             * @brief Find the location/offset of a variable in the domain.
             * 
             * @param variable 
             * @return int 
             */
            virtual int indexOf(int variable) = 0;

            /**
             * @brief Check if the specified variable is present in the domain
             * 
             * @param variable 
             * @return true 
             * @return false 
             */
            virtual bool contains(int variable) = 0;

            /**
             * @brief The vector of variables in this domain. 
             * Please read-only!
             * 
             * @return vector<int>* 
             */
            virtual vector<int> *getVariables() = 0;

            /**
             * @brief Get all the cardinalities of the variables in the domain.
             * 
             * @return vector<int>* 
             */
            virtual vector<int> *getSizes() = 0;

            /**
             * @brief  Get the number of variables in the domain.
             * 
             * @return int 
             */
            virtual int getSize() = 0;

            /**
             * @brief Notify the domain that a variable has been removed. Prior to remove a variable the model will have to remove all arcs involving the variable. Because of this domains should not contain the removed variable.
             * 
             * An Exception may be thrown if the removed variable is part of the domain.
             * 
             * @param variable 
             */
            virtual void removed(int variable) = 0;
        };
    }
}

#endif // IDSIA_CREMA_CORE_DOMAIN_H
