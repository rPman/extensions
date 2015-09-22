#!/bin/sh -e

BASEDIR=~/extensions.git/yatran

jars()
{
    find "$1" -iname '*.jar' | 
    while read l; do
	echo -n "$l:"
    done
    echo
}

MAINCLASS=org.luwrain.extensions.yatran.TranslateTool
CP="$(jars "$BASEDIR/lib/")$(jars "$BASEDIR/jar/")"
exec java -cp "$CP" "$MAINCLASS" "$@"
