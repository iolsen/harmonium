#!/bin/bash
export rev=`hg log -l1|grep changeset|grep -o  ":\([[:alnum:]]\+\)"|grep -o "\([[:alnum:]]\+\)"`
echo "Replacing all occurences of {REV} in $1 with the current hg revision "$rev
perl -pi -e 's/{REV}/'$rev'/g' $1
