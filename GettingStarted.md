# How to download #

Visit http://code.google.com/p/indic-keyboards/downloads/list and select the download that is most suitable. The file name will look like:
  * For windows - indic-keyboards-x.x-winyy-xzz.zip. For example : indic-keyboards-0.1-win32-x86.zip states that the download is of version 0.1 for Windows 32-bit running on x86 architecture.
  * For linux - indic-keyboards-x.x-linux-xyy.zip. For example : indic-keyboards-0.1-linux-x86\_64.zip states that the download is of version 0.1 for Linux running on x86\_64 or amd64 architecture.


---

# How to unzip and get started #

The software will be distributed as a .zip file. Extract the contents to a folder of your choice. The folder will contain the following:

```
indic-keyboards-x.x-yyy-xzz.zip
	|
	|-<kblayouts>  : Contains all the XML files corresponding to the keyboard layouts
	|-<resources>  : Contains the images and  system tray icons
	|-indic-keyboards-sysHook.dll* 
	|-indic-keyboards-opChars.dll*
	|-libIndicKeyboards-x86.so.1.0*
	|-libIndicKeyboards-x86_64.so.1.0*
	|-preferences.conf : Configuration information file for linux only.
	|-indic-keyboards-xxx-x86_xx.jar : Executable java archive.
	|-License document :  Apache 2.0 license.
*Depends on the package downloaded.
```

Open the folder and select/click the file named indic-keyboards-xxx-x86\_xx.jar. A splash screen is invoked and the icon appears in the system tray/notification area.

![http://indic-keyboards.googlecode.com/svn/docs/images/startup.jpg](http://indic-keyboards.googlecode.com/svn/docs/images/startup.jpg)
<p>
<p>
<p>
<b>NOTE</b> :<br>
<ol><li>In Linux, <i><b>super user privileges</b></i> are required to use indic-keyboards. Either login as root or use <b><i>sudo</i></b> to start the software.<br>
</li><li>If clicking the JAR file doesn't start the software, right click the JAR file and try opening it with Java. If this too doesn't work, open a <b>terminal</b> and type in the following :<br>
<pre><code>[user@localhost /]$ cd to-the-unzipped-directory<br>
[user@localhost to-the-unzipped-directory]$ sudo java -jar indic-keyboards-xxx-x86_xx.jar<br>
</code></pre>
</li><li>A 64-bit JRE will require the 64-bit version of indic-keyboards. Similarly, it is not possible to run the 64-bit version of indic-keyboards on a 32-bit JRE. On the other hand, it is possible to install a 32-bit JRE on a 64-bit machine in which case, download and run the 32-bit indic-keyboard release.<br>
Right click on the icon. The main menu will pop-up as shown below</li></ol>

<img src='http://indic-keyboards.googlecode.com/svn/docs/images/mainmenu.jpg' />
<p>
<p>Select the language you want to type in. For example, if you select Kannada, a sub menu will show up containing the offered keyboard layouts for Kannada. Select the keyboard layout you want to use. A keyboard layout here is the mapping of language-specific characters onto the English characters on the standard QWERTY keyboard layout.<br>
Indic-Keyboards offers two types of input methods:<br>
<ol><li>Popular Keyboard Layouts : Keyboard layouts like KaGaPa, Tamil99, Remington, Inscript (For all languages) etc.<br>
</li><li>Phonetic Styled Input : In this method, the user has to type in the characters as it sounds or is pronounced.<br>
<p>Once selected, a balloon pops up and displays the language and the keyboard layout selected.  The tray icon is changed to the “a” of the language selected.</li></ol>

<img src='http://indic-keyboards.googlecode.com/svn/docs/images/languageselected.jpg' />
<p>
<p>To start typing in the selected language-layout, press “Alt + F12” or go to the main menu and click on “Enable” option provided.  The software can be enabled or disabled whenever the user wants to alternate between English and the selected language.<br>
Open the application you want to run the software on. The application can be Notepad, WordPad, Microsoft Word, Microsoft PowerPoint etc. for Microsoft Windows and Linux applications Gedit, OpenOffice.org, Kate etc. <b>The software practically runs on ANY application which supports Unicode.</b>
<p>
For a user who is not familiar with a keyboard layout he/she is using, an option to view the current layout is provided. He can do so by clicking on “Show Current Layout” option in the menu. For example, if the language selected is Kannada and the layout selected is KaGaPa, then by clicking on the “Show Current Layout” option, the following image appears which can be used to familiarize with the layout. The Kannada KaGaPa layout is as follows:<br>
<br>
<img src='http://indic-keyboards.googlecode.com/svn/docs/images/kagapa.png' />


