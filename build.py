from subprocess import call
from os import remove, rename, environ

JAVA_HOME = environ.get('JAVA_HOME')

call('"C:/Program Files (x86)/Microsoft Visual Studio/2019/Community/VC/Auxiliary/Build/vcvars64.bat" && ' +
    f'cl /LD /MD /Od /I "{JAVA_HOME}/include" /I "{JAVA_HOME}/include/win32" FileMetaLib.cpp ole32.lib Shell32.lib')

remove('FileMetaLib.obj')
remove('FileMetaLib.exp')
remove('FileMetaLib.lib')
print('\nDone!')