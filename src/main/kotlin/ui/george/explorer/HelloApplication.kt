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
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.nio.file.Files

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

        fun refreshFileList(list: ListView<String>) {
            list.items.clear()
            for (file in File(dir.get()).listFiles()!!.sorted()) {
                list.items.add(file.name)
            }
            list.selectionModel.select(0)
            statusBar.text = "${dir.get()}/${list.selectionModel.selectedItem ?: ""}"
        }

        val leftPane = ListView<String>().apply {
            // Override cell factory to handle double clicks on cells
            setCellFactory { _ ->
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

            fun updatePreview(displayFile: File) {
                centrePane.children.clear()
                if (!displayFile.canRead()) {
                    centrePane.children.add(Label("File cannot be read").apply {
                        font = Font.font(24.0)
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
                else if (!displayFile.isDirectory) {
                    centrePane.children.add(Label("Unsupported Type").apply {
                        font = Font.font(24.0)
                    })
                }
            }

            prefWidth = 200.0
            selectionModel.selectionMode = SelectionMode.SINGLE
            refreshFileList(this)
            updatePreview(File("${dir.get()}/${selectionModel.selectedItem}"))
            selectionModel.selectedItemProperty().addListener { _, _, newSelection ->
                val selectedFile = File("${dir.get()}/${newSelection}")
                statusBar.text = selectedFile.path
                updatePreview(selectedFile)
            }
            dir.addListener { _, _, _ -> refreshFileList(this) }
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

            fun renameFile(targetFile: File) {
                val newName = TextInputDialog().run {
                    title = "Rename File"
                    headerText = "Enter a new file name."
                    showAndWait()
                }
                if (newName.isPresent
                    && !targetFile.renameTo(File("${targetFile.parent}/${newName.get()}"))) {
                    Alert(Alert.AlertType.ERROR).run {
                        title = "Error Renaming File"
                        headerText = "An error occurred. The provided file name might be invalid."
                        showAndWait()
                    }
                }
                else if (newName.isPresent) {
                    val old_index = leftPane.selectionModel.selectedIndex
                    refreshFileList(leftPane)
                    leftPane.selectionModel.select(old_index)
                }
            }

            fun moveFile(targetFile: File) {
                val newDir = DirectoryChooser().apply {
                    title = "Move File"
                }.showDialog(stage)
                if (newDir != null) {
                    try {
                        if (!newDir.path.startsWith(home.path)) {
                            Alert(Alert.AlertType.ERROR).run {
                                title = "Error Moving File"
                                headerText = "Cannot move file outside of home directory."
                                showAndWait()
                            }
                            return
                        }
                        Files.move(targetFile.toPath(), newDir.toPath().resolve(targetFile.name))
                        dir.set(newDir.path)
                    }
                    catch (_: Exception) {
                        Alert(Alert.AlertType.ERROR).run {
                            title = "Error Moving File"
                            headerText = "An error occurred. Check the destination directory."
                            showAndWait()
                        }
                    }
                }
            }

            fun deleteFile(targetFile: File) {
                fun deleteHelper(target: File) {
                    if (!target.isDirectory) {
                        Files.delete(target.toPath())
                        return
                    }
                    for (file in target.listFiles()!!) {
                        if (file.isDirectory) {
                            deleteHelper(file)
                        }
                        else {
                            Files.delete(file.toPath())
                        }
                    }
                    Files.delete(target.toPath())
                }

                val confirm = Alert(Alert.AlertType.CONFIRMATION).run {
                    title = "Delete File"
                    headerText = "Are you sure you want to delete this file?"
                    showAndWait()
                }
                if (confirm.get() == ButtonType.OK) {
                    try {
                        deleteHelper(targetFile)
                        val old_index = leftPane.selectionModel.selectedIndex
                        refreshFileList(leftPane)
                        if (leftPane.items.size > 0) {
                            leftPane.selectionModel.select(old_index)
                        }
                        else if (old_index >= leftPane.items.size) {
                            leftPane.selectionModel.select(old_index - 1)
                        }
                        else {
                            leftPane.selectionModel.select(old_index)
                        }
                    }
                    catch (_: Exception) {
                        Alert(Alert.AlertType.ERROR).run {
                            title = "Error Deleting File"
                            headerText = "An error occurred. Check the file permissions."
                            showAndWait()
                        }
                    }
                }
            }

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
            val actionsMenu = Menu("Actions").apply {
                items.add(MenuItem("Rename File").apply {
                    setOnAction { renameFile(File("${dir.get()}/${leftPane.selectionModel.selectedItem}")) }
                })
                items.add(MenuItem("Move File").apply {
                    setOnAction { moveFile(File("${dir.get()}/${leftPane.selectionModel.selectedItem}")) }
                })
                items.add(MenuItem("Delete File").apply {
                    setOnAction { deleteFile(File("${dir.get()}/${leftPane.selectionModel.selectedItem}")) }
                })
            }
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
            val renameButton = Button("Rename").apply {
                setOnAction {
                    renameFile(File("${dir.get()}/${leftPane.selectionModel.selectedItem}"))
                }
            }
            val moveButton = Button("Move").apply {
                setOnAction {
                    moveFile(File("${dir.get()}/${leftPane.selectionModel.selectedItem}"))
                }
            }
            val deleteButton = Button("Delete").apply {
                setOnAction {
                    deleteFile(File("${dir.get()}/${leftPane.selectionModel.selectedItem}"))
                }
            }

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
