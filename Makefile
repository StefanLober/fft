all:
	cd ooura && $(MAKE) lib
	cd fft-test && $(MAKE) fft-test

clean:
	cd ooura && $(MAKE) clean
	cd fft-test && $(MAKE) clean

