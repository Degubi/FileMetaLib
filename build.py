from subprocess import call
from os import remove, environ, makedirs
from shutil import move
from sys import argv

JAVA_HOME = environ.get('JAVA_HOME')
BUILD_COMMAND = f'cl /LD /MD /Od /I "{JAVA_HOME}/include" /I "{JAVA_HOME}/include/win32" MediaPropUtils.c ole32.lib Shell32.lib Propsys.lib'

call(BUILD_COMMAND if '-actions' in argv else f'"C:/Program Files (x86)/Microsoft Visual Studio/2019/Community/VC/Auxiliary/Build/vcvars64.bat" && {BUILD_COMMAND}')
remove('MediaPropUtils.obj')
remove('MediaPropUtils.exp')
remove('MediaPropUtils.lib')

makedirs('./src/main/resources', exist_ok = True)
move('MediaPropUtils.dll', './src/main/resources/MediaPropUtils.dll')