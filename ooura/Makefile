CXXFLAGS := -fPIC -O3 -Wall -Werror -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
CXX := g++
NAME := ooura
OUTDIR := ../out

lib: lib$(NAME).so

lib$(NAME).so: $(NAME).o
	mkdir -p $(OUTDIR)
	$(CXX) $(CXXFLAGS) -shared $^ -o $(OUTDIR)/$@

clean:
	$(RM) *.o $(OUTDIR)*.so*

