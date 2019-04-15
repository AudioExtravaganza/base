# Use python 3
from os import getcwd, path, listdir
from sys import platform
from shutil import copy
def main():
    os_dirs = {
        'darwin' : '~/Library/Application Support/SuperCollider/Extensions/',
        'linux' : '~/.local/share/SuperCollider/Extensions/',
        'win32' : '~/AppData/Local/SuperCollider/Extensions/'
    }
    loc = path.expanduser(os_dirs[platform])
    toCopy = listdir("./classes")
    for file in toCopy:
        source = path.join(".", path.join("classes", file))
        dest = path.join(loc, file)
        print("Copying %s to %s" % (source, dest))
        copy(source, dest)

if __name__ == '__main__':
    main()
else:
    print('Not ran correctly.')