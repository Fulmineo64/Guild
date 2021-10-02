# The Guild example datapack

## Introduction

Hello and welcome to this example datapack for The Guild mod.

In this brief guide you'll learn how to create your own datapack for adding tasks, rewards and professions to The Guild mod.

## Defining a namespace

To start creating your own datapack the first thing you'll need to do is to define its namespace.

A namespace is a short string of characters, used to identify your datapack and its contents.

A namespace is typically composed of lowercase letters and underscores like this: *my_guild_datapack*.

## Copying the files

Let's start by copying the entire **guild_example_datapack** folder and rename it to your new namespace.

From now on you'll work on the copied folder.

## Datapack customization

Inside the newly copied and renamed folder you'll find the following files and folders:
- a **pack.mcmeta**, which is the metadata file for datapacks, the description contained in it can be customized.

- a **pack.png** which is the logo of the datapack, it can be customized to an image of your choice but it must be called pack.png to work.

- the **data** folder, which contains the files that will be applied to the game.

## Applying the namespace

Inside the **data** folder you'll find the *example* folder.

*example* is the namespace used by the **guild_example_datapack**, all occurrences of it are to be replaced by the defined datapack namespace from earlier.

To start you can rename the *example* folder to your chosen namespace.

## Introduction to The Guild's data structure

Quests to be generated correctly require 3 things: a Profession, one task Pool and one reward Pool.

A Profession specifies a list of tasks and rewards grouped by a name and additional metadata.

Pools are a data structure that can be used for both tasks and rewards.

## Defining a task pool

To create your own quest task you can edit the first .json files in the **pools** folder.

Inside you'll find a structure like this:

```json
{
	"name": "example:stones",
	"data": [
		{
			"type": "item",
			"name": "minecraft:stone",
			"level": {
				"min": 1,
				"max": 20
			},
			"number": {
				"min": 16,
				"max": 32
			},
			"unit_worth": 20,
			"unit_time": 60,
			"unit_exp": 5,
			"weight": 100
		}
	]
}
```

> Note: The *example* in the name property should be replaced with your namespace. The level property should be removed if you don't want to limit your task to the Profession's levels.

## The Pool's data structure

Let's take an in-depth look to the properties available in the Quest Pool:

- **name**, used to link the quest pool to a Profession, as a task or as a reward. It should be in the format *namespace:pool_name*. The pool's file should be called like this *pool_name.json*. Replace *namespace* with your namespace and *pool_name* with a name that identifies the contents of the pool.

- **data**, an array of Quest Pool Data.

Properties of a Quest Pool Data:

- **type**, can be one of these values: "item", "slay", "summon", "cure". Type "item" requires the player to deliver the requested item, "slay" requires the player to slay the specified entity, "summon" requires the player to summon the requested entity (eg. an Iron Golem) and "cure" requires the player to cure the specified entity (eg. a Zombie Villager). For rewards the type property should always be "item".

- **name**, the identifier of the selected type. If the type is "item" it should be an item identifier (eg. "minecraft:stone") otherwise it should be an entity identifier (eg. "minecraft:creeper").

- **level**, an optional property, establishes the Profession's level range required to access the Quest Pool Data. Specify the values through the **min** and **max** child properties.

- **number**, establishes the unit count range for the Quest Pool Data. Specify the values through the **min** and **max** child properties.

- **unit_worth**, the value of a single unit of the Quest Pool Data, used to determine the value of tasks and rewards. Example: if a "minecraft:stone" item task has an "unit_worth" of 20 and a "minecraft:emerald" reward has an "unit_worth" of 100 the player will need to bring 5 stones to receive an emerald as reward.

- **unit_time**, exclusive for tasks, the time available in seconds for each unit to get the full reward of the quest.

- **unit_exp**, exclusive for tasks, the Profession's exp granted for each unit on quest completion.

- **weight**, specifies how common the task or reward is, the higher the value the more common it will be.

## Defining a reward pool

Defining a reward pool it is the same as defining a task pool, without specifing the properties exclusive to tasks.

```json
{
	"name": "example:rewards",
	"data": [
		{
			"type": "item",
			"name": "minecraft:emerald",
			"number": {
				"min": 1,
				"max": 64
			},
			"unit_worth": 100,
			"weight": 100
		}
	]
}
```
> Note: The *example* in the name property should be replaced with your namespace.

If you don't want to create a custom reward pool (or want to add yours along the default rewards)
you can use The Guild's default rewards in a profession with the identifier "guild:common_rewards".

## Defining a profession

We can now specify the profession to which the quests will belong.

```json
{
	"name": "example:stonecutter",
	"icon": "minecraft:stone",
	"label": "Stonecutter",
	"guild_master_level": 1,
	"levels_pool": "guild:common_levels",
	"task_pools": [
		"example:stones"
	],
	"reward_pools": [
		"guild:common_rewards"
	]
}
```
> Note: The *example* in the name property should be replaced with your namespace.
## The Profession's data structure

Let's take an in-depth look to the properties available in the Profession:

- **name**, used to identify the Profession. It should be in the format *namespace:profession_name*. The Profession's file should be called like this *profession_name.json*. Replace *namespace* with your namespace and *profession_name* with a name that identifies the Profession.

- **icon**, an Item identifier to use as an icon in the Quests Screen. Choose an item that fits the Profession.

- **label**, an optional field that can be used to specify the Profession's readable name, instead of loading it from the language file.

- **guild_master_level**, specifies the trade level of the Guild Master for the Profession. Use 1 for the lowest level.

- **levels_pool**, specifies the Levels Pool for the Profession, a Levels Pool is a list of Levels in which is contained the required exp to level up. An example can be found here: https://github.com/Fulmineo64/Guild/blob/master/src/main/resources/data/guild/quests/levels/guild/common_levels.json

- **task_pools**, an array of Pools' identifiers, specify your task Pools by **name** here.

- **reward_pools**, an array of Pools' identifiers, specify your reward Pools by **name** here.


## Final notes

Make sure to use the correct namespace in Professions and Pools.

Professions in datapacks can reference the default Pools of The Guild's mod and even other datapack's Pools.

## A more in-depth look

To learn more about The Guild's data structures you can take a look at the ones used by The Guild itself here:

https://github.com/Fulmineo64/Guild/tree/master/src/main/resources/data/guild/quests