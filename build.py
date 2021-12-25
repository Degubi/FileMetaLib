from subprocess import call
from os import remove, environ, path, makedirs
from shutil import move
from sys import argv

JAVA_HOME = environ.get('JAVA_HOME')
VCVARS_FILE_DIR = 'Enterprise' if '-actions' in argv else 'Community'
BUILD_COMMAND = f'cl /LD /MD /Od /I "{JAVA_HOME}/include" /I "{JAVA_HOME}/include/win32" MediaPropUtils.c ole32.lib Shell32.lib Propsys.lib'

call(f'"C:/Program Files (x86)/Microsoft Visual Studio/2019/{VCVARS_FILE_DIR}/VC/Auxiliary/Build/vcvars64.bat" && {BUILD_COMMAND}')
remove('MediaPropUtils.obj')
remove('MediaPropUtils.exp')
remove('MediaPropUtils.lib')

if not path.exists('./src/main/resources'):
    makedirs('./src/main/resources')

move('MediaPropUtils.dll', './src/main/resources/MediaPropUtils.dll')