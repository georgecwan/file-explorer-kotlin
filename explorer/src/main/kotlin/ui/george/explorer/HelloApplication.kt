package ui.george.explorer

import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.File

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val home = File("${System.getProperty("user.dir")}/test/")
        var dir = home

        val centrePane = StackPane().apply {
            alignment = Pos.TOP_CENTER
        }

        val statusBar = Label("").apply {
            padding = Insets(5.0)
        }

        val leftPane = ListView<String>().apply {
            prefWidth = 200.0

            // Order the files and folders by type and sort by name
            var (directories, files) = dir.listFiles()!!.partition { it.isDirectory }
            var fileMap = files.groupBy {
                when (it.extension) {
                    "jpg", "png", "bmp" -> "image"
                    "md", "txt" -> "text"
                    else -> "other"
                }
            }
            for (folder in directories) {
                items.add(folder.name + "/")
            }
            if ("text" in fileMap) {
                for (file in fileMap["text"]!!.sorted()) {
                    items.add(file.name)
                }
            }
            if ("image" in fileMap) {
                for (image in fileMap["image"]!!.sorted()) {
                    items.add(image.name)
                }
            }
            if ("other" in fileMap) {
                for (file in fileMap["other"]!!.sorted()) {
                    items.add(file.name)
                }
            }
            selectionModel.selectionMode = SelectionMode.SINGLE
            selectionModel.select(0);
            statusBar.text = "${dir.path}/${selectionModel.selectedItem}"
            selectionModel.selectedItemProperty().addListener { _, _, newSelection ->
                val selectedFile = File("${dir.path}/${newSelection}")
                statusBar.text = selectedFile.path
                centrePane.children.clear()
                if (selectedFile.extension in listOf("md", "txt")) {
                    centrePane.children.add(TextArea(selectedFile.readText()).apply {
                        prefWidthProperty().bind(centrePane.widthProperty())
                        prefHeightProperty().bind(centrePane.heightProperty())
                        isWrapText = true
                        isEditable = false
                    })
                } else if (selectedFile.extension in listOf("jpg", "png", "bmp")) {
                    centrePane.children.add(ImageView(Image(selectedFile.toURI().toString())).apply {
                        fitWidthProperty().bind(centrePane.widthProperty())
                        fitHeightProperty().bind(centrePane.heightProperty())
                        isPreserveRatio = true
                    })
                }
            }
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
