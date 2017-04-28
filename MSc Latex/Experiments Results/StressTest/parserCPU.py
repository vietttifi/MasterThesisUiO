import sys
import re
import numpy
import matplotlib.pyplot as pp

if __name__ == "__main__":
	#Get param from terminal
	filetext = sys.argv[1]

	f = open(filetext)
	content_source = f.read()
	f.close()


	matches = [float(i) for i in re.findall('\S[\d+\.]*\d+(?=\s+\{i\.viettt)', content_source, re.DOTALL)]
	
	print(numpy.mean(matches))

	pp.plot(numpy.arange(0, len(matches)*5,5), matches)

	pp.legend(['%CPU'], loc='upper left')
	pp.xlabel('seconds')
	pp.ylabel('%CPU')
	pp.title('CPU USAGE')
	pp.show()
