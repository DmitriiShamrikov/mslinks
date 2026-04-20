import uno
import msgbox

from scriptforge import CreateScriptService

# current working directory is where the odt/xls/etc file is located
# so saving right next to it
CODE_FILE = 'guids.java'

def ShowMessage(msg):
	mb = msgbox.MsgBox(uno.getComponentContext())
	mb.addButton('OK')
	mb.show(msg, 0, '')

def EscapeStr(str) :
	return str.replace('\\', '\\\\&#x200B;').replace('<', '\\<').replace('>', '\\>').replace('-', '&#8209;')

def GenerateMarkdown():
	doc = CreateScriptService("Calc")
	docRange = None
	if type(doc.CurrentSelection) == str:
		docRange = doc.CurrentSelection
	else :
		docRange = doc.CurrentSelection[0]
	data = doc.getValue(docRange)

	md = []
	if len(data) > 0:
		widths = [0] * len(data[0])
		for row in data:
			for idx, cell in enumerate(row):
				value = cell
				if type(cell) == float:
					value = str(cell)
				widths[idx] = max(widths[idx], len(EscapeStr(value)))

		md = [
			'|'.join(cell.ljust(widths[idx]) if idx != len(data[0]) - 1 else cell for idx, cell in enumerate(data[0])), '\n',
			'|'.join('-' * w for w in widths), '\n',
		]

		for row in data[1:]:
			md += ['|'.join(cell.center(widths[idx]) if len(cell) < 2 else EscapeStr(cell).ljust(widths[idx]) for idx, cell in enumerate(row)), '\n']
	
		if data[0][0] == 'CLSID':
			filename = 'guids.md'
		elif data[0][0] == 'Name':
			filename = 'paths.md'
		else:
			filename = 'export.md'
 
		if len(md) > 0:
			with open(filename, 'w') as f:
				f.writelines(md)
			ShowMessage(f'Saved {sum(len(line) for line in md)} bytes to {filename}')
	doc.Dispose()

def GenerateCodeLine(row, idxes, isBroken):
	line = ''
	if row[idxes[2]]:
		line += f'\t\t{row[idxes[2]]}=\n'
	line += f'\t\tregisterClsid("{row[idxes[0]]}", "{row[idxes[1]]}"'
	if isBroken:
		line += f', ItemIDUnknown.class'
	elif row[idxes[3]] != '':
		line += f', {row[idxes[3]]}'
	line += ');\n'
	return line

def GenerateRegistry() :
	doc = CreateScriptService("Calc")
	docRange = None
	if type(doc.CurrentSelection) == str:
		docRange = doc.CurrentSelection
	else :
		docRange = doc.CurrentSelection[0]
	data = doc.getValue(docRange)

	groupNames = [
		'Windows XP only',
		'Windows XP+',
		'Windows Vista only',
		'Windows Vista+',
		'Windows 7 only',
		'Windows 7+',
		'Windows 8 only',
		'Windows 8+',
		'Windows 10 only',
		'Windows 10+',
		'Windows 11'
	]
	groups = [[] for _ in range(len(groupNames))]
	if len(data) > 0:
		idxes = [0, 7, 9, 10]
		labels = [data[0][i] for i in idxes]
		expectedLabels = ['CLSID', 'Name', 'Variable', 'Options']
		if labels != expectedLabels:
			raise Exception('Unexpected table header')

		for row in data[1:]:
			only = len([cell for cell in row[1:6] if cell == '+']) == 1
			isBroken = len([cell for cell in row[1:6] if cell == 'x']) > 0
			groupIdx = -1
			if row[1] == '+':
				groupIdx = 0 if only else 1
			elif row[2] == '+':
				groupIdx = 2 if only else 3
			elif row[3] == '+':
				groupIdx = 4 if only else 5
			elif row[4] == '+':
				groupIdx = 6 if only else 7
			elif row[5] == '+':
				groupIdx = 8 if only else 9
			elif row[6] == '+':
				groupIdx = 10
			else :
				raise Exception('Guid ' + row[0] + ' is not compatible with anyhting')

			if len(groups[groupIdx]) == 0:
				groups[groupIdx].append('\n\t\t// ' + groupNames[groupIdx] + '\n')
			groups[groupIdx].append(GenerateCodeLine(row, idxes, isBroken))

		with open(CODE_FILE, 'w') as f:
			for group in groups:
				f.writelines(group)
		ShowMessage(f'Saved {sum(len(code) for code in groups)} lines to {CODE_FILE}')
	doc.Dispose()


g_exportedScripts = (GenerateMarkdown, GenerateRegistry)
