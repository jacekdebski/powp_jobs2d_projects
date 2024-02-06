package edu.kis.powp.jobs2d.command.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.swing.*;

import edu.kis.powp.appbase.gui.WindowComponent;
import edu.kis.powp.jobs2d.command.DriverCommand;
import edu.kis.powp.jobs2d.command.ComplexCommand;
import edu.kis.powp.jobs2d.command.manager.CommandManager;
import edu.kis.powp.jobs2d.command.visitor.PrintCommandVisitor;
import edu.kis.powp.jobs2d.command.visitor.DrawCommandVisitor;
import edu.kis.powp.jobs2d.command.utils.*;
import edu.kis.powp.observer.Subscriber;

public class CommandManagerWindow extends JFrame implements WindowComponent {

    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private CommandManager commandManager;

    private JTextArea currentCommandField;

    private String observerListString;
    private String commandString;
    private JTextArea observerListField;
    private JTextArea commandListField;
    private JPanel drawingPanel;

    public static final int NewCenterX = 150;
    public static final int NewCenterY = 150;

    private TextField commandFilePath;

    /**
     *
     */
    private static final long serialVersionUID = 9204679248304669948L;

    public CommandManagerWindow(CommandManager commandManager) {
        this.setTitle("Command Manager");
        this.setSize(1000, 600);
        Container content = this.getContentPane();
        content.setLayout(new GridBagLayout());

        this.commandManager = commandManager;

        GridBagConstraints c = new GridBagConstraints();

        observerListField = new JTextArea("");
        observerListField.setEditable(false);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.weighty = 1;
        content.add(observerListField, c);
        updateObserverListField();

        currentCommandField = new JTextArea("");
        currentCommandField.setEditable(false);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 1;
        c.weighty = 1;
        content.add(currentCommandField, c);
        updateCurrentCommandField();

        JLabel label = new JLabel("Path to command file:");
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.weighty = 1;
        content.add(label, c);

        commandFilePath = new TextField();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.weighty = 1;
        content.add(commandFilePath, c);

        JButton btnLoadFromFile = new JButton("Load from file");
        btnLoadFromFile.addActionListener((ActionEvent e) -> loadCommandFromFile());
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.weighty = 1;
        content.add(btnLoadFromFile, c);

        JButton btnClearCommand = new JButton("Clear command");
        btnClearCommand.addActionListener((ActionEvent e) -> this.clearCommand());
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.weighty = 1;
        content.add(btnClearCommand, c);

        JButton btnClearObservers = new JButton("Delete observers");
        btnClearObservers.addActionListener((ActionEvent e) -> this.deleteObservers());
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.weighty = 1;
        content.add(btnClearObservers, c);

        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                drawCommands(g);
            }
        };

        drawingPanel.setPreferredSize(new Dimension(300, 300));
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 1;
        c.weighty = 1;
        content.add(drawingPanel, c);

        commandListField = new JTextArea("");
        commandListField.setEditable(false);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 3;
        c.gridheight = 3;
        c.weighty = 1;
        content.add(commandListField, c);
        printCommands();
    }

    private void loadCommandFromFile() {
        if (commandFilePath.getText().isEmpty()) {
            return;
        }

        String fileExtension = FileHelper.getExtension(commandFilePath.getText());

        CommandLoaderType loaderType = CommandLoaderTypeHelper.fromString(fileExtension);

        CommandLoader loader = CommandLoaderFactory.getCommandLoader(loaderType);

        try (FileReader reader = new FileReader(commandFilePath.getText())) {
            Optional<ComplexCommand> commandList = loader.loadFromReader(reader);
            if (commandList.isPresent()) {
                ComplexCommand commands = commandList.get();
                commandManager.setCurrentCommand(commands);
            } else {
                clearCommand();
            }
        } catch (FileNotFoundException e) {
            logger.warning("Invalid file path");
        } catch (IOException e) {
            logger.warning("Failed to close file reader");
        }
    }

    private void clearCommand() {
        commandManager.clearCurrentCommand();
        updateCurrentCommandField();
        printCommands();
    }

    public void updateCurrentCommandField() {
        currentCommandField.setText(commandManager.getCurrentCommandString());
    }

    public void deleteObservers() {
        commandManager.getChangePublisher().clearObservers();
        this.updateObserverListField();
    }

    private void updateObserverListField() {
        observerListString = "";
        List<Subscriber> commandChangeSubscribers = commandManager.getChangePublisher().getSubscribers();
        for (Subscriber observer : commandChangeSubscribers) {
            observerListString += observer.toString() + System.lineSeparator();
        }
        if (commandChangeSubscribers.isEmpty())
            observerListString = "No observers loaded";

        observerListField.setText(observerListString);
    }

    @Override
    public void HideIfVisibleAndShowIfHidden() {
        updateObserverListField();
        if (this.isVisible()) {
            this.setVisible(false);
        } else {
            this.setVisible(true);
        }
    }
    public void drawCommands(Graphics g) {
        g.translate(NewCenterX, NewCenterY);
        DriverCommand currentCommand = commandManager.getCurrentCommand();
        if (currentCommand != null) {
            DrawCommandVisitor drawVisitor = new DrawCommandVisitor(g);
            currentCommand.accept(drawVisitor);
        }
    }

    public void printCommands() {
        PrintCommandVisitor printVisitor = new PrintCommandVisitor();
        DriverCommand currentCommand = commandManager.getCurrentCommand();
        System.out.print(currentCommand);
        if (currentCommand != null) {
            currentCommand.accept(printVisitor);
            commandListField.setText("Command step:" + System.lineSeparator() + printVisitor.getCommandsListString());
        }
        else{
            commandListField.setText("Command step:" + System.lineSeparator() + "No command loaded");
        }
    }
}
