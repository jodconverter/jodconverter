# LibreOffice Portable

**JODConverter** should be compatible with LibreOffice Portable. All the documentation has been taken
from [here](https://github.com/jodconverter/jodconverter/issues/28#issuecomment-333811431) and is not actively
maintained. If you find that this documentation should be updated, please open a GitHub issue with the details about
what should be updated.

Some important notes on how to use LibreOffice Portable with JODConverter:

1. The main problem occurring is that your LibreOffice settings contain machine/OS specific settings. Therefore it is
   usually the case that if you distribute LibreOffice Portable with your software together with your preferred
   settings, that the settings work on one machine, but cause LibreOffice to crash on another machine. To solve this
   problem, use the safe mode of LibreOffice to factory reset the settings, then close it and make a copy of the
   settings. Then restart LibreOffice normally with the GUI, select your preferred settings, then close LibreOffice
   again. The settings will end up in registrymodifications.xcu. Unfortunately, LibreOffice adds a lot of extra settings
   to the rest of the settings in the folder, therefore you just copy the registrymodifications.xcu to your factory
   resetted settings copy you made before changing the settings. The registrymodifications.xcu will contain some extra
   settings you did not select (as a good dev, you might want to clean them out until they are only the ones you
   selected explicitly for better maintenance). However, the extra settings are not machine dependent and thus cause no
   crash. Use the one folder of settings per LibreOffice instance mechanism (template profile setting) of JODConverter
   so that none of your task processing LibreOffice instances will ever mess with your fragile settings.

2. LibreOffice Portable can be used like an installed LibreOffice. Just set the LibreOffice home of JODConverter to
   LibreofficePortable/App/libreoffice to make it use the soffice.exe and such, it is not required to make JODConverter
   use the LibreofficePortable*.exe. The latter will even cause cleanup-problems, because JODConverter terminates the
   binary but LibreOffice Portable does not clean up properly.

3. It is important that you do not have a LibreOffice installed on your machine aside from the LibreOffice Portable you
   are using. This will make your settings work flawlessly with LibreOffice Portable (everything will work great all the
   time), but as you transfer to another machine with LibreOffice installed, things will not work anymore. So your
   developer machine should have no LibreOffice installed, just as your testing computer should not have one installed.
   This saves you a lot of time debugging spurious problems.

4. If you want to make pipes work using JODConverter, but your system runs on a x64 machine, you need to additionally
   distribute some libraries from the x64 version of LibreOffice, since there exists no x64 version of
   LibreOfficePortable. To get the libraries, you install a LibreOffice x64 version on your computer (this might be in
   conflict with the point 3 above, at least uninstall it again after you are done with this or use another machine than
   your daily dev machine). Then you get the following dlls from the LibreOffice program folder: jpipe.dll, jpipx.dll,
   sal3.dll, uwinapi.dll. These dlls will have to be on the Java path in order for your JODConverter to be able to use
   the pipes. Additionally, you will have to install the Microsoft Visual C++ redistributable 2013 (x64) on the
   machine (This might change in the future, so if it does not work, you will have to use a dll analyzer program to find
   out what the dlls are linked to and then look for those names on the web to find out what these dlls belong to. Get
   the dlls from trustworthy sources. You do not want to distribute a virus with your application.). This is because
   some of the above dlls are liked against some of the redistributable dlls. I know it is hard and you want to cry, but
   it will work in the end. But that seems to be the cost of making this stuff fully portable.