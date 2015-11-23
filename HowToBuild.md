As indicated on the SystemRequirements wiki page, all the necessary tools have to be installed to build indic-keyboards from source.

Check out the source from the web repository. Steps to checkout can be found [here](http://code.google.com/p/indic-keyboards/source/checkout).

---

## How to build (Linux) ##

Assuming all the necessary tools are present, browse to the directory _indic-keyboards_.
Now, the building is architecture specific. That is, if a 32-bit version of JDK has been installed, please use **build-linux-x86.xml** build file. If the JDK is a 64-bit version, **build-linux-x86\_64.xml** has to be used.

<u>Steps</u>

1.  Browse to the directory _indic-keyboards_.
<p>
2.  Place the Eclipse SWT JAR package in the same folder. The version which has been used is 3.4. Higher versions can also be used.<br>
<p>
3.  Run the following :<br>
<pre><code>ant -f build_filename.xml build -Dswt=the_swt_filename.jar<br>
</code></pre>
For example,<br>
<pre><code>ant -f build-linux-x86.xml build -Dswt=swt-swt-3.4-gtk-linux-x86.jar<br>
</code></pre>
After executing this, a file <b>libIndicKeyboards-x86.so.1.0</b> or <b>libIndicKeyboards-x86_64.so.1.0</b> will be created.<br>
<p>
4.  Create the executable JAR by running the following<br>
<pre><code>ant -f build_filename.xml jar -Dswt=the_swt_filename.jar<br>
</code></pre>
A new directory called <b>dist</b> will be created which will contain the complete package to run indic-keyboards.<br>
<br>
<b>NOTE</b> : To clean, run the following :<br>
<pre><code>ant -f build_filename.xml clean<br>
</code></pre>
The clean target will remove the <i>dist</i> folder along with other intermediate files.<br>
<br>
<hr />

<h2>How to build (Windows)</h2>

Microsoft Visual C++ is a <b>must</b>. It can be obtained from the link provided in the SystemRequirements wiki page.<br>
<br>
To install Apache Ant, follow the steps described in the installation section of the manual <a href='http://ant.apache.org/manual/index.html'>here</a>

Assuming that everything has been properly set up, follow these steps :<br>
<br>
<u>Steps</u>

1. Open Microsoft Windows Command Prompt which is packaged with Microsoft Visual C++.<br>
<p>
2. Browse to the directory which contains the source.<br>
<p>
3.  Run the following :<br>
<pre><code>ant -f build_filename.xml build -Dswt=the_swt_filename.jar<br>
</code></pre>
For example,<br>
<pre><code>ant -f build-win32-x86.xml build -Dswt=swt-3.4-win32-win32-x86.jar<br>
</code></pre>
After executing this, two files <b>indic-keyboards-syshook.dll</b> and <b>indic-keyboards-opChars.dll</b> will be created.<br>
<p>
4.  Create the executable JAR by running the following<br>
<pre><code>ant -f build_filename.xml jar -Dswt=the_swt_filename.jar<br>
</code></pre>
A new directory called <b>dist</b> will be created which will contain the complete package to run indic-keyboards.<br>
<br>
<hr />

<b>NOTE</b> :<br>
<ol><li>Experts can modify the build script to their liking. This needs expertise with Apache Ant's working.<br>
</li><li>Other build targets can be listed using<br>
<pre><code>ant -f build_filename.xml -p<br>
</code></pre>