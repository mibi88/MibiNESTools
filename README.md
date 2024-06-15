# MibiNESTools

It will include a CHR editor, a nametable editor and a level editor for my
future platformer. Currently, it is still very incomplete and unstable, so use
it at your own risk!

# Creating plugins

Create a new project. Add MibiNESTools to the dependencies.

In this project, create a file that extends the `Editor` class.

In the root folder of the project, create a plugin.properties file:

```
folder=target/classes
editorClass=com.example.ExampleEditor
targetVersion=v.1.0a2
```

`folder` is the folder where all the compiled `.class` files of your project
are.

`editorClass` is the class you just created, that extends the `Editor` class.

`targetVersion` is the version of MibiNESTools this plugin is made for.

Compile the plugin. You can then load it by opening MibiNESTools.

# Loading a plugin

To load a plugin. go in **Edit > Load a plugin** and selecting the
`plugin.properties` file of the plugin you want to load. It should now appear in
the **New file...** and **Open with...** menus as any other editor.

