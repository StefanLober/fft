CXXFLAGS := -fPIC -O3 -Wall -Werror -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
CXX := g++
MAJOR := 0
MINOR := 1
NAME := ooura

lib: lib$(NAME).so

lib$(NAME).so: $(NAME).o
	$(CXX) $(CXXFLAGS) -shared $^ -o $@

clean:
	$(RM) *.o *.so*

