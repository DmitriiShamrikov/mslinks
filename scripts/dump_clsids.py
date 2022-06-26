import pprint
import winreg
import re
import sys

dumpCode = False
dumpText = False
includeRemapped = False
for arg in sys.argv:
	dumpCode |= arg == 'code'
	dumpText |= arg == 'text'
	includeRemapped |= arg == 'remap'

if not dumpCode and not dumpText:
	dumpText = True


folders = {}
# known folders were introduced in Vista
try:
	folderDescsKey = 'SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FolderDescriptions'
	with winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, folderDescsKey) as key:
		numSubKeys, numValues, _ = winreg.QueryInfoKey(key)
		for i in range(numSubKeys) :
			clsid = winreg.EnumKey(key, i).upper()

			with winreg.OpenKey(key, clsid) as clsidKey:
				name, type = winreg.QueryValueEx(clsidKey, 'Name')

			folders[clsid] = name
except FileNotFoundError:
	pass

namespaceKey = 'SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Desktop\\NameSpace'
with winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, namespaceKey) as key:
	numSubKeys, numValues, _ = winreg.QueryInfoKey(key)
	for i in range(numSubKeys) :
		clsid = winreg.EnumKey(key, i).upper()
		if not clsid.startswith('{'):
			continue

		with winreg.OpenKey(key, clsid) as clsidKey:
			name = winreg.QueryValue(clsidKey, None)

		folders[clsid] = name

# legacy CSIDL values for XP and older
csidlMap = {
	'0x0000': 'DESKTOP',
	'0x0001': 'INTERNET',
	'0x0002': 'PROGRAMS',
	'0x0003': 'CONTROLS',
	'0x0004': 'PRINTERS',
	'0x0005': 'PERSONAL',
	'0x0006': 'FAVORITES',
	'0x0007': 'STARTUP',
	'0x0008': 'RECENT',
	'0x0009': 'SENDTO',
	'0x000a': 'BITBUCKET',
	'0x000b': 'STARTMENU',
	'0x000d': 'MYMUSIC',
	'0x000e': 'MYVIDEO',
	'0x0010': 'DESKTOPDIRECTORY',
	'0x0011': 'DRIVES',
	'0x0012': 'NETWORK',
	'0x0013': 'NETHOOD',
	'0x0014': 'FONTS',
	'0x0015': 'TEMPLATES',
	'0x0016': 'COMMON_STARTMENU',
	'0x0017': 'COMMON_PROGRAMS',
	'0x0018': 'COMMON_STARTUP',
	'0x0019': 'COMMON_DESKTOPDIRECTORY',
	'0x001a': 'APPDATA',
	'0x001b': 'PRINTHOOD',
	'0x001c': 'LOCAL_APPDATA',
	'0x001d': 'ALTSTARTUP',
	'0x001e': 'COMMON_ALTSTARTUP',
	'0x001f': 'COMMON_FAVORITES',
	'0x0020': 'INTERNET_CACHE',
	'0x0021': 'COOKIES',
	'0x0022': 'HISTORY',
	'0x0023': 'COMMON_APPDATA',
	'0x0024': 'WINDOWS',
	'0x0025': 'SYSTEM',
	'0x0026': 'PROGRAM_FILES',
	'0x0027': 'MYPICTURES',
	'0x0028': 'PROFILE',
	'0x0029': 'SYSTEMX86',
	'0x002a': 'PROGRAM_FILESX86',
	'0x002b': 'PROGRAM_FILES_COMMON',
	'0x002c': 'PROGRAM_FILES_COMMONX86',
	'0x002d': 'COMMON_TEMPLATES',
	'0x002e': 'COMMON_DOCUMENTS',
	'0x002f': 'COMMON_ADMINTOOLS',
	'0x0030': 'ADMINTOOLS',
	'0x0031': 'CONNECTIONS',
	'0x0035': 'COMMON_MUSIC',
	'0x0036': 'COMMON_PICTURES',
	'0x0037': 'COMMON_VIDEO',
	'0x0038': 'RESOURCES',
	'0x0039': 'RESOURCES_LOCALIZED',
	'0x003a': 'COMMON_OEM_LINKS',
	'0x003b': 'CDBURN_AREA',
	'0x003d': 'COMPUTERSNEARME',
}

