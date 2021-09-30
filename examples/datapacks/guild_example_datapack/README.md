# The Guild example datapack

## Introduction

Hello and welcome to this example datapack for The Guild mod.

In this brief guide you'll learn how to create your own datapack for adding tasks, rewards and professions to The Guild mod.

## Defining a namespace

To start customizing our datapack we'll need to decide its name, or better its namespace.

A namespace is a short string of charachters used to identify our datapack and its contents and to prevent conflicts with other datapacks.

A namespace is tipically composed of lowercase letters and underscores like this: *my_guild_datapack*.

## Copying the files

Now we can copy the entire **guild_example_datapack** folder and rename it to our namespace.

From now on we'll work on the copied folder.

## Datapack customization

Inside the newly copied folder we'll find the following files and folders:
- a **pack.mcmeta**, which is the metadata file for datapacks, the description contained in it can be customized.

- a **pack.png** which is the logo of the datapack, it can be customized to an image of your choice but it must be called pack.png to work.

- the **data** folder, which contains the files that will be applied to the game.

## Applying the namespace

Inside the **data** folder we'll find the *example* folder.

*example* is the namespace used by the **guild_example_datapack**, all occurrences of it are to be replaced by the defined datapack namespace from earlier.

To start we can rename the *example* folder to our chosen namespace.

## Introduction to The Guild's data structure

Quests to be generated require 2 things: a Profession and at least one Pool.

A Profession specifies a list of tasks and rewards grouped by a name and additional metadata.

Pools are a data structure that can be used for both task and rewards.
A Pool is composed by a name and a list of items.

## Defining a profession

We can specify the profession to which the quests will be generated.

## Defining a task pool

To start creating our custom quests we can edit the *baker_tasks.json* file which is the example's task pool.

The first step is to rename the file to the desired name, 

## Advices

Professions and Pools in datapacks can reference the original files of The Guild's mod.

You can also reference Professions and Pools from other datapacks though it is not recommended.
