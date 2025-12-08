cmake_minimum_required(VERSION 3.13)
project(cpp-json-http-example)

# You can change the complier version here
set(CMAKE_CXX_STANDARD 17)
# If C++17 is not available, the version will decay to the most recent available
# Uncomment below to treat the CMAKE_CXX_STANDARD setting as a hard requirement.
#set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Boost)
if(Boost_FOUND)
  include_directories(${Boost_INCLUDE_DIRS})
endif()

find_package(CURL REQUIRED)
include_directories(${CURL_INCLUDE_DIRS})

include_directories(cpr)
include_directories(json)

add_executable(json-http-example
        cpr/api.h
        cpr/auth.cpp
        cpr/auth.h
        cpr/body.h
        cpr/cookies.cpp
        cpr/cookies.h
        cpr/cpr.h
        cpr/cprtypes.cpp
        cpr/cprtypes.h
        cpr/curlholder.h
        cpr/defines.h
        cpr/digest.cpp
        cpr/digest.h
        cpr/multipart.cpp
        cpr/multipart.h
        cpr/parameters.cpp
        cpr/parameters.h
        cpr/payload.cpp
        cpr/payload.h
        cpr/proxies.cpp
        cpr/proxies.h
        cpr/response.h
        cpr/session.cpp
        cpr/session.h
        cpr/timeout.h
        cpr/util.cpp
        cpr/util.h
        json/json.hpp
        main.cpp)


target_link_libraries(json-http-example CURL::libcurl)
