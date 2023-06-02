package ui.george.explorer

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import java.io.File

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val home = File("${System.getProperty("user.dir")}/test/")
        val dir = SimpleStringProperty(home.path)

        val centrePane = StackPane().apply {
            alignment = Pos.TOP_CENTER
        }

        val statusBar = Label("").apply {
            padding = Insets(5.0)
        }

        val leftPane = ListView<String>().apply {
            // Override cell factory to handle double clicks on cells
            setCellFactory { lv ->
                val cell = ListCell<String>()
                cell.textProperty().bind(cell.itemProperty())
                cell.setOnMouseClicked { e ->
                    if (e.clickCount == 2 && !cell.isEmpty) {
                        val selectedFile = File("${dir.get()}/${cell.item}")
                        if (selectedFile.isDirectory) {
                            dir.set(selectedFile.path)
                        }
                    }
                }
                cell
            }
            // Order the files and folders by type and sort by name
            fun refreshList() {
                items.clear()
                for (file in File(dir.get()).listFiles()!!.sorted()) {
                    items.add(file.name)
                }
                selectionModel.select(0)
                statusBar.text = "${dir.get()}/${selectionModel.selectedItem}"
            }

            fun updatePreview(displayFile: File) {
                centrePane.children.clear()
                if (!displayFile.canRead()) {
                    centrePane.children.add(Label("File cannot be read").apply {
                        font = Font.font(20.0)
                    })
                }
                else if (displayFile.extension in listOf("md", "txt")) {
                    centrePane.children.add(TextArea(displayFile.readText()).apply {
                        prefWidthProperty().bind(centrePane.widthProperty())
                        prefHeightProperty().bind(centrePane.heightProperty())
                        isWrapText = true
                        isEditable = false
                    })
                }
                else if (displayFile.extension in listOf("jpg", "png", "bmp")) {
                    centrePane.children.add(ImageView(Image(displayFile.toURI().toString())).apply {
                        fitWidthProperty().bind(centrePane.widthProperty())
                        fitHeightProperty().bind(centrePane.heightProperty())
                        isPreserveRatio = true
                    })
                }
                else {
                    centrePane.children.add(Label("Unsupported Type").apply {
                        font = Font.font(20.0)
                    })
                }
            }

            prefWidth = 200.0
            selectionModel.selectionMode = SelectionMode.SINGLE
            refreshList()
            updatePreview(File("${dir.get()}/${selectionModel.selectedItem}"))
            selectionModel.selectedItemProperty().addListener { _, _, newSelection ->
                val selectedFile = File("${dir.get()}/${newSelection}")
                statusBar.text = selectedFile.path
                updatePreview(selectedFile)
            }
            dir.addListener { _, _, _ -> refreshList() }
            setOnKeyPressed { e ->
                if (e.code == KeyCode.ENTER) {
                    val selectedFile = File("${dir.get()}/${selectionModel.selectedItem}")
                    if (selectedFile.isDirectory) {
                        dir.set(selectedFile.path)
                    }
                }
                else if (e.code == KeyCode.BACK_SPACE && dir.get() != home.path) {
                    dir.set(File(dir.get()).parent!!)
                }
            }
        }

        val topPane = VBox().apply {
            prefHeight = 30.0
            background = Background(BackgroundFill(Color.valueOf("#00ffff"), null, null))
            setOnMouseClicked { println("top pane clicked") }

            // Menu bar items
            val navMenu = Menu("Navigation").apply {
                items.add(MenuItem("Home Directory").apply {
                    setOnAction { dir.set(home.path) }
                })
                items.add(MenuItem("Parent Directory (Prev)").apply {
                    setOnAction { dir.set(File(dir.get()).parent!!) }
                })
                items.add(MenuItem("Open Directory (Next)").apply {
                    leftPane.selectionModel.selectedItemProperty().addListener { _, _, newSelection ->
                        isDisable = !File("${dir.get()}/${newSelection}").isDirectory
                    }
                    setOnAction { dir.set("${dir.get()}/${leftPane.selectionModel.selectedItem}") }
                })
            }
            val actionsMenu = Menu("Actions")
            val quitMenu = Menu("Quit").apply {
                items.add(MenuItem("Quit File Explorer").apply {
                    setOnAction { Platform.exit() }
                })
            }

            // Tool bar items
            val homeButton = Button("Home").apply {
                // Enable button if current directory is not home directory
                disableProperty().bind(dir.isEqualTo(home.path))
                // Change directory
                setOnAction { dir.set(home.path) }
            }
            val prevButton = Button("Prev").apply {
                // Enable button if current directory is not home directory
                disableProperty().bind(dir.isEqualTo(home.path))
                // Change directory
                setOnAction { dir.set(File(dir.get()).parent!!) }
            }
            val nextButton = Button("Next").apply {
                // Enable button if item currently selected is a directory
                isDisable = !File("${dir.get()}/${leftPane.selectionModel.selectedItem}").isDirectory
                leftPane.selectionModel.selectedItemProperty().addListener { _, _, newSelection ->
                    isDisable = !File("${dir.get()}/${newSelection}").isDirectory
                }
                // Change directory
                setOnAction { dir.set("${dir.get()}/${leftPane.selectionModel.selectedItem}") }
            }
            val renameButton = Button("Rename")
            val moveButton = Button("Move")
            val deleteButton = Button("Delete")

            children.addAll(
                MenuBar().apply {
                    menus.add(navMenu)
                    menus.add(actionsMenu)
                    menus.add(quitMenu)
                },
                ToolBar().apply {
                    items.add(homeButton)
                    items.add(prevButton)
                    items.add(nextButton)
                    items.add(renameButton)
                    items.add(moveButton)
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
