# GuideMe Recipes

GuideMe Recipes is a library mod that teaches **GuideMe** how to draw recipes that come from other mods.

GuideMe builds wiki-style guide books inside the game, and a guide page can place a recipe directly into the text. On its own it only understands the recipes Minecraft itself ships, so a page that reaches for a modded crusher, infuser or altar gets a "couldn't find a handler" notice where the recipe box should be. This mod supplies the missing handlers, so a guide can document modded crafting without every author writing a renderer of their own.

There is nothing to set up. Install it alongside GuideMe and the recipes start drawing. On 26.1.2 it is needed on the server too, since the server only sends recipes to a client that asks for them.

## Supported Mods

| Mod                           | 1.21.1 | 26.1.2 |
| ----------------------------- | :----: | :----: |
| Actually Additions            |   ✅   |   ❌   |
| Advanced AE                   |   ✅   |   ✅   |
| Apotheosis                    |   ✅   |   ✅   |
| Apothic Enchanting            |   ✅   |   ✅   |
| Ars Nouveau                   |   ✅   |   ❌   |
| Astral Sorcery                |   ✅   |   ❌   |
| Cognition                     |   ✅   |   ❌   |
| Create                        |   ✅   |   ❌   |
| Create Big Cannons            |   ✅   |   ❌   |
| Create Crafts & Additions     |   ✅   |   ❌   |
| Draconic Evolution            |   ✅   |   ❌   |
| Ender IO                      |   ✅   |   ✅   |
| Eternal Starlight             |   ✅   |   ❌   |
| Extended Industrialization    |   ✅   |   ❌   |
| ExtendedAE                    |   ✅   |   ✅   |
| Farmer's Delight              |   ✅   |   ❌   |
| Forbidden Arcanus             |   ✅   |   ❌   |
| Genetics: Resequenced         |   ✅   |   ❌   |
| Hexerei                       |   ✅   |   ❌   |
| Immersive Engineering         |   ✅   |   ❌   |
| Industrial Foregoing          |   ✅   |   ❌   |
| Integrated Dynamics           |   ✅   |   ✅   |
| Iron's Spells 'n Spellbooks   |   ✅   |   ❌   |
| Just Dire Things              |   ✅   |   ✅   |
| Malum                         |   ✅   |   ❌   |
| Mekanism                      |   ✅   |   ❌   |
| Mob Grinding Utils            |   ✅   |   ❌   |
| Modern Industrialization      |   ✅   |   ❌   |
| More Red                      |   ✅   |   ✅   |
| Mystical Agriculture          |   ✅   |   ✅   |
| Mystical Automation           |   ✅   |   ✅   |
| NauTec                        |   ✅   |   ❌   |
| Neo Vitae                     |   ✅   |   ✅   |
| Occultism                     |   ✅   |   ✅   |
| Oritech                       |   ✅   |   ✅   |
| Pagan's Blessing              |   ✅   |   ❌   |
| PneumaticCraft: Repressurized |   ✅   |   ❌   |
| Powah                         |   ✅   |   ✅   |
| Productive Bees               |   ✅   |   ✅   |
| Productive Trees              |   ✅   |   ❌   |
| Pylons                        |   ✅   |   ✅   |
| Reliquary Reincarnations      |   ✅   |   ✅   |
| RFTools Utility               |   ✅   |   ❌   |
| Roots Classic                 |   ✅   |   ✅   |
| Super Factory Manager         |   ✅   |   ✅   |
| Tree Tap                      |   ✅   |   ❌   |
| XyCraft Machines              |   ✅   |   ✅   |

Recipe types are looked up by name while the game loads, so a mod that is not installed is simply skipped. This mod never requires any of the mods it supports.

## What It Covers

*   **By hand** — the mods above get purpose-built renderers that lay each recipe out the way its machine works, with the numbers that matter: chances, energy, time, heat, and the pedestals or rituals around it.
*   **Fluids and chemicals** — fluid slots draw the fluid itself rather than a bucket, and Mekanism's gases, slurries, infusions and pigments draw as themselves.
*   **Vanilla gaps** — campfire cooking, smoking and stonecutting, which GuideMe leaves uncovered.
*   **Everything else** — an optional generic renderer, behind the client config option `generic_fallback_for_unknown_types` (off by default), draws any remaining recipe type from the vanilla recipe interface.

## For Guide Authors

Recipes are placed the same way GuideMe already does it, with no extra tags to learn. `<Recipe id="..." />` draws one named recipe, and `<RecipeFor id="..." />` finds a recipe that makes a given item. Any recipe type this mod handles becomes available to both.

To see what a mod's recipes actually look like before writing a page against them, run `/guideme open @p gmrecipes:examples gmrecipes:<mod_id>.md` in game: it opens a hidden book that draws one loaded example of every recipe type that mod registers, so an uncovered type shows up as a handler error with its name on it.
