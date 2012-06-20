NutriDB
=======

You can build NutriDB from source or install the debug APK and DB from the
Downloads Tab.


Install from Downloads
----------------------

1. Download the SR24 DB and the NutriDB Debug APK from the Downloads Tab
  (http://code.google.com/p/nutridb/downloads/list) or by using wget.

    wget http://nutridb.googlecode.com/files/NutriDB-debug.v1.apk
    wget http://nutridb.googlecode.com/files/sr24.v1.db

2. Use ADB to install the DB on the SD-card of your phone or emulator.

    DBDIR=/sdcard/Android/obb/com.codebionic.android.nutridb
    DB=main.1.com.codebionic.android.nutridb.obb

    adb push ./sr24.v1.db $DBDIR/$DB

3. Use ADB to install the Debug APK.

    adb -s emulator-5554  install -r ./NutriDB-debug.v1.apk



Build from source
-----------------

1. Clone the git master branch.

    git clone https://code.google.com/p/nutridb/ ./nutridb

2. Update the project to reflect your local properties.

    android update project -n NutriDB -p ./nutridb/android/

3. Build the debug APK and install on your phone or emulator.

    pushd ./nutridb/android/

    ant debug
    adb -s emulator-5554  install -r ./bin/NutriDB-debug.apk

4. Download the SR24 database.

    popd
    pushd ./nutridb/db

    SRDIR=SP2UserFiles/Place/12354500/Data/SR24/dnload
    SR=sr24.zip

    wget http://www.ars.usda.gov/$SRDIR/$SR

5. Build the SQLite3 database from the sr24.zip file. You'll need SQLite3 and
   unzip installed and in your PATH.

    ./sr24.sh

  This will take a long time - 30 min. or so on my laptop.

6. Use ADB to install the DB on the SD-card of your phone or emulator.

    DBDIR=/sdcard/Android/obb/com.codebionic.android.nutridb
    DB=main.1.com.codebionic.android.nutridb.obb

    adb push ./sr24.v1.db $DBDIR/$DB

   You only need to build and install the DB once.



                                ---
