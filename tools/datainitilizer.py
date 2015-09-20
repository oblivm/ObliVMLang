import argparse, sys, re

def process_int(file, output):
	toScreen = False
	if output == '[empty]':
		toScreen = True
		writer = sys.stdout
	else:
		writer = open(output, 'w')
	content = open(file).read().strip()
	val = int(content)
	for i in range(0, 32):
		toPrint = 0
		if val % 2 == 1:
			toPrint = 1
		writer.write(str(toPrint))
		val /= 2
	if not toScreen:
		writer.close()

def process_array(file, output):
	toScreen = False
	if output == '[empty]':
		toScreen = True
		writer = sys.stdout
	else:
		writer = open(output, 'w')
        content = open(file).read().strip()
        content = re.sub(r'\D', ' ', content)
	elems = [x for x in content.split(' ') if len(x) > 0]
	for content in elems:
		val = int(content)
		for i in range(0, 32):
			toPrint = 0
			if val % 2 == 1:
				toPrint = 1
			val /= 2
			writer.write(str(toPrint))
	if not toScreen:
		writer.close()


def main():
	parser = argparse.ArgumentParser(description=
		"Initializing the input data file")
	parser.add_argument('inputfiles', type=str, nargs=1)
	parser.add_argument('--type', dest='type', default='int', 
		help='processing the input type (default = int)' \
			 'options include: int, array')
	parser.add_argument('--output', dest='output', default='[empty]', 
		help='the destination file. if not set, the print to screen')
		
	args = parser.parse_args()
	if args.type == 'int':
		process_int(args.inputfiles[0], args.output)
	elif args.type == 'array':
		process_array(args.inputfiles[0], args.output)
	else:
		raise Exception('Unknown input type: '+args.type)
		
main()
			 
