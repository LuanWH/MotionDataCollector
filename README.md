<h2>Sony SW2 Data Collector</h2>

<h3>Description</h3>
--------
<p>MotionDataCollector is designed to collect motion data from Sony SmartWatch 2.
 It is part of a cyber-physical project held by SeSaMe Centre.</p>

<h3>Features</h3>
-----------------------------
<ul>
<li>Collect data from the accelerometer on Sony SmartWatch 2</li>
<li>Filter out gravity using customized filter parameter</li>
<li>Customized sensor frequency</li>
<li>Customized labelling of data</li>
<li>Display data in both table and graph</li>
<li>Customized graph display</li>
<li>Save graph to image</li>
<li>Share data via various APIs</li>
<li>Easy access to data - SDcard Storage</li>
</ul>

<h3>Dependencies</h3>
------------
Two Android library projects are included in the Sony Add-on SDK;
SmartExtensionAPI and SmartExtensionUtils. And <a href="http://androidplot.com/">AndroidPlot Library</a> is used to support graph plotting.

* The SmartExtensionAPI is an Android library project that contains the API.
* The SmartExtensionUtils is an Android library project that contain helper
classes which simplify extension development. We highly recommend using this
project when developing extensions. The SmartExtensionUtils have a dependency
to the SmartExtensionAPI Android library project.
* The AndroidPlot library is an API for creating dynamic and static charts within your Android application. It’s designed from the ground up for the Android platform, is compatible with all versions of Android from 1.6 onward and is used by 500+ apps on the Play Store.
