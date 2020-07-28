CXXFLAGS := -fPIC -O3 -Wall -Werror -I../ooura -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
CXX := g++
NAME := fft-test
OUTDIR := ../out

fft-test: main.o
	mkdir -p $(OUTDIR)
	$(CXX) $(CXXFLAGS) $^ -o $(OUTDIR)/$@ -L$(OUTDIR)/ -looura -pthread

main.o: main.cpp
	$(CXX) $(CXXFLAGS) -c main.cpp $^

clean:
	$(RM) *.o $(OUTDIR)/$@ $(NAME)
