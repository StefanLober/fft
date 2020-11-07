#pragma once

#include <memory>

#ifdef OOURALIB_EXPORTS
#define OOURA_API __declspec(dllexport)
#else
#define OOURA_API __declspec(dllimport)
#endif

class OOURA_API ooura
{
private:
#pragma warning(disable: 4251)
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