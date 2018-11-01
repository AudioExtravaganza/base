from json import loads
from os import system

def main():
    data = getData()
    if data:
        updateTrees(data["repos"])


def getData():
    file = open("data.json")
    info = "".join(str(x) for x in file.readlines())
    file.close()
    try:
        info = loads(info)
    except:
        print("Something went wrong reading file")
        info = None
    return info

def updateTrees(repos : list):
    for i in range(len(repos)):
            print("\n\n", repos[i]['prefix'])
            system("git subtree pull --prefix=%s %s master" % (repos[i]['prefix'], repos[i]['remote']))



main()

