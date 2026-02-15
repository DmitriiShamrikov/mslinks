import os
import sys

import zipfile

def PrintBinary(bytes, width) :
	size = len(bytes)
	idx = 0
	while True:
		lineSize = min(width, size - idx)
		bytesLine = [bytes[idx + i] for i in range(lineSize)]
		print('\t\t\t' + ', '.join('0x{:02x}'.format(i) for i in bytesLine), end='')
		idx += lineSize
		if idx >= size :
			print()
			break
		print(',')

if len(sys.argv) < 2:
	exit(1)

with zipfile.ZipFile(sys.argv[1], 'r') as archive:
	for filename in archive.namelist() :
		name, ext = os.path.splitext(os.path.basename(filename))
		print(f'\tpublic static final byte[] {name} = get{name.capitalize()}();')

	print()
	
	for filename in archive.namelist():
		with archive.open(filename, 'r') as f :
			bytes = f.read()

			name, ext = os.path.splitext(os.path.basename(filename))
			print(f'\tprivate static byte[] get{name.capitalize()}() {{')
			print('\t\treturn ByteArray(')
			PrintBinary(bytes, 16)
			print('\t\t);')
			print('\t}\n')

