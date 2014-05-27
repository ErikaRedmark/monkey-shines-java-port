# What is Monkey Shines?
*Monkey Shines* was a game published by Fantasoft in 1997, and the game is now abandonware. The purpose of this project is to rebuild the game for Java so it is cross-platform and does not require emulation to run properly. It is not intended to look 100% graphically identical in all menus (the editor in particular can use some good advancements over the original). This is mostly a project for personal benefit.

# Why?
Having heavy nostalgia for a game I can not run even in a emulator, I decided to take up as a challenge to see if I can re-create the game in a form that will be more accessible and still function mostly identical. I consider working on this a hobby. Since there is no reason to keep the code to myself I decided there is no harm in hosting it on github. I have no intention of making a profit from any of this; full credit goes to the employees at Fantasoft who created this game. I merely want to take that game idea and port it to be more accessible today. I have an affinity for games with simple premises that can be played quickly and present a challenge.

# Getting Started
There is no easily executable way of running the game and the level editor. I work in Eclipse so that IDE is probably best to use (EGit, clone repo and import all projects is probably the easiest way)
There are two main points of entry in the code:
- LevelEditor: This class starts the Level Editor. Both the main game and level editor share the same basic engine (only slowed down, as in the original). The best way to start is to create a new world and use the default resource pack. This default pack is basically a rip of the original game's 'Spooked' with only the Bee as an available sprite and two backgrounds that prove I don't design graphics well. 
- MonkeyShines: This class allows you to start the actual game. It should open a file chooser from which you can select a .world file (this file chooser is temporary and easy way to just playtest worlds). I do NOT include any sample/demo .world files to run, so you will have to make one with the editor.

# Status
The basic game engine mechanics are coming together, but there is still quite a lot to do. This list is more for my benefit (and is probably incomplete) but if you are familiar with the original game many items should be familiar:
Game Engine
- [x] Basic tilemap (Solids, Jump-Throughs, Scenery)
- [x] Basic sprite mechanics
- [x] Bonzo death and respawn
- [x] Bonzo basic animation
- [x] Jump-Through Tile logic
- [x] Bonzo Movement
- [x] Hazards (Bombs, Dynamite. . .)
- [x] Conveyer Belts
- [x] Goodies and Yums
- [ ] Breakable Tiles
- [ ] Sprite Collisons (currently uses rough bounding box and is not precise enough)
- [ ] UI Containing other information than just main game screen
- [ ] Health
- [ ] Health Draining Sprites
- [ ] Lives
- [ ] Red keys and Exit Door
- [ ] Blue keys and Bonus Door
- [ ] Patterned backgrounds (Currently have full-screen backgrounds as stand-ins)
- [ ] Shield Powerup
- [ ] Wing Powerup
- [ ] Score
- [ ] Multipliers
- [ ] Main menu (A large task that I will break into smaller ones when the rest of the engine/level editor is complete)

#Level Editor
Please note that the current save format uses Java serialisation. Updates may destroy the ability to read old .world files, so if you do work on a world don't overdo it.
- [x] Creating and loading worlds
- [x] Saving worlds
- [x] Adding Tiles to a screen
- [x] Creating a new screen
- [x] Adding Sprites
- [x] Deleting Sprites
- [ ] Editing Sprites (Currently a bit clunky)
- [x] Adding goodies
- [x] Placing Bonzo starting position
- [x] Slowdown animations of sprites
- [x] Adding Hazards
- [x] Adding conveyer belts
- [ ] Adding breakable tiles
- [ ] Setting level background
- [ ] Indicating Authorship (Not part of original game)

#Game Content
Save file format is done; it is possible to work on worlds. Not all engine features exist though.
- [ ] Spooked
- [ ] Spaced Out
- [ ] About the House
- [ ] In the Drink
- [ ] In the Swing

#More Game Content
Some third party worlds that I would like to port to this new version as well:
- [ ] Cory's Storm Keep
- [ ] Phils Mac World
- [ ] Present for Phil

Additional worlds may be added.

# Final Words
If anyone wishes to help, especially if you have played the game before, feel open to contect me (Erika Redmark) at maniacmonkeyinspace@gmail.com. 

# Small About Me
I just recently got into the workforce as a Java developer for an Eclipse plugin. Since I prefer to have my day job be something other than an EA Games inspired death march, I pretty much have decided that games would be a hobby. I do have a game idea that is original that I would like to work on but I already had this code lying around from when I worked on it at university, and I didn't want it to just rot away.

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