package ui.george.explorer

import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.File

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val dir = File("${System.getProperty("user.dir")}/test/")

        val statusBar = Label("").apply {
            padding = Insets(5.0)
        }

        val leftPane = ListView<String>().apply {
            prefWidth = 200.0

            // Order the files and folders by type and sort by name
            val (directories, files) = dir.listFiles()!!.partition { it.isDirectory }
            val file_map = files.groupBy {
                when (it.extension) {
                    "jpg", "png", "bmp" -> "image"
                    "md", "txt" -> "text"
                    else -> "other"
                }
            }
            for (folder in directories) {
                items.add(folder.name + "/").apply {
                    val file_obj = folder
                }
            }
            if ("text" in file_map) {
                for (file in file_map["text"]!!.sorted()) {
                    items.add(file.name).apply {
                        val file_obj = file
                    }
                }
            }
            if ("image" in file_map) {
                for (image in file_map["image"]!!.sorted()) {
                    items.add(image.name).apply {
                        val file_obj = image
                    }
                }
            }
            if ("other" in file_map) {
                for (file in file_map["other"]!!.sorted()) {
                    items.add(file.name).apply {
                        val file_obj = file
                    }
                }
            }
            selectionModel.selectionMode = SelectionMode.SINGLE
            selectionModel.select(0);
            
        }

        val topPane = VBox().apply {
            prefHeight = 30.0
            background = Background(BackgroundFill(Color.valueOf("#00ffff"), null, null))
            setOnMouseClicked { println("top pane clicked") }

            // Menu bar items
            val fileMenu = Menu("File")
            val actionsMenu = Menu("Actions")
            val viewMenu = Menu("View")
            val quitMenu = Menu("Quit").apply {
                items.add(MenuItem("Quit File explorer").apply {
                    setOnAction { Platform.exit() }
                })
            }

            // Tool bar items
            val homeButton = Button("Home")
            val prevButton = Button("Prev")
            val nextButton = Button("Next")
            val renameButton = Button("Rename")
            val deleteButton = Button("Delete")

            children.addAll(
                MenuBar().apply {
                    menus.add(fileMenu)
                    menus.add(actionsMenu)
                    menus.add(viewMenu)
                    menus.add(quitMenu)
                },
                ToolBar().apply {
                    items.add(homeButton)
                    items.add(prevButton)
                    items.add(nextButton)
                    items.add(renameButton)
                    items.add(deleteButton)
                })
        }

        val centrePane = Pane().apply {
            setOnMouseClicked { }
        }

        // put the panels side-by-side in a container
        val root = BorderPane().apply {
            left = leftPane
            center = centrePane
            top = topPane
            bottom = statusBar
        }

        stage.apply {
            title = "File Explorer"
            scene = Scene(root, 640.0, 480.0)
            isResizable = false
        }.show()
    }
}
