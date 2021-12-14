// module and package name
%module(package="ch.idsia", directors="1") classes

// this contains type mapping for cpp's string type
%include <std_string.i>
%include "dynamic_cast_extension.i"

// verbatim to .cxx wrapper file
%{
#include "classes.h"
using namespace ch::idsia;
%}

// process header file
%include "classes.h";

%dynamic_cast_extension(ch::idsia, B, A);
%dynamic_cast_extension(ch::idsia, C, A);

// special directire to java only code
%pragma(java) jniclasscode=%{
    static {
        try {
            // load the library on first time
            System.loadLibrary("classes");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
%}
