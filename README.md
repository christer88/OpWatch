#OpWatch
Plugin for Op management of a spigot server via IRC. Allows sign checking, player relations, player ips, and a few other things.

###Install
Simply download the .jar that is in /target/ and put into your spigot plugins folder, restart your server, edit the config, and restart your server again.
</br>
/spigot/plugins/  
&nbsp;&nbsp;&nbsp;&nbsp;|  
&nbsp;&nbsp;&nbsp;&nbsp;|--- OpWatch  
&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\\--- config.yml  
&nbsp;&nbsp;&nbsp;&nbsp;\\--- OpWatch.jar  


###Commands
######_`'emergencykill`_
Kills the bot in a brutal way.
######_`'wipesign [ID]`_
Wipes the sign (or last sign if no ID) and replaces the text with</br>
<sup>`Sign Wiped`</br>
`By The`</br>
`Minecraft Team`</sup>
######_`'aboutplayer <Player>`_
Returns userID and other player names that are linked to that player, See <a href="#Linking">Linking</a>
######_`'lookupip <Player>`_
Returns the all IPs that the specified user has logged in from
######_`'about`_
Returns version info and the like about this plugin
######_`'help`_
Prints out hopefully useful help about all the commands available

####<a id="Linking"></a>Linking
1st Degree Links are other players that have logged on from the same IP as that player
2nd Degree Links are other players who have logged on from an IP that a 1st degree link has used

##</br>
---
######I have used APIs and other projects that I did not make, all of these are listed below, and the code can also be found in /APIs/
2. [PircBotX](https://github.com/TheLQ/pircbotx) - Under GNU GPL v3  
<sub>I believe the licences above are compatible with the GNU General Public License V3 (GPL) that applies to this project, but please please correct me if I'm wrong.</sub>
