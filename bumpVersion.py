#!/usr/bin/python3
import sys
import json
import os
import re

ARTIFACT_ID= 'basePage'

def usage():
    print('Usage: bumpVersion -v NEWVERSION')
    print('       bumpVersion [major|minor|patch|dev}')
    sys.exit(1)

def getCurrentVersion():
    version = ''
    onArtifact = False
    with open('pom.xml') as f:
        for line in f:
            if onArtifact:
                match = re.match('\\s*<version>(.*)</version>\\s*', line)
                if match:
                    version = match.group(1)
                    break
            match = re.match('\\s*<artifactId>' + ARTIFACT_ID + '</artifactId>\\s*', line)
            if match:
                onArtifact = True
    sArray = version.split('.')
    iArray = list(map(lambda s: int(s), sArray))
    if len (iArray) == 4:
        return iArray
    return iArray + [0]

def fmtVersion(vers):
    v = "{0}.{1}.{2}".format(vers[0], vers[1], vers[2]);
    if vers[3] == 0:
        return v
    return v + ".{0}".format(vers[3])

def bumpMajor(vers):
    return [vers[0] +1, 0, 0, 0]

def bumpMinor(vers):
    return [vers[0], vers[1] +1, 0, 0]

def bumpPatch(vers):
    return [vers[0], vers[1], vers[2] +1, 0]

def bumpDev(vers):
    return [vers[0], vers[1], vers[2], vers[3] +1]


bumpFuncs = {
        'major': bumpMajor,
        'minor': bumpMinor,
        'patch': bumpPatch,
        'dev': bumpDev}

def parseArgs():
   if len(sys.argv) < 2:
       usage()
   if sys.argv[1] == '-v':
       if len(sys.argv) < 3:
           usage()
       return sys.argv[2]
   if not sys.argv[1] in bumpFuncs:
       usage()
   currentVersion = getCurrentVersion()
   return fmtVersion(bumpFuncs[sys.argv[1]](currentVersion))


def updateVersion(newVersion):
    onArtifact = False
    outLines = []
    with open('pom.xml') as f:
        for line in f:
            if onArtifact:
                match = re.match('\\s*<version>(.*)</version>\\s*', line)
                if match:
                    outLines.append('    <version>{0}</version>\n'.format(newVersion))
                    onArtifact = False
                    continue
            outLines.append(line)
            match = re.match('\\s*<artifactId>' + ARTIFACT_ID + '</artifactId>\\s*', line)
            if match:
                onArtifact = True
    with open('pom.xml', 'w') as fw:
        fw.writelines(outLines)



if __name__ == '__main__':
    newVersion = parseArgs();
    print("Setting version to " + newVersion);
    updateVersion(newVersion)

