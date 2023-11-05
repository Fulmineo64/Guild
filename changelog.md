# 0.4.3
- Updated to Minecraft 1.19.2

# 0.4.2
- Updated to Minecraft 1.19.1

# 0.4.1
- Updated to Minecraft 1.19

# 0.4.0
- Added the possibility to specify an exp reward for the player 
- Now the rewarded player exp and profession exp are shown on the quest tooltip
- Added an exp reward to all current existing quests
- Player exp, as with all other values, can be configured with datapacks
- Updated to Minecraft 1.18.2

# 0.3.8
- Added support to the OctoEconomyAPI thanks to ExcessiveAmountsOfZombies (aka TheThonk)!
- Added example datapack for a drag-and-drop integration with EightsEconomy (OctoEconomyAPI's implementation)
- Improved handling of reward pools without the "max" value in the "number" property (although specifying it is always recommended)
- Accepted professions of removed datapacks will be now discarded, avoiding them to clutter the profession cap.
- Slightly adjusted the positioning of the Quests Screen's elements 

# 0.3.7
- Added new configurable values maxProfessions and maxQuestsPerProfession that limit the max acceptable Professions and the max available quest for said Professions.
Both of these values are capped at 7.
- Updated russian translations thanks to Shelbikk [#12](https://github.com/Fulmineo64/Guild/issues/12)!
- Updated the textures for the Guild Master and the Guild Master Table thanks to Alexander210806!

# 0.3.6
- Updated to Minecraft 1.18.1
- Update the textures of the Guild Master and the Guild Master's Table thanks to Alexander210806!

# 0.3.5
- Updated to Minecraft 1.18

# 0.3.4
- Updated Chinese translations thanks to 103sakamoto!

# 0.3.3
- Added missing title to The Guild's configuration screen

# 0.3.2
- Added configuration for the following values: "Available quests expiration ticks", "Quest generation ticks", "Max quests per generation" and "Display unlocked tasks and rewards"
- Added ModMenu integration

# 0.3.1
- Changed Beekeeper Guild Master's trade level from 2 to 3
- Added workaround for mods that call readCustomDataFromNbt on ServerPlayerEntity multiple times

# 0.3.0
- Added Beekeeper profession
- Added nbt support for entities, useful to specify a subset of entities or even players
- Added support for nbt tags for icons with the property "icon_tag"
- Reworked the Quests Screen to greatly improve performance and to better handle stack's custom names

# 0.2.0
- Changed "entity" task type to "slay".
- Cleaned up accesswideners
- Added new "nbt" property to Quest Professions useful to request (or reward) items with custom nbt
- Updated Russian translations (thanks to M1XB0X)
- Added new quests to the Guard Profession involving illagers
- Added new rewards exclusive to the Guard Profession: Steak starting at lvl 5 and Regeneration Potion starting at lvl 10

# 0.1.2
- Added new Profession, the Chef
- Added cooked variants of foods to their respective professions (meat for hunters, fishes and kelp for fisherman)
- Improved translation system for Professions
- Buffed exps and rewards for most Professions
- Revisited the level distribution of tasks for all the Professions
- Now Professions with the same Identifier placed in different namespaces will be merged instead of overridden

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