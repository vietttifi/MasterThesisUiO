import sys
import re
import numpy
import matplotlib.pyplot as pp

if __name__ == "__main__":
	#Get param from terminal
	filetext = sys.argv[1]
	filetext1 = sys.argv[2]
	filetext2 = sys.argv[3]
	filetext3 = sys.argv[4]

	f = open(filetext)
	content_source = f.read()
	f1 = open(filetext1)
	content_source1 = f1.read()
	f2 = open(filetext2)
	content_source2 = f2.read()
	f3 = open(filetext3)
	content_source3 = f3.read()
	f.close()
	f1.close()
	f2.close()
	f3.close()

	matches = [float(i) for i in re.findall('\S[\d+\.]*\d+(?=\s+\{i\.viettt)', content_source, re.DOTALL)]
	matches1 = [float(i) for i in re.findall('\S[\d+\.]*\d+(?=\s+\{i\.viettt)', content_source1, re.DOTALL)]
	matches2 = [float(i) for i in re.findall('\S[\d+\.]*\d+(?=\s+\{i\.viettt)', content_source2, re.DOTALL)]
	matches3 = [float(i) for i in re.findall('\S[\d+\.]*\d+(?=\s+\{i\.viettt)', content_source3, re.DOTALL)]
	
	print(numpy.mean(matches), len(matches))
	print(numpy.mean(matches1), len(matches1))
	print(numpy.mean(matches2), len(matches2))
	print(numpy.mean(matches3), len(matches3))

	pp.plot(numpy.arange(0, len(matches)*5,5), matches)
	pp.plot(numpy.arange(0, len(matches1)*5,5), matches1)
	pp.plot(numpy.arange(0, len(matches2)*5,5), matches2)
	pp.plot(numpy.arange(0, len(matches3)*5,5), matches3)

	pp.legend(['1Hz', '10Hz', '100Hz','700Hz'], loc='lower right')
	pp.xlabel('seconds')
	pp.ylabel('%CPU')
	pp.title('CPU USAGE')
	pp.show()