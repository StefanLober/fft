#pragma once

#include <memory>

#if defined(WIN32) || defined (WIN64)
    #ifdef OOURALIB_EXPORTS
        #define OOURA_API __declspec(dllexport)
    #else
        #define OOURA_API __declspec(dllimport)
    #endif

    #pragma warning(disable: 4251)
#else
    #define OOURA_API
#endif

class OOURA_API ooura
{
private:
    std::unique_ptr<double[]> _data;
    std::unique_ptr<int[]> _ip;
    std::unique_ptr<double[]> _w;
    int _size;

public:
    ooura(int size);
    void fft();
    double* data() { return _data.get(); }
    int size() { return _size; }
};