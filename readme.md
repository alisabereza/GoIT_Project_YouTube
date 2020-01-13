#YouTube API: Searching and Playing Video

JAVA Core final project.

##Project description

This is JavaFX application created to search and play videos from YouTube using YouTube API.

##Prerequisites
Using of YouTube API for searching requires YouTube apikey for authentication purposes.
Valid apikey should be added to *youtube.properties* file.

##Usage

To start application, run **Main** method in **Player** class.
 
###Initial Window
Initial window contains *Part of video name* text field and two buttons: **Show** and **Advanced**.

**Show** - clicking on this button returns 25 videos where *Part of vidoe name* matches part of video name.
Resulting table contains the following columns:
 * *Video Name*
 * *Channel* (clickable)
 * *Published Date*
 * *Thumbnail* 
 * *Play* (button to play a video)

**Advanced** - clicking on this button adds additional text fields: *MAX Results* (number of videos to show) and *Number Of Days* (to show only videos uploaded not earlier than *entered number of days* ago).
Clicking on *Channel* link opens Channel information in same Window: Chanel name, description, avatar and 10 latest videos.

**Play** - clicking on Play button opens Player in separate window.

###Player Window
Player window contains History information and a Player itself. When the first video is played, history is not accessible. 
Player window can be closed to select next video to play. When next video is played, history becomes available.


