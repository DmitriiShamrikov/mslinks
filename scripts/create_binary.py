import os
import sys

if len(sys.argv) < 2:
	exit(1)

bytes = []
with open(sys.argv[1], 'rb') as f:
	bytes = f.read()

name, ext = os.path.splitext(os.path.basename(sys.argv[1]))
print(f'public static final byte[] {name} = get{name.capitalize()}();')
print()
print(f'private static byte[] get{name.capitalize()}() {{')
print(f'\treturn ByteArray(', end='')

size = len(bytes)
idx = 0
while idx < size:
	if idx != 0:
		print(',', end='')

	lineSize = min(16, size - idx)
	bytesLine = [bytes[idx + i] for i in range(lineSize)]
	print('\n\t\t' + ', '.join('0x{:02x}'.format(i) for i in bytesLine), end='')
	idx += lineSize

print(f'\n\t);')
print(f'}}')

