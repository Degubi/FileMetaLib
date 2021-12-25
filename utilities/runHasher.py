from subprocess import call, DEVNULL
from os import remove

BUILD_COMMAND = f'cl /MD /Od HasherUtil.c ole32.lib Shell32.lib Propsys.lib'

call(f'"C:/Program Files (x86)/Microsoft Visual Studio/2019/Community/VC/Auxiliary/Build/vcvars64.bat" && {BUILD_COMMAND}', stdout = DEVNULL, stderr = DEVNULL)
print(call('HasherUtil.exe'))
remove('HasherUtil.exe')
remove('HasherUtil.obj')