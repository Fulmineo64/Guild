# 0.1.2
- Added new Profession, the Chef
- Added cooked variants of foods to their respective professions (meat for hunters, fishes and kelp for fisherman)
- Improved translation system for Professions
- Buffed exps and rewards for Hunter and Gatherer

# 0.1.1
- Changed the commands introduced in 0.1.0 to be available only to operators and above
- Added example datapack
- Added back "label" property to Professions as now it works properly on servers

# 0.1.0
- Removed "label" property from Professions as it doesn't work properly in multiplayer
- Updated to Fabric API 0.40.1+1.17
- Added over-complicated Profession Requirements system, useful to limit early access to professions
- Added /guild licence <profession\> command to generate profession licences
- Added /guild exp <profession\> <exp\> command to change the player's profession exp
- Added /guild quest <profession\> command to generate a new quest for the specified profession
- Centered the level text on Quests Screen

# 0.0.8
- Removed "quest" command
- Added "guild reset" command, that removes all professions and all accepted and available quests
- Added "guild clear" command, that removes all accepted and available quests

# 0.0.7
- Added Russian translations, many thanks to RiverCat!
- Refactored the code that handles the buttons in the Quest Screen.

# 0.0.6
- Rewards get now dropped on the ground when the inventory is full
- Fixed completing quests for resigned professions
- Added extra checks to prevent crashes for missing or invalid professions

# 0.0.5
- Added Chinese translations, many thanks to Peakstep233!

# 0.0.4
- Removed Lumberjack tasks from Gatherer
- Fixed bug with double clicking to complete a quest
- Added Profession Resignment to abandon a specific profession
- Integrated the Profession Resignment with the Profession Licence
- Added optional "label" property to Professions, use it to give the Profession a localized name without adding a lang file

# 0.0.3
- Fixed the Guild Master table being undroppable
- Fixed Quest Screen not working in multiplayer
- Fixed "Deliver Kelp" task and added "Deliver Sea Pickle" to fisherman
- Updated correctly the Profession's Exp when completing a quest
- Added lumberjack profession
- Improved the item completition count
- Added information on which new tasks and rewards get unlocked at level up

# 0.0.2
- Pets' kills now count towards accepted quests, this applies only to Tameables and not to Golems
- Added quests for curing zombie villagers and spawning golems
- Added the possibility to use custom icons for tasks and rewards
- Added more visible errors for the end user to troubleshoot faulty datapacks
- Added extra validators for task pools

# 0.0.1
- First alpha release