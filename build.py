from subprocess import call
from os import remove, environ
from sys import argv

JAVA_HOME = environ.get('JAVA_HOME')

if '-actions' in argv:
    call(f'cl /LD /MD /Od /I "{JAVA_HOME}/include" /I "{JAVA_HOME}/include/win32" MediaProps.cpp ole32.lib Shell32.lib')
else:
    call('"C:/Program Files (x86)/Microsoft Visual Studio/2019/Community/VC/Auxiliary/Build/vcvars64.bat" && ' +
        f'cl /LD /MD /Od /I "{JAVA_HOME}/include" /I "{JAVA_HOME}/include/win32" MediaProps.cpp ole32.lib Shell32.lib')

    remove('MediaProps.obj')
    remove('MediaProps.exp')
    remove('MediaProps.lib')
    print('\nDone!')