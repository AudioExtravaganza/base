import json
import os

def main():
    data = getData()
    if data:
        toUpdate = cloneNew(data["repos"])
        updateExisting(toUpdate)

def getData():
    file = open("data.json")
    info = "".join(str(x) for x in file.readlines())
    file.close()
    try:
        info = json.loads(info)
    except:
        print("Something went wrong reading file")
        info = None
    return info

def cloneNew(repos : list):
    local = os.listdir(".")
    existing = []
    for i in range(len(repos)):
        if not repos[i]["repo"] in local:
            print("\n\n", repos[i]["repo"], ":", sep="")
            os.system("git clone https://github.com/%s/%s" % (repos[i]["owner"], repos[i]["repo"]))
        else:
            existing.append(repos[i])
    return existing


def updateExisting(repos):
    local = os.listdir(".")
    for i in range(len(repos)):
        if repos[i]["repo"] in local:
            print("\n\n", repos[i]["repo"], ":", sep="")
            os.chdir(repos[i]["repo"])
            os.system("git pull")
            os.chdir("../")
        else:
            print("Could not update: %s" % repos[i]["repo"])
main()

