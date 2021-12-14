// this is for class downcasting
%{
#include <memory>
%}

%define %dynamic_cast_extension(CppNamespace, DescendantClass, BaseClass)
%extend CppNamespace::DescendantClass
{
    static std::shared_ptr<CppNamespace::DescendantClass>
    dynamic_cast(std::shared_ptr<CppNamespace::BaseClass> arg)
    {
        return std::dynamic_pointer_cast<CppNamespace::DescendantClass>(arg);
    }
};
%enddef
