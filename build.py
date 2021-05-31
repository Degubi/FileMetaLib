from subprocess import call
from os import environ

JAVA_HOME = environ.get('JAVA_HOME')

call(f'cl /LD /MD /Od /I "{JAVA_HOME}/include" /I "{JAVA_HOME}/include/win32" MediaProps.cpp ole32.lib Shell32.lib')