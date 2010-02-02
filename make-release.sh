#!/bin/bash

#######################
# update from repo
#######################
hg pull $1
hg update --clean

#######################
# get version info
#######################
export VERSION=`grep "VERSION\s*=\s*" org/dazeend/harmonium/Harmonium.java | grep -o "[0-9]*\.[0-9]*\.[0-9]*"`
export rev=`hg log -l1|grep changeset|grep -o  ":\([[:alnum:]]\+\)"|grep -o "\([[:alnum:]]\+\)"`

echo "Building release archives for version ${VERSION} (${rev})..."

perl -pi -e 's/{REV}/'$rev'/g' org/dazeend/harmonium/Harmonium.java

#######################
# compile
#######################
./build.sh

if [ -e Harmonium ]
then
rm -rf Harmonium
fi

#####################
# build linux tarball
#####################
mkdir Harmonium
cp CHANGELOG Harmonium/
cp COPYING Harmonium/
cp linux-INSTALL Harmonium/INSTALL
cp linux-UNINSTALL Harmonium/UNINSTALL
cp linux-UPGRADE Harmonium/UPGRADE
cp README Harmonium/
cp USAGE Harmonium/
cp CONTRIBUTORS Harmonium
cd Harmonium
flip -u CHANGELOG COPYING INSTALL UNINSTALL UPGRADE README USAGE CONTRIBUTORS
cd ..
cp Harmonium.jar Harmonium/

mkdir Harmonium/bin
cp wrapper/harmonium Harmonium/bin/
cp wrapper/wrapper Harmonium/bin/

mkdir Harmonium/conf
cp conf/linux-wrapper.conf Harmonium/conf/wrapper.conf

mkdir Harmonium/lib
cp libs/* Harmonium/lib/
cp wrapper/libwrapper.so Harmonium/lib/
cp wrapper/linux-wrapper.jar Harmonium/lib/wrapper.jar

mkdir Harmonium/logs

export LINUXFILE=harmonium-linux-${VERSION}-${rev}.tar.gz
if [ -f ${LINUXFILE} ]
then
rm ${LINUXFILE}
fi
tar czf ${LINUXFILE} Harmonium

rm -rf Harmonium


#####################
# build windows zip
#####################
mkdir Harmonium
cp CHANGELOG Harmonium/
cp COPYING Harmonium/
cp win-INSTALL Harmonium/INSTALL
cp win-UNINSTALL Harmonium/UNINSTALL
cp win-UPGRADE Harmonium/UPGRADE
cp README Harmonium/
cp USAGE Harmonium/
cp CONTRIBUTORS Harmonium/
cd Harmonium
flip -m CHANGELOG COPYING INSTALL UNINSTALL UPGRADE README USAGE CONTRIBUTORS
cd ..
cp Harmonium.jar Harmonium/

mkdir Harmonium/bin
cp wrapper/*.bat Harmonium/bin/
cp wrapper/wrapper.exe Harmonium/bin/

mkdir Harmonium/conf
cp conf/win-wrapper.conf Harmonium/conf/wrapper.conf

mkdir Harmonium/lib
cp libs/* Harmonium/lib/
cp wrapper/wrapper.dll Harmonium/lib/
cp wrapper/win-wrapper.jar Harmonium/lib/wrapper.jar

mkdir Harmonium/logs

export WINFILE=harmonium-windows-${VERSION}-${rev}.zip
if [ -f ${WINFILE} ]
then
rm ${WINFILE}
fi
zip -qr ${WINFILE} Harmonium
rm -rf Harmonium
