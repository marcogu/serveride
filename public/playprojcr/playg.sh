#!/bin/zsh

# use case :
# ./playg.sh /Users/marco/Documents/temp/tempcreate ptt1 /Users/marco/Documents/backup/tpfolder
container=$1
projName=$2
templateFolder=$3

cd $container

if [ ! -d $projName ]; then
	mkdir $projName
fi

cd $projName

mkdir conf
mkdir lib
mkdir project
mkdir -p public/images
mkdir public/javascripts
mkdir public/stylesheets
mkdir test
mkdir -p app/controllers
mkdir app/assets
mkdir app/views

cp "$templateFolder/tp.build.sbt.tp" ./build.sbt
cp "$templateFolder/tp.plugins.sbt.tp" project/plugins.sbt
cp "$templateFolder/tp.build.properties.tp" project/build.properties
cp "$templateFolder/tp.application.conf.tp" conf/application.conf
cp "$templateFolder/tp.routes.tp" conf/routes
cp $templateFolder/tp.jquery-3.1.1.js.tp > public/javascripts/jquery-3.1.1.js
if [ -f "$templateFolder/tp.DefaultController.scala.tp" ]; then
	cp "$templateFolder/tp.DefaultController.scala.tp" app/controllers/DefaultController.scala
else
	echo "package controllers\n\nimport play.api.mvc._\nclass Application extends Controller {\n\tdef index = Action{ Ok(\"hello word\")}\n}" > app/controllers/Application.scala	
fi

echo "$projName did create at $container"

