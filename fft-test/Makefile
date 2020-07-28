CXXFLAGS := -fPIC -O3 -Wall -Werror -I../ooura -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
CXX := g++
MAJOR := 0
MINOR := 1
NAME := fft-test
VERSION := $(MAJOR).$(MINOR)

fft-test: main.o
	cp ../ooura/*.so ./
	$(CXX) $(CXXFLAGS) $^ -o $@ -L../ooura -looura -pthread

main.o: main.cpp
	$(CXX) $(CXXFLAGS) -c main.cpp $^

clean:
	$(RM) *.o *.so* $(NAME)


