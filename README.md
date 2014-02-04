This repository contains the source code for the TerraMobile Android app.

Please see the [issues](https://github.com/PauloLuan/terramobile/issues) section to
report any bugs or feature requests and to see the list of known issues.

## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## Building

The build requires [Maven](http://maven.apache.org/download.html)
v3.1.1+ and the [Android SDK](http://developer.android.com/sdk/index.html)
to be installed in your development environment. In addition you'll need to set
the `ANDROID_HOME` environment variable to the location of your SDK:

```bash
export ANDROID_HOME=/opt/tools/android-sdk
```

## Clone the repository:

	git clone git@github.com:PauloLuan/TerraMobile.git

## Include OSMBonusPack Into Maven Local Repository 

The OSMBonusPack Library doesn’t exist in the Maven center repository. To include manually into local repository, go to the folder 'app/dependencies'

	cd app/dependencies

Then type the following command: 

	mvn install:install-file -Dfile=osmbonuspack_v4.1.jar -DgroupId=org.osmbonuspack -DartifactId=osmbonuspack -Dversion=4.1 -Dpackaging=jar

After satisfying those requirements, the build is pretty simple:

* Run `mvn clean package` from the `app` directory to build the APK only
* Run `mvn clean install` from the root directory to build the app and also run
  the integration tests, this requires a connected Android device or running
  emulator

See [here](https://github.com/PauloLuan/terramobile/wiki/Building-From-Eclipse) for
instructions on building from [Eclipse](http://eclipse.org).

## Acknowledgements

It also uses many other open source libraries such as:

* [OSMDroid](https://code.google.com/p/osmdroid)
* [OSMBonusPack](https://code.google.com/p/osmbonuspack/)
* [Jackson](http://jackson.codehaus.org/)
* [ORMLite](http://ormlite.com/)
* [SpringAndroid](http://projects.spring.io/spring-android/)
* [ActionBarSherlock](https://github.com/JakeWharton/ActionBarSherlock)
* [android-maven-plugin](https://github.com/jayway/maven-android-plugin)
* [RoboGuice](https://github.com/roboguice/roboguice)
* [ViewPagerIndicator](https://github.com/JakeWharton/Android-ViewPagerIndicator)

These are just a few of the major dependencies, the entire list of dependencies
is listed in the [app's POM file](https://github.com/PauloLuan/terramobile/blob/master/app/pom.xml).

## Contributing

Please fork this repository and contribute back using
[pull requests](https://github.com/PauloLuan/terramobile/pulls).

Any contributions, large or small, major features, bug fixes, additional
language translations, unit/integration tests are welcomed and appreciated
but will be thoroughly reviewed and discussed.
