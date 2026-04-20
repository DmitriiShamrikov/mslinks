import os
import sys

if len(sys.argv) < 2:
	exit(1)

data = []
for i in range(1, len(sys.argv)):
	with open(sys.argv[i], 'rb') as f:
		name, ext = os.path.splitext(os.path.basename(sys.argv[i]))
		data.append((name, f.read()))

for entry in data:
	print(f'\tpublic static final byte[] {entry[0]} = get{entry[0].capitalize()}();')

print()

for entry in data:
	print(f'\tprivate static byte[] get{entry[0].capitalize()}() {{')
	print(f'\t\treturn ByteArray(', end='')

	size = len(entry[1])
	idx = 0
	while idx < size:
		if idx != 0:
			print(',', end='')

		lineSize = min(16, size - idx)
		bytesLine = [entry[1][idx + i] for i in range(lineSize)]
		print('\n\t\t\t' + ', '.join('0x{:02x}'.format(i) for i in bytesLine), end='')
		idx += lineSize

	print(f'\n\t\t);')
	print(f'\t}}')
	print()
