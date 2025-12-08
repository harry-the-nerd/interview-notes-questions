# C++

A couple tips specifically if you're solving this problem using C++:

## Libraries

We recommend you use [nlohmann's json library][nlohmann/json] and
[cpr][libcpr/cpr] for implementation. We've added both libraries to
this project, and included a `CMakeLists.txt` that will build them
(using C++17 if supported). If you don't have a preferred CMake workflow,
you can use this:

```bash
# Generate the project scaffolding into cpp/build/
cd cpp
cmake -S . -B build -DCMAKE_BUILD_TYPE=Debug

# Build the project
cmake --build build
```

You'll need to have [libcurl](https://curl.haxx.se/download.html)
installed - we'll link it automatically.

See "Library Installation Instructions" below if don't have `cmake` or `libcurl`.

## json

The json library tries to be pretty magical. Don't be afraid to use
`.get<type>()` instead of relying on it to coerce.

Iterating over json objects is tricky. The best solution we found was
to assign the object to a `std::map<string, json>` and iterate over
that.

## cpr

If you want to set custom headers, know that `cpr::Header` is basically
just a `std::map<string, string>` - you don't have to use their initializer
list syntax. Note that the type has a custom comparison function, so it's
easiest to declare your variable as `cpr::Header`.

We also made a small change from the public docs: `cpr::Payload` has
an overloaded constructor which can take a `std::string`, instead of
only accepting a list of key/value pairs.

[nlohmann/json]: https://github.com/nlohmann/json
[libcpr/cpr]: https://docs.libcpr.org/

## Library Installation Instructions

### Linux or macOS
- If you're using Debian or Ubuntu, you likely want to run `sudo apt-get install cmake build-essential`.
- If you're using macOS with HomeBrew installed, you might want to run `brew install cmake curl`.

If you can run these commands, your development environment is probably ready for Stripe's C++ interview questions:

```powershell
$ cmake --version
$ git clone [project URL]
$ cd [into the project]
$ mkdir build
$ cd build
$ cmake ..
$ make
$ ./hello
```

### Windows (Visual Studio)
- Make sure you have the **C++ CMake tools for Windows** installed. These should automatically be there if you selected C++ as a language when installing Visual Studio. There are more instructions on installing `CMake` in Visual Studio [here](https://docs.microsoft.com/en-us/cpp/build/cmake-projects-in-visual-studio?view=vs-2019#installation).
- Install `libcurl` using `vcpkg`. Open Windows PowerShell and run the following
    ```
    git clone https://github.com/Microsoft/vcpkg.git
    cd vcpkg
    .\bootstrap-vcpkg.bat
    .\vcpkg.exe integrate install
    .\vcpkg.exe install --triplet x64-windows curl[tool]
    ```
- Optionally, you can also install [boost](https://www.boost.org/) via `vcpkg` if you would like to use it. (This will take a while, 30mins or more!) 
    ```
    vcpkg install --triplet x64-windows boost
    ```
- Setup the project in Visual Studio
    - Make sure you open the folder with this repository via **File > Open > Folder** menu item and Visual Studio should automatically detect and run `cmake.exe`.
    - Double click on `CMakeLists.txt` and that should trigger a build. The output window should not show any errors once CMake completes.
    - There are more details on using CMake with Visual Studio [here](https://docs.microsoft.com/en-us/cpp/build/cmake-projects-in-visual-studio?view=vs-2019#ide-integration).  

- You should now be able to right click on `helloworld.cpp` in Solution Explorer and be able to `compile` and `debug`(run) the code

### Other platforms or if you want to debug
- You can find installation instructions for `CMake` [here](https://cmake.org/install/).
- You can find installation instructions for `libcurl` [here](https://curl.haxx.se/docs/install.html).
