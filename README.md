# What is Monkey Shines?
*Monkey Shines* was a game published by Fantasoft in 1997, and the game is now abandonware. The purpose of this project is to rebuild the game for Java so it is cross-platform and does not require emulation to run properly. It is not intended to look 100% graphically identical in all menus (the editor in particular can use some good advancements over the original). This is mostly a project for personal benefit.

# Why?
Having heavy nostalgia for a game I can not run even in a emulator, I decided to take up as a challenge to see if I can re-create the game in a form that will be more accessible and still function mostly identical. I consider working on this a hobby. Since there is no reason to keep the code to myself I decided there is no harm in hosting it on github. I have no intention of making a profit from any of this; full credit goes to the employees at Fantasoft who created this game. I merely want to take that game idea and port it to be more accessible today. I have an affinity for games with simple premises that can be played quickly and present a challenge.

# Getting Started
To just start playing the game and using the level editor, jump to the wiki: https://github.com/ErikaRedmark/monkey-shines-java-port/wiki. If you wish to work with the source, my setup is Eclipse using EGit which would be the easiest way to get running. 

There are two main points of entry in the code:
- LevelEditor: This class starts the Level Editor. Both the main game and level editor share the same basic engine. The best way to start is to create a new world and use the default resource pack. This default pack is basically a subset rip of the original game's 'Spooked'. 
- MonkeyShines: This class allows you to start the actual game. At this time the menu systems are mostly functional and should be easy to get started.

# Status
Project tracking now moved to issue tracking instead of a task list since the majour core components have been finished.

# Final Words
If anyone wishes to help, especially if you have played the game before, feel open to contact me (Erika Redmark) at maniacmonkeyinspace@gmail.com. 

# Original Credits
Listing of the original people who worked on the original game. These are basically the people you would find if you looked in the Credits section of the original game:

Fantasoft Owner: Tim Phillips

Programming: Mark Elliott

Artwork: Alan Lau

Music and Sound Effects: Petteri Lajunen, Ville-Eemeli Kakela

Level Design: 

Spooked - Vern Jensen

Spaced Out - Ville-Eemeli Kakela & Petteri Lajunen

In The Drink - Alexander Minidis

About the House - Alexander Minidis

In The Swing - Vern Jensen

Special Thanks: 

David Lau

# Extra Original Credits
Some more people associated with the game, not in the original development but nevertheless important contributers.

Philip Roy: Writer of original level design tutorials and documents, creator of 'Bonzo Hits Cyberspace' hosted on Fantasofts website, as well as winner of "Level Design Contest" for original game with the world 'Phils Mac World'.