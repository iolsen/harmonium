#!/bin/bash

#######################
# get latest and build
#######################
#hg pull
#hg update --clean
export VERSION=`grep "VERSION\s*=\s*" org/dazeend/harmonium/Harmonium.java | grep -o "[0-9]*\.[0-9]*\.[0-9]*"`
echo Building release archives for version ${VERSION}...
#./rev-update.sh org/dazeend/harmonium/Harmonium.java
#./build.sh


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
cd Harmonium
flip -u CHANGELOG COPYING INSTALL UNINSTALL UPGRADE README USAGE
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
cp wrapper/wrapper.jar Harmonium/lib/

mkdir Harmonium/logs

export LINUXFILE=harmonium-linux-${VERSION}.tar.gz
if [ -f ${LINUXFILE} ]
then
rm ${LINUXFILE}
fi
tar czf ${LINUXFILE} Harmonium
rm -rf Harmonium
