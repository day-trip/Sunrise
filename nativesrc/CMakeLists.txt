# cmake_minimum_required(VERSION <specify CMake version here>)
project(nativesrc)

cmake_minimum_required(VERSION 3.16)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_LEGACY_CYGWIN_WIN32 0) # Remove when CMake >= 2.8.4 is required

include_directories(.)
include_directories("C:/Users/Jai Giri/.jdks/adopt-openj9-1.8.0_282/include")
include_directories("C:/Users/Jai Giri/.jdks/adopt-openj9-1.8.0_282/include/win32")

add_executable(nativesrc
        com_daytrip_shared_math_SunriseMath.cpp
        com_daytrip_shared_math_SunriseMath.h)
