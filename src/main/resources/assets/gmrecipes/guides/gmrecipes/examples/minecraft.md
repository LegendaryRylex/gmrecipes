---
navigation:
  title: Minecraft
  icon: minecraft:crafting_table
  parent: index.md
---
# Minecraft

Crafting, smelting, blasting and smithing are drawn by GuideMe itself. Smoking, campfire cooking
and stonecutting have no GuideMe handler, so they only draw when the client option
*Generic fallback for unknown types* is on. A handler error on those three with the option off is
the expected result, and them drawing with it on is the smoke test for the generic renderer.

<gmrecipes:RecipeExamples mod="minecraft" />
