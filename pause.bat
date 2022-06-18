@echo off
echo Current file: %0
echo Current directory: %cd%
if not "%*" == "" (
	echo Command line: %*
)
pause