shellFolderClsids = set()
clsidsToRemove = set()
clsidsToRemove.add('{64693913-1C21-4F30-A98F-4E52906D3B56}') # this one causes crash in explorer
with winreg.OpenKey(winreg.HKEY_CLASSES_ROOT, 'CLSID') as key:
	numSubKeys, numValues, _ = winreg.QueryInfoKey(key)
	for i in range(numSubKeys) :
		clsid = winreg.EnumKey(key, i).upper()

		with winreg.OpenKey(key, clsid) as clsidKey:
			targetClsid = None
			targetCsidl = None
			try :
				with winreg.OpenKey(clsidKey, 'ShellFolder') as sf:
					shellFolderClsids.add(clsid)
					pass
			except FileNotFoundError:
				continue

			try :
				with winreg.OpenKey(clsidKey, 'Instance\\InitPropertyBag') as bagKey:
					targetClsid, _ = winreg.QueryValueEx(bagKey, 'TargetKnownFolder')
			except FileNotFoundError:
				try :
					with winreg.OpenKey(clsidKey, 'Instance\\InitPropertyBag') as bagKey:
						targetCsidl, _ = winreg.QueryValueEx(bagKey, 'TargetSpecialFolder')
				except FileNotFoundError:
					continue

			if targetClsid:
				targetClsid = targetClsid.upper()
				if targetClsid == clsid:
					continue

				if targetClsid in folders:
					folders[clsid] = folders[targetClsid]
					clsidsToRemove.add(targetClsid)
				else :
					print('Unknown target CLSID: ' + targetClsid)
			
			if targetCsidl:
				targetCsidl = targetCsidl.lower()
				if targetCsidl in csidlMap:
					folders[clsid] = csidlMap[targetCsidl]
				else:
					print('Unknown CSIDL: ' + targetCsidl + ' (CLSID: ' + clsid +')')

if not includeRemapped:
	for clsid in clsidsToRemove:
		if clsid in folders:
			del folders[clsid]

folderClsids = {}
for clsid, name in folders.items():
	name = name.replace(' ', '')
	if name not in folderClsids:
		folderClsids[name] = []
	folderClsids[name].append(clsid)
	if clsid in shellFolderClsids:
		shellFolderClsids.remove(clsid)

total = 0
if dumpCode :
	for name in sorted(folderClsids.keys()):
		addIdx = len(folderClsids[name]) > 1
		for i in range(len(folderClsids[name])) :
			clsid = folderClsids[name][i]
			suffix = ('_' + str(i)) if addIdx and i > 0 else ''
			if clsid in clsidsToRemove:
				suffix += '_remapped'
			code = 'registerClsid("' + clsid + '", "' + name + suffix + '");' 
			print(code)
			total += 1

if dumpText :
	total = 0
	for name in sorted(folderClsids.keys()):
		addIdx = len(folderClsids[name]) > 1
		for i in range(len(folderClsids[name])) :
			clsid = folderClsids[name][i]
			suffix = ('_' + str(i)) if addIdx and i > 0 else ''
			if clsid in clsidsToRemove:
				suffix += '_remapped'
			print(clsid + ': ' + name + suffix)
			total += 1

print('Found GUIDs: ' + str(total))

total = 0
print('\nUnresolved ShellFolder GUIDs:')
if dumpCode :
	for clsid in sorted(shellFolderClsids):
		code = 'registerClsid("' + clsid + '", "Unknown_' + str(total) + '");' 
		print(code)
		total += 1

if dumpText :
	total = 0
	for clsid in sorted(shellFolderClsids): 
		print(clsid)
		total += 1
print(total)